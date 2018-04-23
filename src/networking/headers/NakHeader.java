package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class NakHeader extends Header {
  private long lowerMsgID;
  private long upperMsgID;
  private long channelID;

  public NakHeader(long lowerMsgID, long upperMsgID, long channelID) {
    this.lowerMsgID = lowerMsgID; this.upperMsgID = upperMsgID; this.channelID = channelID;
  }

  NakHeader() {}

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_NAK);
    out.writeLong(lowerMsgID);
    out.writeLong(upperMsgID);
    out.writeLong(channelID);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NakHeader nakHeader = (NakHeader) o;
    return lowerMsgID == nakHeader.lowerMsgID &&
      upperMsgID == nakHeader.upperMsgID &&
      channelID == nakHeader.channelID;
  }

  @Override
  public int hashCode() {

    return Objects.hash(lowerMsgID, upperMsgID, channelID);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.lowerMsgID = in.readLong();
    this.upperMsgID = in.readLong();
    this.channelID = in.readLong();
  }

  @Override
  public int opcode() { return Constants.OP_NAK; }

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
