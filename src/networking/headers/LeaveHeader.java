package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LeaveHeader extends Header {

  private long channelID;

  public LeaveHeader(long channelID) { this.channelID = channelID; }
  LeaveHeader() {}

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    out.writeLong(channelID);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();
  }

  @Override
  public int opcode() { return Constants.OP_LEAVE; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LeaveHeader that = (LeaveHeader) o;
    return channelID == that.channelID;
  }

  @Override
  public int hashCode() { return Long.hashCode(this.channelID); }

  public long getChannelID() { return channelID; }
}
