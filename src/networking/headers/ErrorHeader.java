package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ErrorHeader implements Header {

  public static final byte ERROR_CONNECTION_CLOSED  = 0x00;
  public static final byte ERROR_INVALID_UNICODE    = 0x01;
  public static final byte ERROR_NO_SUCH_CHANNEL    = 0x02;

  private byte errorCode;
  private String errorMsg;

  public ErrorHeader(byte errorCode, String errorMsg) {  this.errorCode = errorCode; this.errorMsg = errorMsg; }
  ErrorHeader() { }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    out.writeByte(this.errorCode);
    out.writeShort(this.errorMsg.length());
    out.writeBytes(errorMsg);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.errorCode = in.readByte();
    final short errorMsgLen = in.readShort();
    final byte[] p = new byte[errorMsgLen];
    if (in.read(p) != errorMsgLen) throw new IOException("Prematurely encountered end of input stream.");
    this.errorMsg = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_ERROR; }
}
