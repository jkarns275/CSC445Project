package server;

import networking.HeaderIOManager;
import networking.HeartbeatManager;
import networking.PacketSender;
import networking.SocketRequest;
import networking.headers.*;
import server.workers.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.*;

import static common.Constants.*;

/**
 * @static private Constructor()
 * @getInstance() to obtain instance
 */
public class Server {
    private final int MAX_THREADS = 15;
    private final int MAX_POOL_SIZE = 20;
    private final int KEEP_ALIVE_TIME = 100;

    public static HashMap<Long, Channel> channels = new HashMap<>();
    public static ArrayList<InetSocketAddress> users = new ArrayList<>();

    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(MAX_THREADS,MAX_POOL_SIZE,KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static HeaderIOManager headerManager;
    public static HeartbeatManager heartbeatManager;

    private final static Server instance;
    static {
      Server instance1;
      try {
            instance1 = new Server(2703);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
            instance1 = null;
        }
      instance = instance1;
    }

    private Server(int port) throws IOException {
        headerManager = new HeaderIOManager(new InetSocketAddress(port),15);
        heartbeatManager = new HeartbeatManager();
    }

    /**
     * @return Server - the server instance
     */
    public static Server getInstance() { return instance; }

    /**
     * @param args
     * Main method to be run when starting the server
     */
    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.listen();
    }

    /**
     * @param channelID - unique long identifying the particular channel
     * @return Channel - channel corresponding to channelID
     */
    public static Channel getChannel(Long channelID) {
        if (channels.containsKey(channelID)) {
            return channels.get(channelID);
        }
        return null;
    }

    /**
     * @param channel - new channel to be added to the hashmap of available channels
     * @return boolean - indicates if channel was successfully added
     */
    public static synchronized boolean addChannel(Channel channel) {
        for (Channel chan : channels.values()) {
            if (chan.channelName.equals(channel.channelName)) {
                return false;
            }
        }
        channels.put(channel.channelID,channel);
        return true;
    }

    /**
     * @param user - new user joining the chat server
     * @return boolean - indicates if user was successfully added
     */
    public static synchronized boolean addUser(User user) {
        if (!users.contains(user.address)) {
            users.add(user.address);
            return true;
        }
        return false;
    }

    /**
     * @param header - header to be sent to client
     * @param address - address of the client the header will be sent to
     */
    public static void sendPacket(Header header, InetSocketAddress address) {
        PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(header,address);
        packetSender.run();
    }

    /**
     * Starts the server listening on the initialized port number. Server starts up the heartbeat manager in preparation
     * to connect with clients. Server receives header packets from clients, creates corresponding worker threads, and
     * executes the worker threads with the thread pool so as not to block the main server thread.
     */
    public void listen() {
        while (true) {
            try {
                headerManager.update();
                for (Channel channel : channels.values()) {
                  Optional<HashSet<InetSocketAddress>> clients = heartbeatManager.getActiveClients(channel.channelID);
                  channel.update(clients.orElse(new HashSet<>()));
                }
                SocketRequest receive = headerManager.recv();

                if (receive == null) continue;
                    Header header = receive.getHeader();
                    InetSocketAddress srcAddr = receive.getAddress();

                    /*
                    if (!users.contains(srcAddr) && header.opcode() != Constants.OP_JOIN) {
                        continue;
                    }
                    */

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
                            ErrorHeader errorHeader = new ErrorHeader((byte) 0x01, "Invalid opcode");
                            executorPool.execute(new ErrorWorker(errorHeader, srcAddr));
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
