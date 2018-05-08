package client;

import client.reps.Message;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ChannelPanel extends JPanel {

    private final long id;
    private final String channelName;
    private final String nick;
    private boolean isMuted;
    private JTextArea chatArea;
    private JScrollPane scrollPane;
    private SortedSet<Message> messages;
    private long firstMessageID = -1;
    //private JTextArea onlineUsers;

    /**
     * Constuctor for ChannelPanel class.
     * @param id identifier of this channel
     * @param channelName Name of the channel this will represent
     * @param nick User's assigned nick in this channel
     */
    public ChannelPanel(long id, String channelName, String nick) {
        super();
        this.messages = Collections.synchronizedSortedSet(new TreeSet<>());
        this.id = id;
        this.nick = nick;
        this.channelName = channelName;
        this.setLayout(new BorderLayout());
        initWidgets();
    }

    public boolean isMuted() {
        return this.isMuted;
    }

    public void setIsMuted(boolean status) {
        isMuted = status;
    }

    /**
     * Initialize and configure widgets used in channel interface.
     */
    private void initWidgets() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);

        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        this.add(scrollPane, BorderLayout.CENTER);

        /*
        onlineUsers = new JTextArea();
        onlineUsers.setEditable(false);
        this.add(onlineUsers, BorderLayout.EAST);
        */
    }

    private void updateDisplay() {
        chatArea.setText("");

        String newContent =
            messages.stream()
            .map((msg) -> String.format("[%3d] %19s| %s\n", msg.getId(), msg.getNick(), msg.getContent()))
            .reduce(String::concat)
            .get();
        this.chatArea.setText(newContent);


        JScrollBar vert = scrollPane.getVerticalScrollBar();
        if (vert != null) {
            SwingUtilities.invokeLater(() -> vert.setValue(vert.getModel().getMaximum()));
        }
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
     * @param id Identifier signifying ordering of this message
     * @param name Name of user who sent this message
     * @param message Content of the message
     */
    public void addMessage(long id, String name, String message) {
        if (this.firstMessageID == -1) this.firstMessageID = id;
        messages.add(new Message(id, name, message));
        updateDisplay();
    }

    public long[] validateOrdering() {
        long prev = messages.first().getId() - 1;
        for (Message message : messages) {
            long mid = message.getId();
            if (prev + 1 != message.getId()) {
              return new long[] { prev+1, message.getId() };
            }
            prev = mid;
        }
        return new long[0];
    }

    // for printing messages to message channel
    void addMessage(String name, String message) {
        messages.add(new Message(name, message));
        updateDisplay();
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
