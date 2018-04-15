package networking;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;

public class SocketSemaphore {

  private final Semaphore lock = new Semaphore(1, true);
  private final DatagramSocket socket;

  public SocketSemaphore(int port) throws IOException {
    socket = new DatagramSocket(port);
  }

  public SocketSemaphore(InetSocketAddress InetSocketAddress) throws IOException {
    socket = new DatagramSocket(InetSocketAddress);
  }

  public DatagramSocket acquire() throws InterruptedException {
    lock.acquire();
    return socket;
  }

  public void release() {
    lock.release();
  }
}
