package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CommandHeader implements Header {
  private long channelID;
  private String command;

  public CommandHeader(long channelID, String command) {
    this.channelID = channelID; this.command = command;
  }

  CommandHeader() { }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_COMMAND);
    out.writeLong(channelID);
    out.writeShort(command.length());
    out.writeBytes(command);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();

    final int commandLen = (int) in.readByte();
    final byte[] p = new byte[commandLen];
    if (in.read(p) != commandLen) throw new IOException("Prematurely encountered end of input stream.");
    this.command = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_COMMAND; }

  public String getCommand() { return this.command; }
  public long getChannelID() { return this.channelID; }
}
