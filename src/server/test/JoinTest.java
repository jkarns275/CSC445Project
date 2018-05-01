package server.test;

import networking.HeaderIOManager;
import networking.PacketSender;
import networking.SocketRequest;
import networking.headers.JoinHeader;
import networking.headers.SourceHeader;
import server.Channel;
import server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static common.Constants.OP_SOURCE;

public class JoinTest {
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost",2703);
    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2,2,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static void main(String[] args) throws IOException, InterruptedException {
        JoinTest joinTests = new JoinTest();
        if (!joinTests.joinTest()) {
            System.out.println("Failed test: joinTest");
        }
        System.exit(0);
    }

    private boolean joinTest() throws IOException, InterruptedException {
        HeaderIOManager clientHeaderManager = new HeaderIOManager(new InetSocketAddress("localhost",8080),2);
        Channel channel = new Channel("Test Channel", 1);
        Server.addChannel(channel);
        executorPool.execute(new ExecuteServer());

        JoinHeader joinHeader = new JoinHeader("Ben","Test Channel");
        PacketSender packetSender = (PacketSender) clientHeaderManager.packetSender(joinHeader, serverAddress);
        executorPool.execute(packetSender);

        Long timeout = System.currentTimeMillis();
        SocketRequest receive;
        for (;;) {
            while ((receive = clientHeaderManager.recv()) != null) {
                int opcode = receive.getHeader().opcode();

                if (opcode == OP_SOURCE) {
                    boolean channelname = false;
                    boolean channelid = false;
                    boolean assignedname = false;

                    SourceHeader sourceHeader = (SourceHeader) receive.getHeader();

                    System.out.println("Received a source header with fields: ");
                    System.out.println(" -Channel name: " + sourceHeader.getChannelName());
                    System.out.println(" -Channel id: " + sourceHeader.getChannelID());
                    System.out.println(" -Assigned username: " + sourceHeader.getAssignedUsername());

                    if (sourceHeader.getChannelName().equals("Test Channel")) {
                        channelname = true;
                    }
                    if (sourceHeader.getChannelID() == 1) {
                        channelid = true;
                    }
                    if (sourceHeader.getAssignedUsername().equals("Ben")) {
                        assignedname = true;
                    }
                    return channelname && channelid && assignedname;
                } else {
                    System.out.println("Received header of type: " + opcode);
                }
            }
            if (System.currentTimeMillis() - timeout > 1000) {
                System.out.println("Test timed out: no response from server.");
                System.exit(1);
                return false;
            }
        }
    }

    class ExecuteServer implements Runnable {
        Server server = Server.getInstance();

        @Override
        public void run() {
            server.listen();
        }
    }
}
