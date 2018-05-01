package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class ErrorHeader extends Header {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ErrorHeader that = (ErrorHeader) o;
    return errorCode == that.errorCode &&
      Objects.equals(errorMsg, that.errorMsg);
  }

  @Override
  public int hashCode() { return this.errorMsg.hashCode() ^ (this.opcode() >> errorCode); }

  public byte getErrorCode() { return errorCode; }

  public String getErrorMsg() { return errorMsg; }
}
