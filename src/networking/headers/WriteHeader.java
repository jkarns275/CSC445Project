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

  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  public void readObject(ObjectInputStream in) throws IOException {

  }

  @Override
  public int opcode() { return Constants.OP_WRITE; }

  public String getMsg() {
    return msg;
  }

  public String getUsername() {
    return username;
  }

  public long getChannelID() {
    return channelID;
  }

  public long getMsgID() {
    return msgID;
  }
}
