package models.chatClients.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import models.Message;
import models.chatClients.ChatClient;
import models.database.JdbcDatabaseOperations;
import models.gui.LocalDateTimeDeserializer;
import models.gui.LocalDateTimeSerializer;
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

public class ApiChatClient implements ChatClient {
    private String loggedUser;
    private List<String> loggedUsers;
    private List<Message> messages;
    private List<ActionListener> loggedUserListeners = new ArrayList<>();
    private List<ActionListener> messageListeners = new ArrayList<>();
    private final String BASE_URL = "http://fimuhkpro22021.aspifyhost.cz";
    private String apiToken;

    private Gson gson;


    public ApiChatClient() {
        loggedUsers = new ArrayList<>();
        messages = new ArrayList<>();

        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .setPrettyPrinting()
                .create();


        Runnable refreshData = () -> {
            Thread.currentThread().setName("RefreshData");

            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isAuthenticated())
                    continue;

                refreshLoggedUsers();
                refreshMessages();
            }
        };

        Thread refreshDataThread = new Thread(refreshData);
        refreshDataThread.start();
    }

    private void refreshLoggedUsers() {
        try {
            String url = BASE_URL + "/api/Chat/GetLoggedUsers";
            HttpGet get = new HttpGet(url);

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonBody = EntityUtils.toString(response.getEntity());
                loggedUsers = gson.fromJson(jsonBody, new TypeToken<ArrayList<String>>(){}.getType());
            }
            raiseLoggedUsersChangedEvent();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshMessages() {
        try {
            String url = BASE_URL + "/api/Chat/GetMessages";
            HttpGet get = new HttpGet(url);

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonBody = EntityUtils.toString(response.getEntity());
                messages = gson.fromJson(jsonBody, new TypeToken<ArrayList<Message>>(){}.getType());
            }
            raiseMessagesChangedEvent();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(String text) {
        if (!isAuthenticated())
            return;

        try {
            SendMessageRequest messageRequest = new SendMessageRequest(apiToken, text);
            String url = BASE_URL + "/api/Chat/SendMessage";
            HttpPost post = new HttpPost(url);
            String jsonBody = gson.toJson(messageRequest);
            StringEntity body = new StringEntity(jsonBody, "UTF-8");
            body.setContentType("application/json");
            post.setEntity(body);

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 204) {
                refreshMessages();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void login(String username) {
        try {
            String loginUrl = BASE_URL + "/api/Chat/Login";
            HttpPost post = new HttpPost(loginUrl);
            StringEntity body = new StringEntity(String.format("\"%s\"", username), "UTF-8");
            body.setContentType("application/json");
            post.setEntity(body);

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {
                apiToken = EntityUtils.toString(response.getEntity());
                apiToken = apiToken.replace("\"", "").trim();

                loggedUser = username;
                refreshLoggedUsers();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logout() {
        try {
            String logoutUrl = BASE_URL + "/api/Chat/Logout";
            HttpPost post = new HttpPost(logoutUrl);
            StringEntity body = new StringEntity(String.format("\"%s\"", apiToken), "UTF-8");
            body.setContentType("application/json");
            post.setEntity(body);

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 204) {
                apiToken = null;
                loggedUser = null;
                loggedUsers.clear();

                raiseLoggedUsersChangedEvent();
            }

        } catch (Exception e) {
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
