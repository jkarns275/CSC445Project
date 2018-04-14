package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HeartbeatHeader implements Header {

  private static final HeartbeatHeader instance = new HeartbeatHeader();

  HeartbeatHeader() { }

  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_HEARTBEAT);
  }

  public void readObject(ObjectInputStream in) throws IOException {
    if ((int) in.readByte() != Constants.OP_HEARTBEAT) throw new IOException("Wrong opcode for heartbeat header.");
  }

  public static HeartbeatHeader getInstance() { return instance; }
}
