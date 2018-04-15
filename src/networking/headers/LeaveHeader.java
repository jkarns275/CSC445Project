package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LeaveHeader implements Header {

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

  public long getChannelID() { return channelID; }
}
