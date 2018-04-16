package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HeartbeatHeader extends Header {

  private static final HeartbeatHeader instance = new HeartbeatHeader();

  HeartbeatHeader() { }

  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(Constants.OP_HEARTBEAT);
  }

  public void readObject(ObjectInputStream in) throws IOException { }

  @Override
  public int opcode() { return Constants.OP_HEARTBEAT; }

  public int hashCode() { return opcode(); }

  public boolean equals(Object o) { return o instanceof HeartbeatHeader; }

  public static HeartbeatHeader getInstance() { return instance; }
}
