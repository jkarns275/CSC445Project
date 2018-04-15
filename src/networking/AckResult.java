package networking;

public interface AckResult {
  /**
   * @return Returns a runnable object that will resend packets to hosts that failed to send an ack.
   */
  public Runnable resend();

  /**
   * @return If all acks that were expected were received, returns true, otherwise false.
   */
  public boolean wasSuccessful();
}
