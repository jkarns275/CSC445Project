package client;

import javax.swing.*;
import java.awt.*;

public class ChannelPanel extends JPanel {

    private final long id;
    private final String channelName;
    private final String nick;
    private JTextArea chatArea;
    //private JTextArea onlineUsers;

    /**
     * Constuctor for ChannelPanel class.
     * @param id identifier of this channel
     * @param channelName Name of the channel this will represent
     * @param nick User's assigned nick in this channel
     */
    public ChannelPanel(long id, String channelName, String nick) {
        super();
        this.id = id;
        this.nick = nick;
        this.channelName = channelName;
        this.setLayout(new BorderLayout());
        initWidgets();
    }

    /**
     * Initialize and configure widgets used in channel interface.
     */
    private void initWidgets() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        this.add(chatArea, BorderLayout.CENTER);

        /*
        onlineUsers = new JTextArea();
        onlineUsers.setEditable(false);
        this.add(onlineUsers, BorderLayout.EAST);
        */
    }

    /**
     * Get identifier of this channel.
     * @return channel id
     */
    public long getChannelID() {
        return this.id;
    }

    /**
     * Get name of the channel this panel represents.
     * @return Channel Name
     */
    public String getChannelName() {
        return this.channelName;
    }

    /**
     * Get user's nick on this channel.
     * @return User nick
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * Add a message to be displayed on this channel.
     * @param name Name of user who sent this message
     * @param message Content of the message
     */
    public void addMessage(String name, String message) {
        SwingUtilities.invokeLater(() ->
                chatArea.setText(chatArea.getText() + String.format("%19s| %s%n", name, message)));
    }

    /*
    /**
     * Add a user to the list of users in the channel.
     * @param name Name the user is using
     */
    /*
    public void addUser(String name) {
        SwingUtilities.invokeLater(() ->
                onlineUsers.setText(onlineUsers.getText() + String.format("%s%n", name)));
    }
    */

    /*
    /**
     * Remove a user from the list of users in this channel.
     * @param name Name the user is using
     */
    /*
    public void removeUser(String name) {
        SwingUtilities.invokeLater(() -> {
            String users = onlineUsers.getText();
            String modUsers = users.replace(String.format("%s%n", name), "");
            onlineUsers.setText(modUsers);
        });
    }
    */

}
