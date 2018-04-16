package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class AckHeader extends Header {

  private Header body;

  public AckHeader(Header body) {
    this.body = body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AckHeader ackHeader = (AckHeader) o;
    return Objects.equals(body, ackHeader.body);
  }

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    body.writeObject(out);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.body = HeaderFactory.getInstance().readHeader(in);
  }

  @Override
  public int opcode() { return Constants.OP_ACK; }

  @Override
  public int hashCode() { return this.opcode() ^ body.opcode(); }

  public Header getBody() { return body; }
}
