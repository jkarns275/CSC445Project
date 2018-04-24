package server.workers;

import networking.headers.ErrorHeader;

import java.net.InetSocketAddress;

public class ErrorWorker implements Runnable {
    ErrorHeader header;
    InetSocketAddress address;

    public ErrorWorker(ErrorHeader header, InetSocketAddress address) {
        this.header = header;
        this.address = address;
    }

    public void run() {

    }
}
