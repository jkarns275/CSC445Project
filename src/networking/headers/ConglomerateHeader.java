package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import static common.Constants.*;

public class ConglomerateHeader extends Header {

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
    int size = (int) in.readByte();
    for (int i = 0; i < size; i += 1)
      headers.add(HeaderFactory.getInstance().readHeader(in));
  }

  @Override
  public int opcode() { return OP_CONG; }

  @Override
  public int hashCode() {
    int hashcode = 0;
    for (Header h : this.headers) {
      hashcode >>= 3; hashcode ^= h.hashCode();
    }
    return hashcode ^ this.opcode();
  }

  public LinkedList<Header> getHeaders() { return this.headers; }
  public int size() { return this.headers.size(); }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConglomerateHeader that = (ConglomerateHeader) o;
    return Objects.equals(headers, that.headers);
  }
}
