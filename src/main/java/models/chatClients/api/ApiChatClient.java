package models.chatClients.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.Message;
import models.chatClients.ChatClient;
import models.gui.LocalDateTimeDeserializer;
import models.gui.LocalDateTimeSerializer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiChatClient implements ChatClient
{

        private String loggedUser;
        private List<String> loggedUsers;
        private List<Message> messages;
        private List<ActionListener> loggedUserListeners = new ArrayList<>();
        private List<ActionListener> messageListeners = new ArrayList<>();

        private final String BASE_URL = "http://fimuhkpro22021.aspifyhost.cz";
        private String token;
        private Gson gson;

        public ApiChatClient() {
            loggedUsers = new ArrayList<>();
            messages = new ArrayList<>();

            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                    .create();


            // Nové vlákno
            Runnable refreshData = ()->{
                Thread.currentThread().setName("Refresh Data Thread");
                try
                {
                    while(true)
                    {
                        if(isAuthenticated())
                        {
                            refreshLoggedUsers();
                            refreshMessages();
                        }
                        TimeUnit.SECONDS.sleep(2);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            };

            Thread refreshDataThread = new Thread(refreshData);
            refreshDataThread.start();
        }

        @Override
        public void sendMessage(String text) {

            try
            {
                SendMessageRequest msgRequest = new SendMessageRequest(token, text);
                String url = BASE_URL + "/api/Chat/SendMessage";
                HttpPost post = new HttpPost(url);

                String jsonBody = gson.toJson(msgRequest);

                StringEntity body = new StringEntity(
                  jsonBody,
                  "utf-8"
                );
                body.setContentType("application/json");
                post.setEntity(body);

                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post);

                if(response.getStatusLine().getStatusCode() == 204)
                {
                    refreshMessages();
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            messages.add(new Message(loggedUser, text));
            System.out.printf("new message - " + text);
            raiseMessagesChangedEvent();
        }

        @Override
        public void login(String username) {
            try
            {
                String url = BASE_URL + "/api/Chat/Login";
                HttpPost post = new HttpPost(url);
                StringEntity body = new StringEntity(
                        "\""+username+"\"",
                        "utf-8"
                );
                body.setContentType("application/json");
                post.setEntity(body);

                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post);

                if(response.getStatusLine().getStatusCode() == 200)
                {
                    token = EntityUtils.toString(response.getEntity());
                    token = token.replace("\"", "").trim();

                    loggedUser = username;
                    System.out.println("user logged in " + username);
                    raiseLoggedUsersChangedEvent();
                    refreshLoggedUsers();
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void logout() {

            try
            {
                String url = BASE_URL + "/api/Chat/Logout";
                HttpPost post = new HttpPost(url);
                StringEntity body = new StringEntity(
                        "\""+token+"\"",
                        "utf-8"
                );
                body.setContentType("application/json");
                post.setEntity(body);

                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post);

                if(response.getStatusLine().getStatusCode() == 204)
                {
                    token = null;
                    loggedUser = null;
                    loggedUsers.clear();
                    System.out.println("user logged out");
                    raiseLoggedUsersChangedEvent();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            /*
            messages.add(new Message(Message.USER_LOGGED_OUT, loggedUser));
            raiseMessagesChangedEvent();
            loggedUsers.remove(loggedUser);
            loggedUser = null;
            raiseLoggedUsersChangedEvent();
            */

        }


    private void refreshLoggedUsers()
    {
        try{
            String url = BASE_URL + "/api/Chat/GetLoggedUsers";
            HttpGet get = new HttpGet(url);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(get);

            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
                String jsonBody = EntityUtils.toString(httpResponse.getEntity());
                loggedUsers = gson.fromJson(
                        jsonBody,
                        new TypeToken<ArrayList<String>>(){}.getType()
                );
                raiseLoggedUsersChangedEvent();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void refreshMessages()
    {
        try{
            String url = BASE_URL + "/api/Chat/GetMessages";
            HttpGet get = new HttpGet(url);

            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(get);

            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
                String jsonBody = EntityUtils.toString(httpResponse.getEntity());
                loggedUsers = gson.fromJson(
                        jsonBody,
                        new TypeToken<ArrayList<Message>>(){}.getType()
                );
                raiseMessagesChangedEvent();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
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

