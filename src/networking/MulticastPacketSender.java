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
import java.util.concurrent.LinkedBlockingQueue;

public class MulticastPacketSender implements SendJob {

  private final HashSet<InetSocketAddress> recipients;
  private final SocketManager socket;
  private final Header packet;
  private boolean needsAck;

  private LinkedBlockingQueue<SendJob> doneQueue;

  MulticastPacketSender(boolean needsAck, Header packet, HashSet<InetSocketAddress> recipients, SocketManager socket,
                        LinkedBlockingQueue<SendJob> doneQueue) {
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
  public void setNeedsAck(boolean needsAck) { this.needsAck = needsAck; }

  @Override
  public AckJob getAckJob(ArrayBlockingQueue<InetSocketAddress> ackQueue, LinkedBlockingQueue<AckResult> resultQueue) {
   return new MulticastAckHandler(this.packet, this.recipients, ackQueue, resultQueue, this.socket);
  }

  @Override
  public AckHeader getAckHeader() { return new AckHeader(packet); }

  @Override
  public int numClients() { return recipients.size(); }

  public boolean isNeedsAck() { return this.needsAck; }
}
