package server.workers;

import networking.headers.HeartbeatHeader;
import server.Server;

import java.net.InetSocketAddress;

public class HeartbeatWorker implements Runnable {
    HeartbeatHeader header;
    InetSocketAddress address;

    public HeartbeatWorker(HeartbeatHeader header, InetSocketAddress address) {
        this.header = header;
        this.address = address;
    }

    public void run() {
        System.out.println(header.getChannelID());
        Server.heartbeatManager.processHeartbeat(header.getChannelID(), address);
    }
}
