package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;

/**
 * A type of socket request that just tells the SocketManager to close the worker thread.
 */
class KillRequest extends SocketRequest {
  KillRequest() {
    super(null, null);
  }
}
