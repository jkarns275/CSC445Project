package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SourceHeader implements Header {

  private long channelID;
  private String channelName      = "ERROR";
  private String assignedUsername = "ERROR";

  public SourceHeader(long channelID, String channelName, String assignedUsername) {
    this.channelID = channelID; this.channelName = channelName; this.assignedUsername = assignedUsername;
  }

  SourceHeader() { }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    out.writeLong(this.channelID);
    out.writeByte(this.channelName.length());
    out.writeBytes(this.channelName);
    out.writeByte(this.assignedUsername.length());
    out.writeBytes(this.assignedUsername);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();

    final int channelNameLen = (int) in.readByte();
    byte[] p = new byte[channelNameLen];
    if (in.read(p) != channelNameLen) throw new IOException("Prematurely encountered end of input stream.");
    this.channelName = new String(p);

    final int assignedUsernameLen = (int) in.readByte();
    p = new byte[assignedUsernameLen];
    if (in.read(p) != assignedUsernameLen) throw new IOException("Prematurely encountered end of input stream.");
    this.assignedUsername = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_SOURCE; }

  public String getAssignedUsername() { return assignedUsername; }

  public String getChannelName() { return channelName; }

  public long getChannelID() { return channelID; }
}
