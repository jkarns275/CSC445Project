package server;

import networking.headers.*;
import server.Workers.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    MulticastSocket multicastSocket;
    DatagramSocket serverSocket;
    DatagramPacket packet;
    byte[] message;
    public HashMap<String, Channel> channels = new HashMap<String, Channel>();
    public HashMap<InetAddress, User> users = new HashMap<InetAddress, User>();
    HeaderFactory headerFactory = HeaderFactory.getInstance();
    ExecutorService executor = Executors.newFixedThreadPool(10);

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
        serverSocket = new DatagramSocket(2703);
        multicastSocket = new MulticastSocket(27030);
        this.init();
        multicastSocket.joinGroup(InetAddress.getByName("230.0.0.0"));
    }

    public static Server getInstance() { return instance; }

    /*
     *
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listen();
    }

    /*
     * Need to decide if channels are all on the same mutlicast address or if they are on separate addresses
     */
    private void init() throws IOException {
        String[] addresses = {"230.0.0.0","230.0.0.1","230.0.0.2"};
        String[] names = {"Channel1","Channel2","Channel3"};
        long[] ids = {31415, 8314, 27345};
        for (int i = 0; i < addresses.length; i++) {
            InetAddress inetAddress = InetAddress.getByName(addresses[i]);
            channels.put(names[i],new Channel(names[i],inetAddress,ids[i]));
        }
    }

    /*
     *
     */
    public synchronized void sendMulticast(DatagramPacket datagramPacket) throws IOException {
        multicastSocket.send(datagramPacket);
    }

    /*
     *
     */
    public synchronized void send(DatagramPacket datagramPacket) throws IOException {
        serverSocket.send(datagramPacket);
    }

    /*
     *
     */
    public void listen() throws IOException {
        while (true) {
            message = new byte[2048];
            packet = new DatagramPacket(message, message.length);
            serverSocket.receive(packet);
//            multicastSocket.receive(packet);

            ByteArrayInputStream bis = new ByteArrayInputStream(packet.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(bis);
            Header header = headerFactory.readHeader(objectInputStream);

            if (header instanceof JoinHeader) {
                JoinWorker worker = new JoinWorker((JoinHeader) header, packet);
                executor.execute(worker);

            } else if (header instanceof WriteHeader) {
                WriteWorker worker = new WriteWorker((WriteHeader) header, packet.getAddress(), packet.getPort());
                executor.execute(worker);

            } else if (header instanceof LeaveHeader) {
                LeaveWorker leaveWorker = new LeaveWorker((LeaveHeader) header, packet);
                executor.execute(leaveWorker);

            } else if (header instanceof NakHeader) {
                NakWorker worker = new NakWorker();
                executor.execute(worker);

            } else if (header instanceof CommandHeader) {
                CommandWorker worker = new CommandWorker();
                executor.execute(worker);

            } else if (header instanceof ConglomerateHeader) {
                ConglomerateWorker worker = new ConglomerateWorker();
                executor.execute(worker);

            } else if (header instanceof ErrorHeader) {
                ErrorWorker worker = new ErrorWorker();
                executor.execute(worker);

            } else if (header instanceof HeartbeatHeader) {
                HeartBeatWorker worker = new HeartBeatWorker();
                executor.execute(worker);

            } else if (header instanceof InfoHeader) {
                InfoWorker worker = new InfoWorker();
                executor.execute(worker);

            }
        }
    }
}
