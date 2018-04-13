package Server;

import java.util.HashMap;

public class Server {

    HashMap<Integer,ChatRoom> chatRooms = new HashMap<Integer, ChatRoom>();

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

    private void addUser(ChatRoom chatRoom) {

    }

    private void deleteUser(ChatRoom chatRoom) {

    }
}
