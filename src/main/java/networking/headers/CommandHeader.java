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

  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_COMMAND);
    out.writeLong(channelID);
    out.writeShort(command.length());
    out.writeBytes(command);
  }

  public void readObject(ObjectInputStream in) throws IOException {
    // TODO: this
  }
}
