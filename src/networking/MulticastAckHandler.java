package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MulticastAckHandler implements AckJob {

  public class MultiAckHandlerResult implements AckResult {
    /**
     * Socket addresses for which there were no acks received
     */
    private final HashSet<InetSocketAddress> missingAcks;
    /**
     * The packet that we were receiving acks for.
     */
    private final Header packet;

    private final SocketManager socket;

    MultiAckHandlerResult(HashSet<InetSocketAddress> missingAcks, Header packet, SocketManager socket) {
      this.missingAcks = missingAcks; this.packet = packet; this.socket = socket;
    }

    public SendJob resend(ArrayBlockingQueue<SendJob> doneQueue) {
      return (SendJob) new MulticastPacketSender(true, this.packet, this.missingAcks, this.socket, doneQueue);
    }

    @Override
    public boolean wasSuccessful() { return missingAcks.isEmpty(); }
  }

  /**
   * Timeout time in seconds.
   */
  private final static int timeout = 2;

  // A set of addresses that the server is still waiting for acks from.
  private final HashSet<InetSocketAddress> waitingForAck = new HashSet<InetSocketAddress>();
  private final ArrayBlockingQueue<InetSocketAddress> ackQueue;
  private final ArrayBlockingQueue<AckResult> resultQueue;
  private final Header packet;
  private final SocketManager socket;

  MulticastAckHandler(Header packet,
                             Collection<InetSocketAddress> addresses,
                             ArrayBlockingQueue<InetSocketAddress> ackQueue,
                             ArrayBlockingQueue<AckResult> resultQueue,
                             SocketManager socket) {
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
