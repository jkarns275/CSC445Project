package server.workers;

import common.Constants;
import networking.headers.*;
import server.Server;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;

import static common.Constants.OP_ACK;

public class ConglomerateWorker implements Runnable {
    ConglomerateHeader congHeader;
    InetSocketAddress address;

    public ConglomerateWorker(ConglomerateHeader header, InetSocketAddress address) {
        this.congHeader = header;
        this.address = address;
    }

    public void run() {
        for (Header header : congHeader.getHeaders()) {
            try {
                switch (header.opcode()) {
                    case Constants.OP_WRITE:
                        new WriteWorker((WriteHeader) header, address).run();
                        break;

                    case Constants.OP_JOIN:
                        new JoinWorker((JoinHeader) header, address).run();
                        break;

                    case Constants.OP_LEAVE:
                        new LeaveWorker((LeaveHeader) header, address).run();
                        break;

                    case Constants.OP_NAK:
                        new NakWorker((NakHeader) header, address).run();
                        break;

                    case Constants.OP_ERROR:
                        new ErrorWorker((ErrorHeader) header, address).run();
                        break;

                    case Constants.OP_HEARTBEAT:
                        new HeartbeatWorker((HeartbeatHeader) header, address).run();
                        break;

                    case Constants.OP_COMMAND:
                        new CommandWorker((CommandHeader) header, address).run();
                        break;

                    case OP_ACK:
                        Server.headerManager.processAckHeader((AckHeader) header, address);
                        break;

                    default:
                        System.err.println("Received erroneous opcode, " + header.opcode() + ", from: " + address);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
