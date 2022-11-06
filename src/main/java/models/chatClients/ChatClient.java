package models.chatClients;

import models.Message;

import java.awt.event.ActionListener;
import java.util.List;

public interface ChatClient {
    void sendMessage(String text);
    void login(String username);
    void logout();
    boolean isAuthenticated();
    List<String> getLoggedUsers();
    List<Message> getMessages();

    void addLoggedUsersListened(ActionListener listener);
    void addMessageListened(ActionListener listener);
}
