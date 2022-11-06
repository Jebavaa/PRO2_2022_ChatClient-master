package models.gui;

import models.Message;
import models.chatClients.ChatClient;
import models.chatClients.InMemoryChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    private ChatClient chatClient;
    private JTextField messageTextField;
    private JTextArea chat;
    public MainFrame(int width, int height, ChatClient chatClient) {
        super("PRO2 2022 ChatClient");
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.chatClient = chatClient;

        initGui();
        setVisible(true);
    }

    private void initGui() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(initLoginPanel(), BorderLayout.NORTH);
        mainPanel.add(initChatPanel(), BorderLayout.CENTER);
        mainPanel.add(initMessagePanel(), BorderLayout.SOUTH);
        mainPanel.add(initLoggedUsersPanel(), BorderLayout.EAST);

        add(mainPanel);
    }

    private JPanel initLoginPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Username: "));

        JTextField usernameInputField = new JTextField("", 30);
        panel.add(usernameInputField);
        JButton loginButton = new JButton("Let me in!");
        loginButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (chatClient.isAuthenticated()) {
                    chatClient.logout();
                    usernameInputField.setEditable(true);
                    messageTextField.setEditable(false);
                    loginButton.setText("Let me in!");
                } else if (!usernameInputField.getText().isEmpty()) {
                    chatClient.login(usernameInputField.getText());
                    usernameInputField.setEditable(false);
                    messageTextField.setEditable(true);
                    loginButton.setText("Let me out!");
                }
            }
        });
        panel.add(loginButton);
        return panel;
    }

    private JPanel initChatPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        chat = new JTextArea();
        chat.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chat);

        panel.add(scrollPane);

        chatClient.addMessageListened(e -> {
            refreshMessages();
        });

        return panel;
    }

    private JPanel initMessagePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        messageTextField = new JTextField("", 50);
        messageTextField.setEditable(false);
        panel.add(messageTextField);
        JButton sendButton = new JButton("Send it!");
        panel.add(sendButton);

        sendButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                chatClient.sendMessage(messageTextField.getText());
                refreshMessages();
                messageTextField.setText("");
            }
        });

        messageTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        sendButton.doClick();
                    default:
                        super.keyPressed(e);
                }
            }
        });

        return panel;
    }

    private JPanel initLoggedUsersPanel() {
//        Object[][] data = new Object[][] {
//                {"0.0", "0.1"},
//                {"1.0", "1.1"}
//        };
//
//        String[] colNames = {"a", "b"};

        JPanel panel = new JPanel();
        JTable table = new JTable();
        LoggedUsersTableModel loggedUsersModel = new LoggedUsersTableModel(chatClient);
        table.setModel(loggedUsersModel);
        JScrollPane scrollPane = new JScrollPane(table);

        chatClient.addLoggedUsersListened(e -> {
            loggedUsersModel.fireTableDataChanged();
        });

        scrollPane.setPreferredSize(new Dimension(250, 500));
        panel.add(scrollPane);

        return panel;
    }

    private void refreshMessages() {
        if (!chatClient.isAuthenticated()) {
            return;
        }

        chat.setText("");
        for (Message msg: chatClient.getMessages()) {
            chat.append(msg.toString());
            chat.append("\n");
        }
    }
}
