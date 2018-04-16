package networking.headers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Header {
  public Header() { throw new NotImplementedException(); }

  /**
   * Attempts to serialize this object. If any fields are null serialization will fail
   * @param out
   */
  abstract public void writeObject(ObjectOutputStream out) throws IOException;
  abstract public void readObject(ObjectInputStream in) throws IOException;
  abstract public int opcode();
  @Override abstract public int hashCode();
  @Override abstract public boolean equals(Object o);
}
