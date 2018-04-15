package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import static networking.headers.Constants.*;

public class ConglomerateHeader implements Header {

  private final LinkedList<Header> headers = new LinkedList<Header>();

  public ConglomerateHeader(Collection<Header> headers) {
    headers.addAll(headers);
  }

  ConglomerateHeader() {}

  public void addHeader(Header h) { headers.add(h); }

  protected void cloneFrom(ConglomerateHeader other) {
    this.headers.clear();
    this.headers.addAll(other.headers);
  }

  public final void writeObject(ObjectOutputStream out) throws IOException {
    int length = headers.size();
    if (length > Constants.MAX_CONG_PACKETS)
      throw new IOException("Cannot put more than " + Constants.MAX_CONG_PACKETS + " into a ConglomerateHeader");

    out.write((byte) Constants.OP_CONG);
    out.write((byte) length);
    for (Header h : headers) {
      h.writeObject(out);
    }
  }

  public final void readObject(ObjectInputStream in) throws IOException {
    in.mark(MAX_HEADER_SIZE);
    int opcode = (int) in.readByte();
    if (opcode != OP_CONG) {
      throw new IOException("Wrong opcode for ConglomerateHeader.");
    }

    int size = (int) in.readByte();
    for (int i = 0; i < size; i += 1)
      headers.add(HeaderFactory.getInstance().readHeader(in));
  }

  @Override
  public int opcode() { return OP_CONG; }
}
