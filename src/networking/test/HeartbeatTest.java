package networking.test;

import networking.HeaderIOManager;
import networking.HeartbeatManager;
import networking.HeartbeatSender;
import networking.SocketRequest;
import networking.headers.Constants;
import networking.headers.HeartbeatHeader;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HeartbeatTest {
  public static void main(String[] args) throws InterruptedException {
    ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>());

    poolExecutor.execute(() -> {
      long beginning = System.nanoTime();
      long channelID = 0;
      try {
        InetSocketAddress local = new InetSocketAddress(InetAddress.getLocalHost(), 4444);
        InetSocketAddress other = new InetSocketAddress(InetAddress.getLocalHost(), 4445);

        HeaderIOManager hiom = new HeaderIOManager(local, 4);
        HeartbeatSender heartbeatSender = new HeartbeatSender(hiom.getSocket(), other);

        for (;;) {
          try {
            hiom.update();
            heartbeatSender.addChannel(++channelID);
            heartbeatSender.update();
            SocketRequest sr = hiom.recv();
            //if (sr != null) { }
            if (System.nanoTime() - beginning > 2_000_000_000) break;
          } catch (InterruptedException ignored) {
          } catch (Exception e) {
            e.printStackTrace();
          }
          Thread.sleep(1);
        }

        hiom.shutdownNow();
      } catch (InterruptedException ignored) {
      } catch (Exception e) {
        System.err.println("Encountered error in test:");
        e.printStackTrace();
      }
    });
    poolExecutor.execute(() -> {
      try {
        long beginning = System.nanoTime();
        HeaderIOManager hiom = new HeaderIOManager(new InetSocketAddress(InetAddress.getLocalHost(), 4445), 4);
        HeartbeatManager manager = new HeartbeatManager();
        for (;;) {
          try {
            hiom.update();
            manager.update();
            SocketRequest sr = hiom.recv();
            if (sr != null) {
              //System.out.println("received header with opcode: " + sr.getHeader().opcode());
              if (sr.getHeader().opcode() == Constants.OP_HEARTBEAT) {
                HeartbeatHeader heartbeatHeader = (HeartbeatHeader) sr.getHeader();
                manager.processHeartbeat(heartbeatHeader.getChannelID(), sr.getAddress());
                System.out.println(heartbeatHeader.getChannelID());
              }
            }
          } catch (InterruptedException ignored) {
          } catch (Exception e) {
            e.printStackTrace();
          }

          if (System.nanoTime() - beginning > 2_000_000_000) {
            break;
          }
        }

        hiom.shutdownNow();
      } catch (Exception e) {
        System.err.println("Encountered error in test:");
        e.printStackTrace();
      }
    });
    poolExecutor.shutdown();
    poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    poolExecutor.shutdownNow();
  }
}
