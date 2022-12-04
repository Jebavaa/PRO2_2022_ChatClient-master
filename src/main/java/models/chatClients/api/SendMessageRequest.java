package models.chatClients.api;

public class SendMessageRequest {
    private String token;
    private String text;

    public SendMessageRequest(String token, String text) {
        this.token = token;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getToken() {
        return token;
    }
}
