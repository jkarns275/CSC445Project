package server.workers;

import networking.headers.ErrorHeader;
import networking.headers.Header;
import networking.headers.NakHeader;
import server.Channel;
import server.Server;

import java.net.InetSocketAddress;

public class NakWorker implements Runnable {
    NakHeader header;
    InetSocketAddress address;

    public NakWorker(NakHeader header, InetSocketAddress address) {
        this.header = header;
        this.address = address;
    }

    public void run() {
        Channel channel = Server.getChannel(header.getChannelID());
        long upperMsg = header.getUpperMsgID();
        long lowerMsg = header.getLowerMsgID();
        for (long i = lowerMsg; ((upperMsg - i) > 0); i++) {
            Header bufferedHeader = channel.getFromTreeMap(i);
            if (bufferedHeader != null) {
                channel.sendPacket(bufferedHeader,address);
            } else {
                ErrorHeader errorHeader = new ErrorHeader((byte)0x04, "Message no longer buffered");
                channel.sendPacket(errorHeader,address);
            }
        }
    }
}
