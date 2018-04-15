package server;

import networking.headers.*;
import server.Workers.JoinWorker;
import server.Workers.LeaveWorker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
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
        }
    }

    private Server() throws SocketException {
        serverSocket = new DatagramSocket(2703);
    }

    public static Server getInstance() { return instance; }

    public static void main(String[] args) throws IOException {
        getInstance().listen();
    }

    public void listen() throws IOException {
        while (true) {
            message = new byte[2048];
            packet = new DatagramPacket(message, message.length);
            serverSocket.receive(packet);
            ByteArrayInputStream bis = new ByteArrayInputStream(packet.getData());
            ObjectInputStream objectInputStream = new ObjectInputStream(bis);
            Header header = headerFactory.readHeader(objectInputStream);

            if (header instanceof JoinHeader) {
                JoinWorker joinWorker = new JoinWorker((JoinHeader) header, packet.getAddress());
                executor.execute(joinWorker);
            } else if (header instanceof WriteHeader) {

            } else if (header instanceof LeaveHeader) {
                LeaveWorker leaveWorker = new LeaveWorker((LeaveHeader) header);
                executor.execute(leaveWorker);
            } else if (header instanceof NakHeader) {

            } else if (header instanceof CommandHeader) {

            } else if (header instanceof ConglomerateHeader) {

            } else if (header instanceof ErrorHeader) {

            } else if (header instanceof HeartbeatHeader) {

            } else if (header instanceof InfoHeader) {

            } else {
                //error: not a viable header
            }
        }
    }
}
