package client;

import javax.swing.*;

public class GUI {

    private static class MainFrameHolder {
        static final MainFrame INSTANCE = new MainFrame();
    }

    static MainFrame getInstance() {
        return MainFrameHolder.INSTANCE;
    }

    public static void writeMessage(long channelID, long messageID, String nick, String message) {
        getInstance().addMessageToChannel(channelID, messageID, nick, message);
    }

    public static void writeInfo(long channelID, long messageID, String message) {
        final String infoNick = "SERVER";
        getInstance().addMessageToChannel(channelID, messageID, infoNick, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::getInstance);
    }

}
