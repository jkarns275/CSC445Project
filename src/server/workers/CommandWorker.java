package server.workers;

import networking.headers.CommandHeader;
import networking.headers.ErrorHeader;
import networking.headers.InfoHeader;
import server.Channel;
import server.Server;
import server.User;

import java.net.InetSocketAddress;

import static networking.headers.ErrorHeader.ERROR_NO_SUCH_USER;

public class CommandWorker implements Runnable {
    CommandHeader commandHeader;
    InfoHeader infoHeader;
    InetSocketAddress address;
    long msgID;

    public CommandWorker(CommandHeader header, InetSocketAddress address) {
        this.commandHeader = header;
        this.address = address;
    }

    public void run() {
        try {
          Channel channel = Server.getChannel(commandHeader.getChannelID());
          String command[] = commandHeader.getCommand().split(" ");

          if (command[0].equals("/me")) {
            commandHeader.setMsgID(channel.getAndIncrementMsgID());
            channel.addToBufferedTreeMap(commandHeader.getMsgID(), commandHeader);
            channel.sendPacket(commandHeader, address);

          } else if (command[0].equals("/op")) {
            User user;
            switch (command[1]) {
              case "kick":
                user = channel.users.remove(command[2]);
                msgID = channel.getAndIncrementMsgID();
                channel.incrementLastLoggedMsg(1);
                infoHeader = new InfoHeader(channel.channelID, (byte) 0x00, msgID, "User, " + command[2]
                  + ", kicked from channel, " + channel.channelID + ".");
                channel.sendPacket(infoHeader, user.address);

                infoHeader = new InfoHeader(channel.channelID, (byte) 0x03, msgID, "User, " + command[2]
                  + ", kicked from channel, " + channel.channelID + ".");
                channel.addToBufferedTreeMap(msgID, infoHeader);
                channel.sendPacket(infoHeader);
                break;

              case "mute":
                user = channel.users.get(command[2]);
                if (user != null) {
                  System.out.println("User muted");
                  user.setMuted(true);
                  msgID = channel.getAndIncrementMsgID();
                  channel.incrementLastLoggedMsg(1);
                  infoHeader = new InfoHeader(channel.channelID, (byte) 0x01, msgID, "User, " + command[2]
                    + ", muted in channel, " + channel.channelID + ".");
                  channel.sendPacket(infoHeader, user.address);

                  infoHeader = new InfoHeader(channel.channelID, (byte) 0x03, msgID, "User, " + command[2]
                    + ", muted in channel, " + channel.channelID + ".");
                  channel.addToBufferedTreeMap(msgID, infoHeader);
                  channel.sendPacket(infoHeader);
                } else {
                  ErrorHeader errorHeader = new ErrorHeader(ERROR_NO_SUCH_USER, "User " + command[2]
                    + " does not exists in channel" + channel.channelID + ".");
                  channel.sendPacket(errorHeader, address);
                }
                break;

              case "unmute":
                user = channel.users.get(command[2]);
                user.setMuted(false);
                msgID = channel.getAndIncrementMsgID();
                channel.incrementLastLoggedMsg(1);
                infoHeader = new InfoHeader(channel.channelID, (byte) 0x02, msgID, "User, " + command[2]
                  + ", unmuted in channel, " + channel.channelID + ".");
                channel.sendPacket(infoHeader, user.address);

                infoHeader = new InfoHeader(channel.channelID, (byte) 0x03, msgID, "User, " + command[2]
                  + ", unmuted in channel, " + channel.channelID + ".");
                channel.addToBufferedTreeMap(msgID, infoHeader);
                channel.sendPacket(infoHeader);
                break;

              default:
                System.err.println("Received erroneous command: " + command[1]);
            }
          } else if (command[0].equals("/listusers")) {
            StringBuilder sb = new StringBuilder("Users: ");
            channel.users.keySet().stream().forEach((username) -> {
                sb.append(username);
                sb.append(", ");
            });
            channel.sendPacket(new InfoHeader(channel.channelID, InfoHeader.INFO_SERVER_MSG, channel
              .getAndIncrementMsgID(), sb.toString()));
          } else if (command[0].equals("/listchannels")) {
            StringBuilder sb = new StringBuilder("Channels: ");
            Server.channels.values().stream()
              .map((chan) -> chan.channelName)
              .forEach((chanName) -> { sb.append(chanName); sb.append(", "); });
            channel.sendPacket(new InfoHeader(channel.channelID, InfoHeader.INFO_SERVER_MSG, channel.getAndIncrementMsgID(), sb
              .toString()));
          } else {
            throw new Error("Incorrect header");
          }
        } catch (Exception e) {
          System.err.println("Encountered error in CommandWorker");
          e.printStackTrace();
        }
    }
}
