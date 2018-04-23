package client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class MainFrame extends JFrame {

    private JTabbedPane channels;
    private ChannelPanel messagePanel;
    private JTextField input;
    private Client client;

    /**
     * Constructor for MainFrame class.
     */
    public MainFrame(String nick) {
        super();
        this.getContentPane().setLayout(new BorderLayout());
        initWidgets();
        this.setTitle("Chat");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * Instantiate all the widgets used in this class.
     */
    private void initWidgets() {
        channels = new JTabbedPane();
        this.add(channels, BorderLayout.CENTER);

        messagePanel = new ChannelPanel("*MSG*", "");
        channels.addTab(messagePanel.getChannelName(), messagePanel);

        input = new JTextField();
        input.addActionListener(e -> {
            parseMessage(input.getText());
            input.setText("");
        });
        //input.setPreferredSize(new Dimension(800, 10));
        this.add(input, BorderLayout.SOUTH);
    }

    private boolean connect(String hostname, int port) {
        try {
            this.client = new Client(new InetSocketAddress(hostname, port), port);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void printToMesssageChannel(String sender, String message) {
        messagePanel.addMessage(sender, message);
    }

    /**
     * Direct message or command inputted by the user to the correct function for sending.
     * @param input String input from user
     */
    private void parseMessage(String input) {
        // check if message is a command
        if (input.regionMatches(0, "/", 0, 1)) {
            // command packet
            String[] substrings = input.split(" ");
            switch(substrings[0]) {
                case "/connect":
                    if (!connect(substrings[1], Integer.valueOf(substrings[2]))) {
                        printToMesssageChannel("","Connect to server failed.");
                    }
                    break;
                case "/me":
                    break;
                case "/whois":
                    break;
                case "/join":
                    try {
                        client.sendJoinHeader(substrings[1], substrings[2]);
                        joinChannel(substrings[1], substrings[2]);
                    } catch (InterruptedException e) {
                        printToMesssageChannel("", "Joining channel failed.");
                        e.printStackTrace();
                    }
                    break;
                case "/op":
                    break;
                default:
                    // not a command
            }
        } else {
            // write packet
            ChannelPanel channel = (ChannelPanel) channels.getSelectedComponent();
            channel.addMessage(channel.getNick(), input);
        }
    }

    /**
     * Add and display a channel to the client gui.
     * @param channelName Name of the channel
     */
    public void joinChannel(String channelName, String nick) {
        SwingUtilities.invokeLater(() -> {
            ChannelPanel channel = new ChannelPanel(channelName, nick);
            channels.addTab(channelName, channel);
            channels.setSelectedComponent(channel);
        });
    }

    // for testing
    public static void main(String[] args) {
        final String nick = "bb";
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(nick);
            frame.joinChannel("#java", nick);
        });
    }

}
