package networking;

import java.util.concurrent.ArrayBlockingQueue;

public interface AckResult {
  /**
   * @return Returns a runnable object that will resend packets to hosts that failed to send an ack.
   */
  public SendJob resend(ArrayBlockingQueue<SendJob> doneQueue);

  /**
   * @return If all acks that were expected were received, returns true, otherwise false.
   */
  public boolean wasSuccessful();
}
