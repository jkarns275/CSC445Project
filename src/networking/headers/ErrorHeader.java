package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ErrorHeader implements Header {

  private byte errorCode;
  private String errorMsg;

  public ErrorHeader(byte errorCode, String errorMsg) {  this.errorCode = errorCode; this.errorMsg = errorMsg; }
  ErrorHeader() { }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {

  }

  @Override
  public int opcode() { return Constants.OP_ERROR; }
}
