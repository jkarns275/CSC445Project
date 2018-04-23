package server.workers;

import networking.PacketSender;
import networking.headers.JoinHeader;
import networking.headers.SourceHeader;
import server.Channel;
import server.Server;
import server.User;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class JoinWorker implements Runnable {
    JoinHeader joinHeader;
    InetSocketAddress address;
    Server server = Server.getInstance();

    public JoinWorker(JoinHeader joinHeader, InetSocketAddress address) {
        this.joinHeader = joinHeader;
        this.address = address;
    }

    /*
     *
     */
    public void run() {
        User user = new User(joinHeader.getDesiredUsername(), address);
        for (Channel channel : Server.channels.values()) {
            if (channel.channelName.equals(joinHeader.getChannelName())) {
                String assignedUsername = channel.addUser(user);
                if (!server.users.containsKey(user.address)) {
                    server.users.put(address, user);
                }
                SourceHeader sourceHeader = new SourceHeader(Server.channels.get(joinHeader.getChannelName()).channelID,
                        joinHeader.getChannelName(), assignedUsername);
                PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(sourceHeader,address);
                packetSender.run();
                break;
            }
        }
//        String assignedUsername = Server.channels.get(joinHeader).addUser(user);
//        if (!server.users.containsKey(user.address)) {
//            server.users.put(address, user);
//        }
//        SourceHeader sourceHeader = new SourceHeader(Server.channels.get(joinHeader.getChannelName()).channelID,
//                joinHeader.getChannelName(), assignedUsername);
//        PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(sourceHeader,address);
//        packetSender.run();
    }
}
