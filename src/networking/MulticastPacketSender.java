package networking;

import networking.headers.AckHeader;
import networking.headers.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

public class MulticastPacketSender implements SendJob {

  private final HashSet<InetSocketAddress> recipients;
  private final SocketManager socket;
  private final Header packet;
  private final boolean needsAck;

  private ArrayBlockingQueue<SendJob> doneQueue;

  MulticastPacketSender(boolean needsAck, Header packet, HashSet<InetSocketAddress> recipients, SocketManager socket,
                        ArrayBlockingQueue<SendJob> doneQueue) {
    this.needsAck = needsAck; this.recipients = recipients; this.socket = socket; this.packet = packet;
    this.doneQueue = doneQueue;
  }

  public void run() {
    try {
      for (InetSocketAddress to : recipients) {
        socket.send(packet, to);
      }
    } catch (InterruptedException e) {
      System.err.println("Failed to send packet:");
      e.printStackTrace();
    }
    try {
      this.doneQueue.put(this);
    } catch (InterruptedException e) {
      System.err.println("Failed to add to done queue.");
      e.printStackTrace();
    }
  }

  @Override
  public boolean needsAck() {
    return false;
  }

  @Override
  public AckJob getAckJob(ArrayBlockingQueue<InetSocketAddress> ackQueue, ArrayBlockingQueue<AckResult> resultQueue) {
   return new MulticastAckHandler(this.packet, this.recipients, ackQueue, resultQueue, this.socket);
  }

  @Override
  public AckHeader getAckHeader() { return new AckHeader(packet); }

  public boolean isNeedsAck() { return this.needsAck; }
}
