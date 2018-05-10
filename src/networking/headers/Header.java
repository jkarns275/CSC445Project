package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *   An abstract class for all headers to inherit. Doesn't use java serialization as the serialization and
 *   deserialization of each header is hand-written.
 */
public abstract class Header {
  /**
   * Attempts to read an object in - if there are not enough bytes this will fail.
   * @param in
   * @throws IOException
   */
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
