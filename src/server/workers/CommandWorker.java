package server.workers;

import networking.headers.CommandHeader;
import networking.headers.ErrorHeader;
import networking.headers.InfoHeader;
import server.Channel;
import server.Server;

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
        Channel channel = Server.getChannel(commandHeader.getChannelID());

        System.out.println("Command following:");
        System.out.println(commandHeader.getCommand());

        String command[] = commandHeader.getCommand().split(" ");

        if (command[0].equals("/me")) {
            commandHeader.setMsgID(channel.getAndIncrementMsgID());
            channel.addToTreeMap(commandHeader.getMsgID(), commandHeader);
            channel.sendPacket(commandHeader, address);

        } else if (command[0].equals("/op")) {
            switch(command[1]) {
                case "kick":
                    channel.users.remove(command[2]);
                    msgID = channel.getAndIncrementMsgID();
                    infoHeader = new InfoHeader(channel.channelID, (byte) 0x00, msgID, "User, " + command[2]
                            + ", kicked from channel, " + channel.channelID + ".");
                    channel.addToTreeMap(msgID, infoHeader);
                    channel.sendPacket(infoHeader, address);
                    break;

                case "mute":
                    System.out.println(command[2]);
                    if (channel.users.get(command[2]) != null) {
                        System.out.println("User muted");
                        channel.users.get(command[2]).setMuted(true);
                        msgID = channel.getAndIncrementMsgID();
                        infoHeader = new InfoHeader(channel.channelID, (byte) 0x01, msgID, "User, " + command[2]
                                + ", muted in channel, " + channel.channelID + ".");
                        channel.addToTreeMap(msgID, infoHeader);
                        channel.sendPacket(infoHeader, address);
                    } else {
                        System.out.println("Null");
                        ErrorHeader errorHeader = new ErrorHeader(ERROR_NO_SUCH_USER, "User " + command[2]
                                + " does not exists in channel" + channel.channelID + ".");
                        channel.sendPacket(errorHeader,address);
                    }
                    break;

                case "unmute":
                    channel.users.get(command[2]).setMuted(false);
                    msgID = channel.getAndIncrementMsgID();
                    infoHeader = new InfoHeader(channel.channelID, (byte) 0x02, msgID, "User, " + command[2]
                            + ", unmuted in channel, " + channel.channelID + ".");
                    channel.addToTreeMap(msgID, infoHeader);
                    channel.sendPacket(infoHeader, address);
                    break;

                default:
                    System.err.println("Received erroneous command: " + command[1]);
            }
        } else {
            new Error("Incorrect header");
        }
    }
}
