package networking;

import networking.headers.Header;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AckHandler implements AckJob {

  public class AckHandlerResult implements AckResult {

    private final boolean receivedAck;
    /**
     * The packet that we were receiving acks for.
     */
    private final Header packet;

    private final InetSocketAddress address;

    private final SocketManager socket;

    AckHandlerResult(boolean receivedAck, Header packet, InetSocketAddress address, SocketManager socket) {
      this.receivedAck = receivedAck; this.packet = packet; this.address = address; this.socket = socket;
    }

    public SendJob resend(ArrayBlockingQueue<SendJob> doneQueue) {
      return new PacketSender(true, this.packet, this.address, this.socket, doneQueue);
    }

    public boolean wasSuccessful() { return receivedAck; }
  }

  /**
   * Timeout time in seconds.
   */
  private final static int timeout = 4;

  // A set of addresses that the server is still waiting for acks from.
  private final InetSocketAddress waitingFor;
  private final ArrayBlockingQueue<InetSocketAddress> ackQueue;
  private final ArrayBlockingQueue<AckResult> resultQueue;
  private final Header packet;
  private final SocketManager socket;

  AckHandler(Header packet,
                    InetSocketAddress address,
                    ArrayBlockingQueue<InetSocketAddress> ackQueue,
                    ArrayBlockingQueue<AckResult> resultQueue,
                    SocketManager socket) {
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
