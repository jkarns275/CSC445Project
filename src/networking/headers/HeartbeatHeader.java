package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HeartbeatHeader extends Header {

  private long channelID;

  HeartbeatHeader() { }

  public HeartbeatHeader(long channelID) { this.channelID = channelID; }
  public HeartbeatHeader(HeartbeatHeader h) { this.channelID = h.channelID; }
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_HEARTBEAT);
    out.writeLong(channelID);
  }

  public void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();
  }

  @Override
  public int opcode() { return Constants.OP_HEARTBEAT; }

  public int hashCode() { return Long.hashCode(this.channelID); }

  public boolean equals(Object other) {
    if (other instanceof HeartbeatHeader) {
      HeartbeatHeader otherHeartbeat = (HeartbeatHeader) other;
      return otherHeartbeat.channelID == this.channelID;
    }
    return false;
  }

  public long getChannelID() { return channelID; }
  public void setChannelID(long channelID) { this.channelID = channelID; }


}
