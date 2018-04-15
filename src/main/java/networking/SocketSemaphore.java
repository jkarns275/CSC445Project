package networking;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.concurrent.Semaphore;

public class SocketSemaphore {

  private final Semaphore lock = new Semaphore(1, true);
  private final DatagramSocket socket;

  public SocketSemaphore(int port) throws IOException {
    socket = new DatagramSocket(port);
  }

  public SocketSemaphore(SocketAddress socketAddress) throws IOException {
    socket = new DatagramSocket(socketAddress);
  }

  public DatagramSocket acquire() throws InterruptedException {
    lock.acquire();
    return socket;
  }

  public void release() {
    lock.release();
  }
}
