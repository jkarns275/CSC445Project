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
        Channel.BufferedMessageEntry bufferedMessageEntry;
        long upperMsg = header.getUpperMsgID();
        long lowerMsg = header.getLowerMsgID();
        for (Long i = lowerMsg; ((upperMsg - i) > 0); i++) {
            if ((bufferedMessageEntry = channel.getFromBufferedTreeMap(i)) != null) {
                Header bufferedHeader = bufferedMessageEntry.getHeader();
                channel.sendPacket(bufferedHeader, address);
            } else {
                ErrorHeader errorHeader = new ErrorHeader((byte) 0x04, "Message, " + i + ", no longer buffered");
                channel.sendPacket(errorHeader, address);
            }
        }
    }
}
