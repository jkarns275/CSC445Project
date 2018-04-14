package server;

import java.util.HashMap;

public class Server {

    HashMap<Integer, Channel> chatRooms = new HashMap<Integer, Channel>();

    public static void main(String[] args) {
        new Server().listen();
    }

    public void listen() {
        while(true) {

        }
    }

    private void createChatRoom() {

    }

    private void deleteChatRoom() {

    }

    private void addUser(Channel chatRoom) {

    }

    private void deleteUser(Channel chatRoom) {

    }
}
