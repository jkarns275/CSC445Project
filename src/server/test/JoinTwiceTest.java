package server.test;

import networking.HeaderIOManager;
import networking.PacketSender;
import networking.SocketRequest;
import networking.headers.ErrorHeader;
import networking.headers.JoinHeader;
import networking.headers.SourceHeader;
import server.Channel;
import server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static common.Constants.OP_ERROR;
import static common.Constants.OP_SOURCE;
import static networking.headers.ErrorHeader.ERROR_FAILED_CHANNEL_JOIN;

public class JoinTwiceTest {
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost",2703);
    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2,2,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static void main(String[] args) throws IOException, InterruptedException {
        JoinTwiceTest joinTests = new JoinTwiceTest();
        if (!joinTests.joinTwiceTest()) {
            System.out.println("Failed test: joinTwiceTest");
        }
        System.exit(0);
    }

    private boolean joinTwiceTest() throws IOException, InterruptedException {

        Channel channel = new Channel("Test Channel", 1);
        Server.addChannel(channel);
        executorPool.execute(new ExecuteServer());

        HeaderIOManager clientHeaderManager1 = new HeaderIOManager(new InetSocketAddress("localhost",8080),2);
        JoinHeader joinHeader = new JoinHeader("Ben","Test Channel");
        PacketSender packetSender = (PacketSender) clientHeaderManager1.packetSender(joinHeader, serverAddress);
        executorPool.execute(packetSender);
        executorPool.execute(packetSender);

        Long timeout = System.currentTimeMillis();
        SocketRequest receive;
        int numberOfSourceHeadersReceived = 0;
        for (;;) {
            while ((receive = clientHeaderManager1.recv()) != null) {
                int opcode = receive.getHeader().opcode();

                if (opcode == OP_SOURCE) {

                    SourceHeader sourceHeader = (SourceHeader) receive.getHeader();

                    System.out.println("Received a source header with fields: ");
                    System.out.println(" -Channel name: " + sourceHeader.getChannelName());
                    System.out.println(" -Channel id: " + sourceHeader.getChannelID());
                    System.out.println(" -Assigned username: " + sourceHeader.getAssignedUsername() + "\n");

                } else if (opcode == OP_ERROR) {
                    ErrorHeader errorHeader = (ErrorHeader) receive.getHeader();

                    boolean servercontains = false;
                    boolean channelcontains = false;
                    boolean errorcode = false;
                    boolean errormessage = false;

                    System.out.println("Received an error header with fields: ");
                    System.out.println(" -Error code: " + errorHeader.getErrorCode());
                    System.out.println(" -Channel id: " + errorHeader.getErrorMsg() + "\n");

                    if (errorHeader.getErrorCode() == ERROR_FAILED_CHANNEL_JOIN) {
                        errorcode = true;
                    }
                    if (errorHeader.getErrorMsg().equals("Failed join - user already in channel")) {
                        errormessage = true;
                    }
                    if (Server.users.contains(new InetSocketAddress("localhost",8080))) {
                        servercontains = true;
                    }
                    if (Server.getChannel((long) 1) != null) {
                        if (Server.getChannel((long) 1).containsUser("Ben")) {
                            channelcontains = true;
                        }
                    }
                    return servercontains && channelcontains && errorcode && errormessage;
                } else {
                    System.out.println("Received header of type: " + opcode + "\n");
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
