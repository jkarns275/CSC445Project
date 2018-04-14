package networking.headers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class InfoHeader implements Header {
  /**
   * Recipient has been kicked from room.
   */
  public final static byte INFO_KICKED        = 0;
  /**
   * Recipient has been muted
   */
  public final static byte INFO_MUTED         = 1;
  /**
   * Recipient has been unmuted.
   */
  public final static byte INFO_UNMUTED       = 2;
  /**
   * Means that there is a message from the server for the channel that should be displayed
   */
  public final static byte INFO_SERVER_MSG    = 3;
  /**
   * The channel or server was closed.
   */
  public final static byte INFO_CLOSED        = 4;

  private long channelID;
  private byte infoCode;
  private long messageID;
  private String message = "ERROR";

  public InfoHeader(long channelID, byte infoCode, long messageID, String message) {
    this.channelID = channelID; this.infoCode = infoCode; this.messageID = messageID; this.message = message;
  }

  InfoHeader() { }

  public void writeObject(ObjectOutputStream out) throws IOException {

  }

  public void readObject(ObjectInputStream in) throws IOException {

  }

  public long getChannelID() {
    return channelID;
  }

  public byte getInfoCode() {
    return infoCode;
  }

  public long getMessageID() {
    return messageID;
  }

  public String getMessage() {
    return message;
  }
}
