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

  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  public void readObject(ObjectInputStream in) throws IOException {

  }

  public String getAssignedUsername() {
    return assignedUsername;
  }

  public String getChannelName() {
    return channelName;
  }

  public long getChannelID() {
    return channelID;
  }
}
