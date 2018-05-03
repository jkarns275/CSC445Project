package server.workers;

import networking.PacketSender;
import networking.headers.ErrorHeader;
import networking.headers.WriteHeader;
import server.Channel;
import server.Server;

import java.net.InetSocketAddress;

public class WriteWorker implements Runnable {
    WriteHeader writeHeader;
    InetSocketAddress address;

    public WriteWorker(WriteHeader writeHeader, InetSocketAddress address) {
        this.writeHeader = writeHeader;
        this.address = address;
    }

    public void run() {
        Channel channel = Server.getChannel(writeHeader.getChannelID());
        if (channel != null) {
            if (channel.users.get(writeHeader.getUsername()).getMuted()) return;
            writeHeader.setMsgID(channel.getAndIncrementMsgID());
            channel.addToTreeMap(writeHeader.getMsgID(), writeHeader);
            System.out.println(writeHeader.getMsg());
            channel.sendPacket(writeHeader);
        } else {
            ErrorHeader header = new ErrorHeader((byte)0x02,"No such channel exists");
            PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(header,address);
            packetSender.run();
        }
    }
}
