package networking;

import networking.headers.Header;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class MulticastAckHandler implements Runnable {

  public class MultiAckHandlerResult {
    /**
     * Socket addresses for which there were no acks received
     */
    public final HashSet<SocketAddress> missingAcks;
    /**
     * The packet that we were receiving acks for.
     */
    public final Header packet;

    MultiAckHandlerResult(HashSet<SocketAddress> missingAcks, Header packet) {
      this.missingAcks = missingAcks; this.packet = packet;
    }
  }

  /**
   * Timeout time in seconds.
   */
  private final static int timeout = 2;

  // A set of addresses that the server is still waiting for acks from.
  private final HashSet<SocketAddress> waitingForAck = new HashSet<SocketAddress>();
  private final SynchronousQueue<SocketAddress> ackQueue;
  private final SynchronousQueue<MultiAckHandlerResult> resultQueue;
  private final Header packet;

  public MulticastAckHandler(Header packet,
                             Collection<SocketAddress> addresses,
                             SynchronousQueue<SocketAddress> ackQueue,
                             SynchronousQueue<MultiAckHandlerResult> resultQueue) {
    waitingForAck.addAll(addresses); this.ackQueue = ackQueue; this.resultQueue = resultQueue; this.packet = packet;
  }

  public void run() {
    try {
      for (;;) {
        SocketAddress item = ackQueue.poll(timeout, TimeUnit.SECONDS);
        waitingForAck.remove(item);
      }
    } catch (InterruptedException te) {
      try { resultQueue.put(new MultiAckHandlerResult(waitingForAck, packet)); }
      catch (InterruptedException e) {
        System.err.println("Failed to push to result queue");
      }
    }
  }
}
