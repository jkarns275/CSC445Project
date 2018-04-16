package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;

public class SocketJob {

  private final Header header;
  private final InetSocketAddress address;

  SocketJob(Header header, InetSocketAddress address) { this.header = header; this.address = address; }

  public Header getHeader() {
    return header;
  }

  public InetSocketAddress getAddress() {
    return address;
  }
}
