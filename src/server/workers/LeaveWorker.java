package server.workers;

import networking.headers.ErrorHeader;
import networking.headers.InfoHeader;
import networking.headers.LeaveHeader;
import server.Channel;
import server.Server;
import server.User;

import java.net.InetSocketAddress;

public class LeaveWorker implements Runnable {
    LeaveHeader leaveHeader;
    InetSocketAddress address;

    public LeaveWorker(LeaveHeader leaveHeader, InetSocketAddress address) {
        this.leaveHeader = leaveHeader;
        this.address = address;
    }

    public void run() {
        Channel channel = Server.channels.get(leaveHeader.channelID);
        for (User user : channel.users.values()) {
            if (user.address.getAddress().equals(address.getAddress())) {
                channel.removeUser(user);
                break;
            }
        }
        InfoHeader infoHeader = new InfoHeader(leaveHeader.channelID, (byte) 0x04, 0,
                "Connection to channel " + leaveHeader.channelID + " successfully closed");
        channel.sendPacket(infoHeader,address);
    }
}
