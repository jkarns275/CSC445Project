package networking;

import networking.headers.AckHeader;
import common.Constants;
import networking.headers.Header;
import networking.headers.HeaderFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketManager {
  // All opcodes will be considered 'Ackable' for now.
  //private static HashSet<Integer> ACKABLE_OPCODES = new HashSet<Integer>();

  public static SocketRequest job(Header header, InetSocketAddress to) { return new SocketRequest(header, to); }

  private final DatagramSocket socket;
  private final LinkedBlockingQueue<SocketRequest> receivedItems = new LinkedBlockingQueue<>(1024);
  private final LinkedBlockingQueue<SocketRequest> toSend = new LinkedBlockingQueue<>(1024);

  AtomicBoolean lastSendWasSuccessfull = new AtomicBoolean(true);

  public SocketManager(int port, ThreadPoolExecutor pool) throws IOException {
    socket = new DatagramSocket(port);
    this.begin(pool, socket);
  }

  public SocketManager(InetSocketAddress address, ThreadPoolExecutor pool) throws IOException {
    socket = new DatagramSocket(address);
    this.begin(pool, socket);
  }

  private void begin(ThreadPoolExecutor pool, DatagramSocket socket) {
    pool.execute(() -> {
      final byte[] buffer = new byte[Constants.MAX_HEADER_SIZE];

      try {
        socket.setSoTimeout(1);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      for (;;) {
        try {
          final ByteArrayOutputStream bout = new ByteArrayOutputStream(Constants.MAX_HEADER_SIZE);
          final ObjectOutputStream out = new ObjectOutputStream(bout);
          for (int i = 0; i < buffer.length; i++) buffer[i] = 0;
          final SocketRequest job = toSend.poll(0, TimeUnit.MILLISECONDS);

          if (job != null) {
            if (job instanceof KillRequest) return;
            job.getHeader().writeObject(out);
            out.close();

            final DatagramPacket toSend = new DatagramPacket(bout.toByteArray(), bout.size());
            toSend.setSocketAddress(job.getAddress());
            socket.send(toSend);
            this.lastSendWasSuccessfull.set(true);
          }
        } catch (InterruptedException | SocketTimeoutException ignored) {
          this.lastSendWasSuccessfull.set(false);
        } catch (Exception e) {
          System.err.println("Encountered error in SocketManager:");
          e.printStackTrace();
        }
        try {
          for (int i = 0; i < buffer.length; i++) buffer[i] = 0;
          final DatagramPacket received = new DatagramPacket(buffer, buffer.length);
          socket.receive(received);
          final ByteArrayInputStream bin = new ByteArrayInputStream(received.getData(), received.getOffset(), received
            .getLength());
          final ObjectInputStream in = new ObjectInputStream(bin);
          final Header header = HeaderFactory.getInstance().readHeader(in);
          final InetSocketAddress src = new InetSocketAddress(received.getAddress(), received.getPort());
          receivedItems.put(job(header, src));
          System.out.println("Received: " + header);
//          System.out.println("Received header with opcode " + header.opcode() + " from host " + src);
          if (header.opcode() != Constants.OP_ACK && header.opcode() != Constants.OP_HEARTBEAT) {
//            System.out.println("Sending ACK");
            toSend.put(job(new AckHeader(header), src));
          }
        } catch (InterruptedException | SocketTimeoutException ignored) {
        } catch (Exception e) {
          System.err.println("Encountered error in SocketManager:");
          e.printStackTrace();
        }
      }
    });
  }

  public void send(Header header, InetSocketAddress to) throws InterruptedException {
    if (this.toSend.offer(SocketManager.job(header, to))) return;
    throw new InterruptedException();
  }

  public void send(SocketRequest sr) throws InterruptedException {
    if (this.toSend.offer(sr)) return;
    throw new InterruptedException();
  }

  public SocketRequest recv() throws InterruptedException {
    return this.receivedItems.poll(100, TimeUnit.MILLISECONDS);
  }

  public boolean probablyConnected() {
    return this.lastSendWasSuccessfull.get();
  }
}
