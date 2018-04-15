package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LeaveHeader implements Header {

  LeaveHeader() {}

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {

  }

  @Override
  public int opcode() { return Constants.OP_LEAVE; }
}
