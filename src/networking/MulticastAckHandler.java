package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class MulticastAckHandler implements Runnable {

  public class MultiAckHandlerResult implements AckResult {
    /**
     * Socket addresses for which there were no acks received
     */
    private final HashSet<InetSocketAddress> missingAcks;
    /**
     * The packet that we were receiving acks for.
     */
    private final Header packet;

    private final SocketSemaphore socket;

    MultiAckHandlerResult(HashSet<InetSocketAddress> missingAcks, Header packet, SocketSemaphore socket) {
      this.missingAcks = missingAcks; this.packet = packet; this.socket = socket;
    }

    public Runnable resend() { return new MulticastPacketSender(this.packet, this.missingAcks, this.socket); }

    public boolean wasSuccessful() { return missingAcks.isEmpty(); }
  }

  /**
   * Timeout time in seconds.
   */
  private final static int timeout = 2;

  // A set of addresses that the server is still waiting for acks from.
  private final HashSet<InetSocketAddress> waitingForAck = new HashSet<InetSocketAddress>();
  private final SynchronousQueue<InetSocketAddress> ackQueue;
  private final SynchronousQueue<MultiAckHandlerResult> resultQueue;
  private final Header packet;
  private final SocketSemaphore socket;

  public MulticastAckHandler(Header packet,
                             Collection<InetSocketAddress> addresses,
                             SynchronousQueue<InetSocketAddress> ackQueue,
                             SynchronousQueue<MultiAckHandlerResult> resultQueue,
                             SocketSemaphore socket) {
    this.waitingForAck.addAll(addresses); this.ackQueue = ackQueue; this.resultQueue = resultQueue;
    this.packet = packet; this.socket = socket;
  }

  public void run() {
    try {
      for (;;) {
        InetSocketAddress item = this.ackQueue.poll(MulticastAckHandler.timeout, TimeUnit.SECONDS);
        this.waitingForAck.remove(item);
        if (this.waitingForAck.isEmpty()) {
          resultQueue.put(new MultiAckHandlerResult(this.waitingForAck, this.packet, this.socket));
          return;
        }
      }
    } catch (InterruptedException te) {
      try { resultQueue.put(new MultiAckHandlerResult(this.waitingForAck, this.packet, this.socket)); }
      catch (InterruptedException e) {
        System.err.println("Failed to push to result queue");
      }
    }
  }
}