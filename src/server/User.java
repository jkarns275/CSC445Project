package server;

import java.net.InetSocketAddress;

public class User {

  public final long joinTime;
  public String username;
  public InetSocketAddress address;
  private boolean muted = false;


    public User (String username, InetSocketAddress address) {
        this.username = username;
        this.address = address;
        this.joinTime = System.nanoTime();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean getMuted() {
        return muted;
    }

    @Override
    public String toString() {
      return "<" + this.username + "; " + this.address + "; " + muted + ">";
    }
}
