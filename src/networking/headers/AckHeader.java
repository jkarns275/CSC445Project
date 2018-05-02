package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class AckHeader extends Header {

  private Header body;

  public AckHeader(Header body) {
    this.body = body;
  }

  AckHeader() {

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
  public boolean equals(Object o) {
    return o instanceof AckHeader && ((AckHeader) o).body.equals(this.body);
  }

  @Override
  public int opcode() { return Constants.OP_ACK; }

  @Override
  public int hashCode() { return this.opcode() ^ body.opcode() ^ body.hashCode(); }

  public Header getBody() { return body; }
}
