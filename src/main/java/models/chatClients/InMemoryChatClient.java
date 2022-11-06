package models.chatClients;

import models.Message;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class InMemoryChatClient implements ChatClient {
    private String loggedUser;
    private List<String> loggedUsers;
    private List<Message> messages;
    private List<ActionListener> loggedUserListeners = new ArrayList<>();
    private List<ActionListener> messageListeners = new ArrayList<>();

    public InMemoryChatClient() {
        loggedUsers = new ArrayList<>();
        messages = new ArrayList<>();
    }

    @Override
    public void sendMessage(String text) {
        if (!isAuthenticated())
            return;
        messages.add(new Message(loggedUser, text));
        System.out.printf("%s sent message \"%s\"%n", loggedUser, text);
        raiseMessagesChangedEvent();
    }

    @Override
    public void login(String username) {
        loggedUser = username;
        loggedUsers.add(loggedUser);
        messages.add(new Message(Message.USER_LOGGED_IN, username));
        raiseLoggedUsersChangedEvent();
        raiseMessagesChangedEvent();
    }

    @Override
    public void logout() {
        messages.add(new Message(Message.USER_LOGGED_OUT, loggedUser));
        raiseMessagesChangedEvent();
        loggedUsers.remove(loggedUser);
        loggedUser = null;
        raiseLoggedUsersChangedEvent();
    }

    @Override
    public boolean isAuthenticated() {
        return loggedUser != null;
    }

    @Override
    public List<String> getLoggedUsers() {
        return loggedUsers;
    }

    @Override
    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public void addLoggedUsersListened(ActionListener listener) {
        loggedUserListeners.add(listener);
    }

    @Override
    public void addMessageListened(ActionListener listener) {
        messageListeners.add(listener);
    }

    private void raiseLoggedUsersChangedEvent() {
        for (ActionListener listener: loggedUserListeners) {
            listener.actionPerformed(new ActionEvent(this, 1, "usersChanged"));
        }
    }

    private void raiseMessagesChangedEvent() {
        for (ActionListener listener: messageListeners) {
            listener.actionPerformed(new ActionEvent(this, 1, "messagesChanged"));
        }
    }
}
