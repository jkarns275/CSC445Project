package server;

import networking.HeaderIOManager;
import networking.HeartbeatManager;
import networking.SocketRequest;
import networking.headers.*;
import server.workers.*;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.*;

public class Server {
    public static final int port = 2703;
    private final int MAX_THREADS = 10;
    private final int MAX_POOL_SIZE = 15;
    private final int KEEP_ALIVE_TIME = 100;

    public static HashMap<Long, Channel> channels = new HashMap<>();
    public HashMap<InetSocketAddress, User> users = new HashMap<>();

    private ThreadPoolExecutor executorPool = new ThreadPoolExecutor(MAX_THREADS,MAX_POOL_SIZE,KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(1024));

    public static HeaderIOManager headerManager;

    public static HeartbeatManager heartbeatManager;

    DatagramPacket packet;

    private static Server instance = null;
    static {
        try {
            instance = new Server();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Server() throws IOException {
        this.init();
        headerManager = new HeaderIOManager(new InetSocketAddress(port),15);
        heartbeatManager = new HeartbeatManager();
    }

    public static Server getInstance() { return instance; }

    /*
     *
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
        server.listen();
    }

    /*
     *
     */
    private void init() {
        String[] names = {"Channel1","Channel2","Channel3"};
        long[] ids = {31415, 8314, 27345};
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
    public void listen() throws IOException, InterruptedException {
        while (true) {
            SocketRequest request = headerManager.recv();
            Header header = request.getHeader();
            switch (header.opcode()) {
                case 0x00:
                    executorPool.execute(new WriteWorker((WriteHeader) header,
                            new InetSocketAddress(packet.getAddress(), packet.getPort())));
                    break;

                case 0x01:
                    executorPool.execute(new JoinWorker((JoinHeader) header,
                            new InetSocketAddress(packet.getAddress(),packet.getPort())));
                    break;

                case 0x02:
                    executorPool.execute(new LeaveWorker((LeaveHeader) header,
                            new InetSocketAddress(packet.getAddress(),packet.getPort())));
                    break;

                case 0x04:
                    executorPool.execute(new NakWorker((NakHeader) header,
                            new InetSocketAddress(packet.getAddress(),packet.getPort())));
                    break;

                case 0x05:
                    executorPool.execute(new ErrorWorker((ErrorHeader) header));
                    break;

                case 0x06:
                    executorPool.execute(new HeartbeatWorker((HeartbeatHeader) header));
                    break;

                case 0x08:
                    executorPool.execute(new CommandWorker((CommandHeader) header));
                    break;

                case 0x0FF:
                    executorPool.execute(new ConglomerateWorker((ConglomerateHeader) header));
                    break;

            }
        }
    }
}
