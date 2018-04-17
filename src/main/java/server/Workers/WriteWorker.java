package server.Workers;

import networking.headers.WriteHeader;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class WriteWorker implements Runnable {
    WriteHeader writeHeader;
    InetAddress address;
    int port;

    public WriteWorker(WriteHeader writeHeader, InetAddress address, int port) {
        this.writeHeader = writeHeader;
        this.address = address;
        this.port = port;
    }

    public void run() {
//        DatagramPacket packet = new DatagramPacket();
        writeHeader.getMsg();

    }
}
