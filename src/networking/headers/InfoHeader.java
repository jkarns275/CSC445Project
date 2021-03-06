package networking.headers;

import common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class InfoHeader extends Header {
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

  @Override
  public void writeObject(ObjectOutputStream out) throws IOException {
    out.writeByte(opcode());
    out.writeLong(this.channelID);
    out.writeByte(this.infoCode);
    out.writeLong(this.messageID);
    out.writeShort(this.message.length());
    out.writeBytes(message);
  }

  @Override
  public void readObject(ObjectInputStream in) throws IOException {
    this.channelID = in.readLong();
    this.infoCode = in.readByte();
    this.messageID = in.readLong();

    final int messageLen = in.readShort();
    final byte[] p = new byte[messageLen];
    if (in.read(p) != messageLen) throw new IOException("Prematurely encountered end of input stream.");
    this.message = new String(p);
  }

  @Override
  public int opcode() { return Constants.OP_INFO; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InfoHeader that = (InfoHeader) o;
    return channelID == that.channelID &&
      infoCode == that.infoCode &&
      messageID == that.messageID &&
      Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return (int) ((((this.channelID >> this.opcode()) >> this.infoCode) ^ this.message.hashCode()) ^ this.messageID);
  }

  public long getChannelID() { return channelID; }

  public byte getInfoCode() { return infoCode; }

  public long getMessageID() { return messageID; }

  public String getMessage() { return message; }

  public void setInfoCode(byte infoCode) {
    this.infoCode = infoCode;
  }
}
