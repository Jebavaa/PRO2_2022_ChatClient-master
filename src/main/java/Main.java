import models.chatClients.ChatClient;
import models.chatClients.FileChatClient;
import models.chatClients.InMemoryChatClient;
import models.chatClients.fileOperations.ChatFileOperations;
import models.chatClients.fileOperations.JsonChatFileOperations;
import models.gui.MainFrame;

public class Main {
    public static void main(String[] args) {
        ChatFileOperations chatFileOperations = new JsonChatFileOperations();
        ChatClient chatClient = new FileChatClient(chatFileOperations);

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
}