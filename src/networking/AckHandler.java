package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class AckHandler implements AckJob {

  public class AckHandlerResult implements AckResult {

    private final boolean receivedAck;
    /**
     * The packet that we were receiving acks for.
     */
    private final Header packet;

    private final InetSocketAddress address;

    private final SocketSemaphore socket;

    AckHandlerResult(boolean receivedAck, Header packet, InetSocketAddress address, SocketSemaphore socket) {
      this.receivedAck = receivedAck; this.packet = packet; this.address = address; this.socket = socket;
    }

    public Runnable resend() { return new PacketSender(true, this.packet, this.address, this.socket); }

    public boolean wasSuccessful() { return receivedAck; }
  }

  /**
   * Timeout time in seconds.
   */
  private final static int timeout = 4;

  // A set of addresses that the server is still waiting for acks from.
  private final InetSocketAddress waitingFor;
  private final SynchronousQueue<InetSocketAddress> ackQueue;
  private final SynchronousQueue<AckResult> resultQueue;
  private final Header packet;
  private final SocketSemaphore socket;

  AckHandler(Header packet,
                    InetSocketAddress address,
                    SynchronousQueue<InetSocketAddress> ackQueue,
                    SynchronousQueue<AckResult> resultQueue,
                    SocketSemaphore socket) {
    this.waitingFor = address; this.ackQueue = ackQueue; this.resultQueue = resultQueue;
    this.packet = packet; this.socket = socket;
  }

  public void run() {
    try {
      InetSocketAddress item = ackQueue.poll(timeout, TimeUnit.SECONDS);
      boolean isEqual =   item.getAddress().equals(this.waitingFor.getAddress())
                          && item.getPort() == this.waitingFor.getPort();
      resultQueue.put(new AckHandlerResult(true, packet, this.waitingFor, this.socket));
    } catch (InterruptedException te) {
      try {
        resultQueue.put(new AckHandlerResult(false, this.packet, this.waitingFor, this.socket));
      } catch (InterruptedException e) {
        System.err.println("Failed to push to result queue");
      }
    }
  }
}
