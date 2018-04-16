package server.Workers;

import networking.headers.JoinHeader;
import networking.headers.SourceHeader;
import server.Server;
import server.User;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class JoinWorker implements Runnable {
    JoinHeader joinHeader;
    InetAddress address;
    int port;
    Server server = Server.getInstance();
    DatagramPacket packet;

    public JoinWorker(JoinHeader joinHeader, DatagramPacket packet) {
        this.joinHeader = joinHeader;
        this.address = packet.getAddress();
        this.port = packet.getPort();
        this.packet = packet;
    }

    /*
     *
     */
    public void run() {
        User user = new User(joinHeader.getDesiredUsername(), address, port);
        String assignedUsername = server.channels.get(joinHeader.getChannelName()).addUser(user);
        if (!server.users.containsKey(user.address)) {
            server.users.put(address, user);
        }
        SourceHeader sourceHeader = new SourceHeader(server.channels.get(joinHeader.getChannelName()).channelID,
                joinHeader.getChannelName(), assignedUsername);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
            sourceHeader.writeObject(objectOutputStream);
            byte[] bytes = bos.toByteArray();
            DatagramPacket datagram = new DatagramPacket(bytes,bytes.length,address, port);
            server.send(datagram); //send confirmation back to client
            server.sendMulticast(packet); //multicast original packet to let other clients know another user has joined a channel
            //Need to add ip address and packet to ack queue
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
