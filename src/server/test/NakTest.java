package server.test;

import networking.HeaderIOManager;
import networking.PacketSender;
import networking.SocketRequest;
import networking.headers.NakHeader;
import networking.headers.WriteHeader;
import server.Channel;
import server.Server;
import server.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static common.Constants.OP_WRITE;

public class NakTest {
    public static InetSocketAddress serverAddress = new InetSocketAddress("localhost",2703);
    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(2,2,1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static void main(String[] args) throws IOException, InterruptedException {
        NakTest nakTest = new NakTest();
        if (!nakTest.nakTest()) {
            System.out.println("Failed test: joinTest");
        }
        System.exit(0);
    }

    private boolean nakTest() throws IOException, InterruptedException {
        HeaderIOManager clientHeaderManager = new HeaderIOManager(new InetSocketAddress("localhost",8080),2);
        Channel channel = new Channel("Test Channel", 4);
        User user = new User("bvalenti",new InetSocketAddress("localhost",8080));
        channel.addUser(user);
        Server.addUser(user);

        WriteHeader writeHeader1 = new WriteHeader(4,1L,"First message","bvalenti");
        WriteHeader writeHeader2 = new WriteHeader(4,2L,"Second message","bvalenti");
        WriteHeader writeHeader3 = new WriteHeader(4,3L,"Third message","bvalenti");

        channel.addToBufferedTreeMap(1,writeHeader1);
        channel.addToBufferedTreeMap(2,writeHeader2);
        channel.addToBufferedTreeMap(3,writeHeader3);

        Server.addChannel(channel);
        executorPool.execute(new ExecuteServer());

        NakHeader nakHeader = new NakHeader(2L,3L,4);
        PacketSender packetSender = (PacketSender) clientHeaderManager.packetSender(nakHeader, serverAddress);
        executorPool.execute(packetSender);

        Long timeout = System.currentTimeMillis();
        SocketRequest receive;
        for (;;) {
            while ((receive = clientHeaderManager.recv()) != null) {
                int opcode = receive.getHeader().opcode();

                if (opcode == OP_WRITE) {
                    boolean msgid = false;
                    boolean msgcontents = false;

                    WriteHeader writeHeader = (WriteHeader) receive.getHeader();
                    System.out.println("\nReceived a write header with fields: ");
                    System.out.println(" -User name: " + writeHeader.getUsername());
                    System.out.println(" -Channel id: " + writeHeader.getChannelID());
                    System.out.println(" -MsgID: " + writeHeader.getMsgID());

                    if (writeHeader.getMsgID() == 2) {
                        msgid = true;
                    }
                    if (writeHeader.getMsg().equals("Second message")) {
                        msgcontents = true;
                    }
                    return msgid && msgcontents;
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
