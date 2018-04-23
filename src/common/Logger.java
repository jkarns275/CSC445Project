package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

  public static void log(Object msg) {
    LocalDateTime date = LocalDateTime.now();
    String timestamp = date.format(DateTimeFormatter.ISO_LOCAL_TIME);
    System.err.printf("[%s][LOG] %s", timestamp, msg.toString());
  }

  public static void err(Object msg) {
    LocalDateTime date = LocalDateTime.now();
    String timestamp = date.format(DateTimeFormatter.ISO_LOCAL_TIME);
    System.err.printf("[%s][ERR] %s", timestamp, msg.toString());
  }
}
