package server.workers;

import networking.PacketSender;
import networking.headers.ErrorHeader;
import networking.headers.InfoHeader;
import networking.headers.JoinHeader;
import networking.headers.SourceHeader;
import server.Channel;
import server.Server;
import server.User;

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
        boolean channelExists = false;

        for (Channel channel : Server.channels.values()) {
            if (channel.channelName.equals(joinHeader.getChannelName())) {
                channelExists = true;
                String assignedUsername = channel.addUser(user);
                if (assignedUsername == null) {
                    ErrorHeader header = new ErrorHeader((byte)0x04,"Failed join - user already in channel");
                    Server.sendPacket(header,address);
                    return;
                }
                if (!Server.users.contains(address)) {
                    Server.addUser(user);
                }
                SourceHeader sourceHeader = new SourceHeader(channel.channelID, channel.channelName, assignedUsername);
                PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(sourceHeader,address);
                packetSender.run();
                InfoHeader info = new InfoHeader(channel.channelID, InfoHeader.INFO_SERVER_MSG, -1,
                  "User " + assignedUsername + " has joined.");
                break;
            }
        }
        if (!channelExists) {
            long newID = 0;
            for (Channel channel : Server.channels.values()) {
                if (channel.channelID > newID) newID = channel.channelID;
            }
            Channel newChannel = new Channel(joinHeader.getChannelName(), ++newID);
            String assignedUsername = newChannel.addUser(user);
            boolean added = Server.addChannel(newChannel);
            if (added) {
                if (!Server.users.contains(address)) {
                    Server.addUser(user);
                }
                SourceHeader sourceHeader = new SourceHeader(newChannel.channelID, newChannel.channelName, assignedUsername);
                Server.sendPacket(sourceHeader,address);
            } else {
                ErrorHeader header = new ErrorHeader((byte)0x03, "Failed create - channel exists");
                Server.sendPacket(header,address);
            }
        }
    }
}
