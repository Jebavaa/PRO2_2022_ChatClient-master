package models.chatClients.fileOperations;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import models.Message;
import models.gui.LocalDateTimeDeserializer;
import models.gui.LocalDateTimeSerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JsonChatFileOperations implements ChatFileOperations {
    private final Gson gson;
    private static final String MESSAGE_FILE = "./messages.json";

    public JsonChatFileOperations() {
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void writeMessages(List<Message> messages) {
        String jsonText = gson.toJson(messages);

        try {
            FileWriter writer = new FileWriter(MESSAGE_FILE);
            writer.write(jsonText);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Message> readMessages() {
        List<Message> messages;
        try {
            FileReader reader = new FileReader(MESSAGE_FILE);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonText.append(line);
            }

            Type targetType = new TypeToken<ArrayList<Message>>(){}.getType();
            messages = gson.fromJson(jsonText.toString(), targetType);

            return messages;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}