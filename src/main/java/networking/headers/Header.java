package networking.headers;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public interface Header {
  /**
   * Attempts to serialize this object. If any fields are null serialization will fail
   * @param out
   */
  void writeObject(ObjectOutputStream out) throws IOException;
  void readObject(ObjectInputStream in) throws IOException;
}