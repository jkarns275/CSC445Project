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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class PacketSender implements SendJob {
  private final InetSocketAddress to;
  private final SocketManager socket;
  private final Header packet;
  private boolean needsAck;
  private AckHeader ackHeader = null;

  public PacketSender(boolean needsAck, Header packet, InetSocketAddress to, SocketManager socket) {
    this.needsAck = needsAck; this.to = to; this.socket = socket; this.packet = packet;
  }

  public void run() {
    try {
      // Serialize the data
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bout);
      packet.writeObject(out);
      ByteBuffer data = ByteBuffer.wrap(bout.toByteArray());
      byte[] arr = data.array();

      socket.send(packet, to);

    } catch (IOException e) {
      System.err.println("Failed to create ObjectOutputStream:");
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.err.println("Failed to acquire socket semaphore:");
      e.printStackTrace();
    }
  }

  @Override
  public boolean needsAck() { return this.needsAck; }

  @Override
  public void setNeedsAck(boolean needsAck) { this.needsAck = needsAck; }

  @Override
  public AckJob getAckJob(ArrayBlockingQueue<InetSocketAddress> ackQueue, LinkedBlockingQueue<AckResult> resultQueue) {
    return new AckHandler(this.packet, this.to, ackQueue, resultQueue, socket);
  }

  @Override
  public AckHeader getAckHeader() {
    if (this.ackHeader == null)
      return this.ackHeader = new AckHeader(packet);
    else
      return this.ackHeader;
  }

  @Override
  public int numClients() { return 1; }
}
