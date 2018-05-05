package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Header {
  abstract public void readObject(ObjectInputStream in) throws IOException;
  /**
   * Attempts to serialize this object. If any fields are null serialization will fail
   * @param out
   */
  abstract public void writeObject(ObjectOutputStream out) throws IOException;
  abstract public int opcode();
  @Override abstract public int hashCode();
  @Override abstract public boolean equals(Object o);
}
