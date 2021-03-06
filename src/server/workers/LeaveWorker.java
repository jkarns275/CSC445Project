package server.workers;

import networking.headers.InfoHeader;
import networking.headers.LeaveHeader;
import server.Channel;
import server.Server;
import server.User;

import java.net.InetSocketAddress;
import java.util.Optional;

public class LeaveWorker implements Runnable {
    LeaveHeader leaveHeader;
    InetSocketAddress address;

    public LeaveWorker(LeaveHeader leaveHeader, InetSocketAddress address) {
        this.leaveHeader = leaveHeader;
        this.address = address;
    }

    public void run() {
      /*
        Channel channel = Server.channels.get(leaveHeader.channelID);
        String userNickname = channel.getUsers().values().stream()
          .filter((User u) -> u.address.equals(address)).findAny()
          .map(u -> u.username)
          .orElse("Mystery");

        InfoHeader infoHeader = new InfoHeader(leaveHeader.channelID, InfoHeader.INFO_CLOSED, channel
          .getAndIncrementMsgID(),
                "User " + userNickname + " has left.");

        channel.sendPacket(infoHeader, address);

        Optional<User> user = channel.users.values().stream()
          .filter(u -> u.address.getAddress().equals(address.getAddress()))
          .findFirst();

        user.ifPresent(channel::removeUser);
        */
    }
}
