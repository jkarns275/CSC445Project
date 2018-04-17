package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;

public class SocketRequest {

  private final Header header;
  private final InetSocketAddress address;

  SocketRequest(Header header, InetSocketAddress address) { this.header = header; this.address = address; }

  public Header getHeader() {
    return header;
  }

  public InetSocketAddress getAddress() {
    return address;
  }
}
