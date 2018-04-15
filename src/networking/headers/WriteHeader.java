package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WriteHeader implements Header {

  private long channelID;
  private long msgID;
  private String msg      = "ERROR";
  private String username = "ERROR";

  public WriteHeader(long channelID, long msgID, String msg, String username) {
    this.channelID = channelID; this.msgID = msgID; this.msg = msg; this.username = username;
  }

  WriteHeader() { }

  @Override
  public final void writeObject(ObjectOutputStream out) throws IOException {
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
    if (in.read(p) != msgLen) throw new IOException("Prematurely encountered end of input stream.");
    this.msg = new String(p);

    int usernameLen = (int) in.readByte();
    p = new byte[msgLen];
    if (in.read(p) != usernameLen) throw new IOException("Prematurely encountered end of input stream.");
    this.username = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_WRITE; }

  public String getMsg() { return this.msg; }

  public String getUsername() { return this.username; }

  public long getChannelID() { return this.channelID; }

  public long getMsgID() { return msgID; }
}
