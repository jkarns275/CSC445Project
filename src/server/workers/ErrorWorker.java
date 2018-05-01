package server.workers;

import networking.PacketSender;
import networking.headers.ErrorHeader;
import server.Server;

import java.net.InetSocketAddress;

public class ErrorWorker implements Runnable {
    ErrorHeader header;
    InetSocketAddress address;

    public ErrorWorker(ErrorHeader header, InetSocketAddress address) {
        this.header = header;
        this.address = address;
    }

    public void run() {
        PacketSender packetSender = (PacketSender) Server.headerManager.packetSender(header, address);
        packetSender.run();
    }
}
