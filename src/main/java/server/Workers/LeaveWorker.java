package server.Workers;

import javafx.scene.chart.PieChart;
import networking.headers.ErrorHeader;
import networking.headers.LeaveHeader;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class LeaveWorker implements Runnable {
    LeaveHeader leaveHeader;
    InetAddress address;
    Server server = Server.getInstance();
    int port;
    DatagramPacket packet;

    public LeaveWorker(LeaveHeader leaveHeader, DatagramPacket packet) {
        this.leaveHeader = leaveHeader;
        this.address = packet.getAddress();
        this.port = packet.getPort();
        this.packet = packet;
    }

    public void run() {
        server.channels.get(leaveHeader.channelID).users.remove(address);
        ErrorHeader header = new ErrorHeader((byte)0x00,"Connection closed");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(bos);
            header.writeObject(objectOutputStream);
            byte[] bytes = bos.toByteArray();
            DatagramPacket datagram = new DatagramPacket(bytes,bytes.length,address,port);
            server.send(datagram);
            server.sendMulticast(packet);
            //Need to add ip address and packet to ack queue
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
