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

  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  public void readObject(ObjectInputStream in) throws IOException {

  }

  public String getDesiredUsername() {
    return desiredUsername;
  }

  public String getChannelName() {
    return channelName;
  }
}
