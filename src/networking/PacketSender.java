package networking;

import networking.headers.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.SynchronousQueue;

public class PacketSender implements SendJob {
  private final InetSocketAddress to;
  private final SocketSemaphore socket;
  private final Header packet;
  private final boolean needsAck;

  public PacketSender(boolean needsAck, Header packet, InetSocketAddress to, SocketSemaphore socket) {
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

      { // Actually send the data
        DatagramSocket sock = this.socket.acquire();
        sock.send(new DatagramPacket(arr, arr.length, to));
        this.socket.release();
      }
    } catch (IOException e) {
      System.err.println("Failed to create ObjectOutputStream:");
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.err.println("Failed to acquire socket semaphore:");
      e.printStackTrace();
    }
  }

  public boolean isNeedsAck() { return needsAck; }

  @Override
  public boolean needsAck() { return this.needsAck; }

  @Override
  public AckJob getAckJob(SynchronousQueue<InetSocketAddress> ackQueue, SynchronousQueue<AckResult> resultQueue) {
    return new AckHandler(this.packet, this.to, ackQueue, resultQueue, socket);
  }
}
