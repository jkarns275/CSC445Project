package client;

import client.workers.JoinSwingWorker;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainFrame extends JFrame {

    private JTabbedPane channels;
    private ChannelPanel messagePanel;
    private JTextField input;
    private Client client;

    /**
     * Constructor for MainFrame class.
     */
    public MainFrame() {
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

        messagePanel = new ChannelPanel(-1, "*MSG*", "");
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
            new Thread(client).start();
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
                        printToMesssageChannel("ERROR","Connect to server failed.");
                    }
                    break;
                case "/me":
                    break;
                case "/whois":
                    break;
                case "/join":
                    JoinSwingWorker joinWorker = new JoinSwingWorker(client, substrings[1], substrings[2]);
                    try {
                        Optional<ChannelPanel> channel = joinWorker.get(2, TimeUnit.SECONDS);
                        if (channel.isPresent()) {
                            addChannel(channel.get());
                        } else {
                            printToMesssageChannel("ERROR", "Joining channel failed.");
                        }
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        printToMesssageChannel("ERROR", "Joining channel failed.");
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
     * @param panel ChannelPanel representing some channel
     */
    public void addChannel(ChannelPanel panel) {
        SwingUtilities.invokeLater(() -> {
            channels.addTab(panel.getChannelName(), panel);
            channels.setSelectedComponent(panel);
        });
    }

    // for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
        });
    }

}
