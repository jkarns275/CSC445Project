package client;

import javax.swing.*;

public class GUI {

    private static class MainFrameHolder {
        static final MainFrame INSTANCE = new MainFrame();
    }

    private static MainFrame getInstance() {
        return MainFrameHolder.INSTANCE;
    }

    public static void writeMessage(long channelID, long messageID, String nick, String message) {
        getInstance().addMessageToChannel(channelID, messageID, nick, message);
    }

    // for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::getInstance);
    }

}
