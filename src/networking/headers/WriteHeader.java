package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class WriteHeader extends Header {

  private long channelID;
  private long msgID;
  private String msg      = "ERROR";
  private String username = "ERROR";

  public WriteHeader(long channelID, long msgID, String msg, String username) {
    this.channelID = channelID; this.msgID = msgID; this.msg = msg; this.username = username;
  }

  public WriteHeader() { }

  @Override
  public final void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    out.writeLong(this.channelID);
    out.writeLong(this.msgID);
    out.writeChar((char) this.msg.length());
    out.writeBytes(this.msg);
    out.writeByte(this.username.length());
    out.writeBytes(this.username);
  }

  @Override
  public final void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();
    this.msgID = in.readLong();

    int msgLen = (int) in.readChar();
    byte[] p = new byte[msgLen];
    int result = in.read(p);
    if (result != msgLen) throw new IOException("Prematurely encountered end of input stream.");
    this.msg = new String(p);

    int usernameLen = (int) in.readByte();
    p = new byte[msgLen];
    if (in.read(p) != usernameLen) throw new IOException("Prematurely encountered end of input stream.");
    this.username = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_WRITE; }

  @Override
  public int hashCode() {
    return Long.hashCode(this.channelID) ^ Long.hashCode(this.msgID) ^ this.msg.hashCode() ^ this.username.hashCode();
  }

  public String getMsg() { return this.msg; }

  public String getUsername() { return this.username; }

  public long getChannelID() { return this.channelID; }

  public long getMsgID() { return msgID; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WriteHeader that = (WriteHeader) o;
    return channelID == that.channelID &&
      msgID == that.msgID &&
      Objects.equals(msg, that.msg) &&
      Objects.equals(username, that.username);
  }
}
