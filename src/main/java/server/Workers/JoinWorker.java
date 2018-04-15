package server.Workers;

import networking.headers.JoinHeader;
import networking.headers.SourceHeader;
import server.Server;
import server.User;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class JoinWorker implements Runnable {
    JoinHeader joinHeader;
    InetAddress address;
    Server server = Server.getInstance();

    public JoinWorker(JoinHeader joinHeader, InetAddress address) {
        this.joinHeader = joinHeader;
        this.address = address;
    }

    public void run() {
        User user = new User(joinHeader.getDesiredUsername(), address);
        String assignedUsername = server.channels.get(joinHeader.getChannelName()).addUser(user);
        if (!server.users.containsKey(user.address)) {
            server.users.put(address, user);
        }
        SourceHeader sourceHeader = new SourceHeader(server.channels.get(joinHeader.getChannelName()).chatRoomID, joinHeader.getChannelName(), assignedUsername);
//        DatagramPacket packet = new DatagramPacket();

    }
}
