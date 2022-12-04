package models.database;

import models.Message;

import java.util.List;

public interface DatabaseOperations
{
    void addMessage(Message message);
    List<Message> getMessages();
}
