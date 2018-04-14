package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class NakHeader implements Header {
  private long lowerMsgID;
  private long upperMsgID;
  private long channelID;

  public NakHeader(long lowerMsgID, long upperMsgID, long channelID) {
    this.lowerMsgID = lowerMsgID; this.upperMsgID = upperMsgID; this.channelID = channelID;
  }

  NakHeader() {}

  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_NAK);
    out.writeLong(lowerMsgID);
    out.writeLong(upperMsgID);
    out.writeLong(channelID);
  }

  public void readObject(ObjectInputStream in) throws IOException {
    this.lowerMsgID = in.readLong();
    this.upperMsgID = in.readLong();
    this.channelID = in.readLong();
  }

  public long getChannelID() {
    return channelID;
  }

  public long getUpperMsgID() {
    return upperMsgID;
  }

  public long getLowerMsgID() {
    return lowerMsgID;
  }
}
