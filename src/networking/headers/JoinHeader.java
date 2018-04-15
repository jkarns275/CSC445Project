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
  public final void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    out.writeByte(desiredUsername.length());
    out.writeBytes(desiredUsername);
    out.writeByte(channelName.length());
    out.writeBytes(channelName);
  }

  @Override
  public final void readObject(ObjectInputStream in) throws IOException {
    final int desiredUsernameLen = (int) in.readByte();
    byte[] p = new byte[desiredUsernameLen];
    if (in.read(p) != desiredUsernameLen) throw new IOException("Prematurely encountered end of input stream.");
    this.desiredUsername = new String(p);

    final int channelNameLen = (int) in.readByte();
    p = new byte[channelNameLen];
    if (in.read(p) != channelNameLen) throw new IOException("Prematurely encountered end of input stream.");
    this.channelName = new String(p);
  }

  @Override
  public final int opcode() { return Constants.OP_JOIN; }

  public String getDesiredUsername() {
    return desiredUsername;
  }

  public String getChannelName() {
    return channelName;
  }
}
