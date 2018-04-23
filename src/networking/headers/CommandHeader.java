package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class CommandHeader extends Header {
  private long channelID;
  private long msgID;
  private String command;

  public CommandHeader(long channelID, String command) {
    this.channelID = channelID; this.command = command;
  }

  CommandHeader() { }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_COMMAND);
    out.writeLong(channelID);
    out.writeLong(msgID);
    out.writeShort(command.length());
    out.writeBytes(command);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();
    this.msgID = in.readLong();
    final int commandLen = (int) in.readByte();
    final byte[] p = new byte[commandLen];
    if (in.read(p) != commandLen) throw new IOException("Prematurely encountered end of input stream.");
    this.command = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_COMMAND; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommandHeader that = (CommandHeader) o;
    return channelID == that.channelID &&
      Objects.equals(command, that.command);
  }

  @Override
  public int hashCode() { return ((this.opcode() ^ this.command.hashCode()) >> 16) ^ Long.hashCode(channelID); }

  public String getCommand() { return this.command; }
  public long getChannelID() { return this.channelID; }
  public void setMsgID(long msgID) { this.msgID = msgID; }
  public long getMsgID() { return this.msgID; }
}
