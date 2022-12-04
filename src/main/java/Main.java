import models.chatClients.ChatClient;
import models.chatClients.FileChatClient;
import models.chatClients.InMemoryChatClient;
import models.chatClients.api.ApiChatClient;
import models.chatClients.fileOperations.ChatFileOperations;
import models.chatClients.fileOperations.JsonChatFileOperations;
import models.database.DbInitializer;
import models.gui.MainFrame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String databaseDriver = "org.apache.derby.jdbc.EmbeddedDriver";
        String databaseUrl = "jdbc:derby:D:\\School\\PRGF1\\PRO2_2022_ChatClient-master\\src\\ChatClientDb";

        //DbInitializer dbInitializer = new DbInitializer(databaseDriver, databaseUrl);
        //dbInitializer.init();

        ChatFileOperations chatFileOperations = new JsonChatFileOperations();
        ChatClient chatClient = new ApiChatClient();


        //region Test Reflexe
        /*
        Class<ApiChatClient> refExample = ApiChatClient.class;
        List<Field> fields = getAllFields(refExample);

        System.out.println("Class name - " + refExample.getName());
        for(Field f : fields)
        {
            System.out.println(f.getName() + " " + f.getType());
        }*/
        //endregion


        MainFrame window = new MainFrame(700, 500, chatClient);
        //test();
    }

    private static void test() {
        ChatClient client = new InMemoryChatClient();

        client.login("bot");

        client.sendMessage("Hello there");
        client.sendMessage("Testing 123");

        client.logout();
    };

    private static List<Field> getAllFields(Class<?> cls)
    {
        List<Field> fields = new ArrayList<>();
        for(Field f: cls.getDeclaredFields())
        {
            fields.add(f);
        }
        return fields;
    }
}