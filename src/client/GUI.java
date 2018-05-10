package client;

import javax.swing.*;

/**
 * Threadsafe singleton container for MainFrame GUI class.
 * Allows Client class to interact with the GUI.
 */
public class GUI {

    private static class MainFrameHolder {
        static final MainFrame INSTANCE = new MainFrame();
    }

    static MainFrame getInstance() {
        return MainFrameHolder.INSTANCE;
    }

    /**
     * Display a message to the GUI on a specific channel.
     * @param channelID Identifier of the channel
     * @param messageID Identifier of the message
     * @param nick Sender of the message
     * @param message Contents of the message
     */
    public static void writeMessage(long channelID, long messageID, String nick, String message) {
        getInstance().addMessageToChannel(channelID, messageID, nick, message);
    }

    /**
     * Display an info message from the server to the GUI on a specific channel
     * @param channelID Identifier of the channel
     * @param messageID Identifier of the message
     * @param message Contents of the message
     */
    public static void writeInfo(long channelID, long messageID, String message) {
        final String infoNick = "SERVER";
        getInstance().addMessageToChannel(channelID, messageID, infoNick, message);
    }

    /**
     * Force a channel to be removed from the GUI in response to a kick sent by the server.
     * @param channelID Identifier of the channel
     */
    public static void kickUser(long channelID) {
        getInstance().removeChannel(channelID);
    }

    /**
     * Inform the GUI of a change in the mute status of the user on a channel.
     * @param channelID Identifier of the channel
     * @param status New mute status of the user
     */
    public static void setMuteStatus(long channelID, boolean status) {
        getInstance().setMuteChannel(channelID, status);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::getInstance);
    }

}
