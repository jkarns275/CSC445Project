package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JoinHeader implements Header {

  private String desiredUsername =  "ERROR";
  private String channelName =      "ERROR";

  public JoinHeader(String desiredUsername, String channelName) {
    this.desiredUsername = desiredUsername; this.channelName = channelName;
  }

  JoinHeader() { }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {

  }

  @Override
  public int opcode() { return Constants.OP_JOIN; }

  public String getDesiredUsername() {
    return desiredUsername;
  }

  public String getChannelName() {
    return channelName;
  }
}
