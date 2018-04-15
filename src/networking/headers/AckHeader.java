package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class AckHeader implements Header {

  private Header body;

  public AckHeader(Header body) {
    this.body = body;
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

  public Header getBody() { return body; }
}
