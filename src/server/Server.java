package server;

import common.Constants;
import networking.HeaderIOManager;
import networking.HeartbeatManager;
import networking.SocketRequest;
import networking.headers.*;
import server.workers.*;

import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.*;

import static common.Constants.*;

public class Server {
    public static int port = 2703;
    private final int MAX_THREADS = 15;
    private final int MAX_POOL_SIZE = 20;
    private final int KEEP_ALIVE_TIME = 100;

    public static HashMap<Long, Channel> channels = new HashMap<>();
    public HashMap<InetSocketAddress, User> users = new HashMap<>();

    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(MAX_THREADS,MAX_POOL_SIZE,KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static HeaderIOManager headerManager;
    public static HeartbeatManager heartbeatManager;

    private static Server instance = null;

    /*
    static {
        try {
            instance = new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private Server() throws IOException {
        this.init();
        headerManager = new HeaderIOManager(new InetSocketAddress(port),4);
        heartbeatManager = new HeartbeatManager();
    }

    public static Server getInstance() { return instance; }

    /*
     *
     */
    public static void main(String[] args) throws IOException, InterruptedException {
      if (args.length == 0) {
        System.out.println("No port number supplied, using 2703.");
      } else {
        try {
          port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
          System.out.println("Supplied port number was not a valid integer, using 2703 instead.");
          port = 2703;
        }
      }
      Server server = new Server();
        server.listen();
    }

    /*
     *
     */
    private void init() {
        String[] names = {"Channel1","Channel2","Channel3"};
        long[] ids = { 31415, 8314, 27345 };
        for (int i = 0; i < names.length; i++) {
            channels.put(ids[i],new Channel(names[i],ids[i]));
        }
    }

    public static Channel getChannel(Long channelID) {
        if (channels.containsKey(channelID)) {
            return channels.get(channelID);
        }
        return null;
    }

    /*
     *
     */
    public void listen() {
        while (true) {
            try {
                System.out.print(",");
                headerManager.update();
                heartbeatManager.update();
                for (Channel channel : channels.values()) channel.update();
                SocketRequest receive;
                while ((receive = headerManager.recv()) != null) {
                    System.out.println("Received " + receive.getHeader() + " from " + receive.getAddress());
                    Header header = receive.getHeader();
                    InetSocketAddress srcAddr = receive.getAddress();

                    if (!users.containsKey(srcAddr) && header.opcode() != Constants.OP_JOIN) {
                        continue;
                    }

                    switch (header.opcode()) {
                        case OP_WRITE:
                            executorPool.execute(new WriteWorker((WriteHeader) header, srcAddr));
                            break;

                        case OP_JOIN:
                            executorPool.execute(new JoinWorker((JoinHeader) header, srcAddr));
                            break;

                        case OP_LEAVE:
                            executorPool.execute(new LeaveWorker((LeaveHeader) header, srcAddr));
                            break;

                        case OP_NAK:
                            executorPool.execute(new NakWorker((NakHeader) header, srcAddr));
                            break;

                        case OP_ERROR:
                            executorPool.execute(new ErrorWorker((ErrorHeader) header, srcAddr));
                            break;

                        case OP_HEARTBEAT:
                            executorPool.execute(new HeartbeatWorker((HeartbeatHeader) header, srcAddr));
                            break;

                        case OP_COMMAND:
                            executorPool.execute(new CommandWorker((CommandHeader) header, srcAddr));
                            break;

                        case OP_CONG:
                            executorPool.execute(new ConglomerateWorker((ConglomerateHeader) header, srcAddr));
                            break;

                        case OP_ACK:
                            headerManager.processAckHeader((AckHeader) header, srcAddr);
                            break;

                        default:
                            System.err.println("Received erroneous opcode, " + header.opcode() + ", from: " + srcAddr);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
