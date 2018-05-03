package server.test;

import networking.HeaderIOManager;
import networking.PacketSender;
import networking.SocketRequest;
import networking.headers.ErrorHeader;
import networking.headers.LeaveHeader;
import server.Channel;
import server.Server;
import server.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static common.Constants.OP_ERROR;

public class LeaveTest {
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost",2703);
    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2,2,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static void main(String[] args) throws IOException, InterruptedException {
        LeaveTest leaveTest = new LeaveTest();
        if (!leaveTest.leaveTest()) {
            System.out.println("Failed test: joinTwiceTest");
        }
        System.exit(0);
    }

    private boolean leaveTest() throws IOException, InterruptedException {
        HeaderIOManager clientHeaderManager = new HeaderIOManager(new InetSocketAddress("localhost",8080),2);
        User user = new User("Ben",new InetSocketAddress("localhost",8080));
        Channel channel = new Channel("Test Channel", 1);
        Server.addChannel(channel);

        Server.getChannel((long) 1).addUser(user);
        Server.addUser(user);
        executorPool.execute(new ExecuteServer());

        LeaveHeader leaveHeader = new LeaveHeader(1);
        PacketSender packetSender = (PacketSender) clientHeaderManager.packetSender(leaveHeader, serverAddress);
        executorPool.execute(packetSender);

        Long timeout = System.currentTimeMillis();
        SocketRequest receive;
        for (;;) {
            while ((receive = clientHeaderManager.recv()) != null) {
                int opcode = receive.getHeader().opcode();

                if (opcode == OP_ERROR) {

                    boolean errorcode = false;
                    boolean errormessage = false;
                    boolean removeduser = false;

                    ErrorHeader errorHeader = (ErrorHeader) receive.getHeader();

                    System.out.println("Received an error header with fields: ");
                    System.out.println(" -Error code: " + errorHeader.getErrorCode());
                    System.out.println(" -Error message: " + errorHeader.getErrorMsg());

                    if (errorHeader.getErrorCode() == 0x00) errorcode = true;
                    if (errorHeader.getErrorMsg().equals("Connection closed")) errormessage = true;
                    if (Server.getChannel((long) 1).users.values().isEmpty())  removeduser = true;

                    return errorcode && errormessage && removeduser;
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
