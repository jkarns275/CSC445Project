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

/*
 * This test is outdated. It was written under the assumption that a user could join a single channel multiple times.
 * The test was supposed to test the naming logic of the channel addUser method.
 */
public class JoinTenTimesTest {
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost",2703);
    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2,2,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static void main(String[] args) throws IOException, InterruptedException {
        JoinTenTimesTest joinTest = new JoinTenTimesTest();
        if (!joinTest.joinTest()) {
            System.out.println("Failed test: joinTenTimesTest");
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
        for (int i = 0; i < 10; i++) executorPool.execute(packetSender);


        Long timeout = System.currentTimeMillis();
        SocketRequest receive;
        int numberOfSourceHeadersReceived = 0;
        for (;;) {
            while ((receive = clientHeaderManager.recv()) != null) {
                int opcode = receive.getHeader().opcode();

                if (opcode == OP_SOURCE) {

                    if (numberOfSourceHeadersReceived < 9) {
                        numberOfSourceHeadersReceived++;
                        continue;
                    }

                    boolean servercontains = false;
                    boolean channelcontains = false;
                    boolean channelname = false;
                    boolean channelid = false;
                    boolean assignedname = false;

                    SourceHeader sourceHeader = (SourceHeader) receive.getHeader();

                    System.out.println("Received a source header with fields: ");
                    System.out.println(" -Channel name: " + sourceHeader.getChannelName());
                    System.out.println(" -Channel id: " + sourceHeader.getChannelID());
                    System.out.println(" -Assigned username: " + sourceHeader.getAssignedUsername());

                    if (Server.users.contains(new InetSocketAddress("localhost",8080))) {
                        servercontains = true;
                    }
                    if (Server.getChannel((long) 1) != null) {
                        if (Server.getChannel((long) 1).users.containsKey("Ben") &&
                                Server.getChannel((long) 1).users.containsKey("Ben1") &&
                                Server.getChannel((long) 1).users.containsKey("Ben2") &&
                                Server.getChannel((long) 1).users.containsKey("Ben3") &&
                                Server.getChannel((long) 1).users.containsKey("Ben4") &&
                                Server.getChannel((long) 1).users.containsKey("Ben5") &&
                                Server.getChannel((long) 1).users.containsKey("Ben6") &&
                                Server.getChannel((long) 1).users.containsKey("Ben7") &&
                                Server.getChannel((long) 1).users.containsKey("Ben8") &&
                                Server.getChannel((long) 1).users.containsKey("Ben9")) {
                            channelcontains = true;
                        }
                    }
                    if (sourceHeader.getChannelName().equals("Test Channel")) {
                        channelname = true;
                    }
                    if (sourceHeader.getChannelID() == 1) {
                        channelid = true;
                    }
                    if (sourceHeader.getAssignedUsername().equals("Ben9")) {
                        assignedname = true;
                    }
                    return servercontains && channelcontains && channelname && channelid && assignedname;
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
