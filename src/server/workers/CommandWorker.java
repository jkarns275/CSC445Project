package server.workers;

import networking.headers.CommandHeader;
import server.Channel;
import server.Server;

public class CommandWorker implements Runnable {
    CommandHeader commandHeader;

    public CommandWorker(CommandHeader header) {
        this.commandHeader = header;
    }

    public void run() {
        Channel channel = Server.getChannel(commandHeader.getChannelID());
        String command[] = commandHeader.getCommand().split(" ");
        if (command[0].equals("me")) {
            synchronized (channel.lock) {
                commandHeader.setMsgID(channel.getAndIncrementMsgID());
            }
            channel.addToTreeMap(commandHeader.getMsgID(),commandHeader);
            channel.sendPacket(commandHeader);

        } else if (command[0].equals("op")) {
            switch(command[1]) {
                case "kick":
                    channel.users.remove(command[2]);
                    synchronized (channel.lock) {
                        commandHeader.setMsgID(channel.getAndIncrementMsgID());
                    }
                    channel.addToTreeMap(commandHeader.getMsgID(),commandHeader);
                    channel.sendPacket(commandHeader);
                    break;

                case "mute":
                    channel.users.get(command[2]).setMuted(true);
                    synchronized (channel.lock) {
                        commandHeader.setMsgID(channel.getAndIncrementMsgID());
                    }
                    channel.addToTreeMap(commandHeader.getMsgID(),commandHeader);
                    channel.sendPacket(commandHeader);
                    break;

                case "unmute":
                    channel.users.get(command[2]).setMuted(false);
                    synchronized (channel.lock) {
                        commandHeader.setMsgID(channel.getAndIncrementMsgID());
                    }
                    channel.addToTreeMap(commandHeader.getMsgID(),commandHeader);
                    channel.sendPacket(commandHeader);
                    break;
            }
        } else {
            new Error("Incorrect header");
        }
    }
}
