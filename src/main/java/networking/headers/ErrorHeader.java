package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ErrorHeader implements Header {

  private byte errorCode;
  private String errorMsg;

  public ErrorHeader(byte errorCode, String errorMsg) {  this.errorCode = errorCode; this.errorMsg = errorMsg; }
  ErrorHeader() { }

  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  public void readObject(ObjectInputStream in) throws IOException {

  }
}
