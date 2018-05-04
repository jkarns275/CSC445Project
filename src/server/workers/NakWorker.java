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
        Channel.BufferedMessageEntry entry = null;
        for (long i = lowerMsg; ((upperMsg - i) > 0); i++) {
          Header bufferedHeader = null;
          if (channel != null && (entry = channel.getMessage(i)) != null) {
            bufferedHeader = entry.getHeader();
            if (bufferedHeader != null) {
                channel.sendPacket(bufferedHeader,address);
            }
          } else {
            ErrorHeader errorHeader = new ErrorHeader((byte)0x04, "Message no longer buffered");
            if (channel != null) channel.sendPacket(errorHeader,address);
          }
        }
    }
}
