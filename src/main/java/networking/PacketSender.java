package networking;

import networking.headers.Header;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class PacketSender implements Runnable {
  private final InetSocketAddress to;
  private final SocketSemaphore socket;
  private final Header packet;
  public PacketSender(Header packet, InetSocketAddress to, SocketSemaphore socket) {
    this.to = to;
    this.socket = socket;
    this.packet = packet;

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
}
