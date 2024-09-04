package org.example;

import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatClientGUI {

    private ChatDatabase chatDatabase;
    private Socket socket;
    private SecretKey encryptionKey;
    private String roomCode;
    private JFrame frame;
    private JTextField textField;
    private JTextPane textPane;
    private String username;
    private boolean isCreatingRoom;
    private PrintWriter out;
    private BufferedReader in;
    private List<String> userList = new ArrayList<>();

    public ChatClientGUI(String username, String roomCode, boolean isCreatingRoom) {
        this.username = username;
        this.roomCode = roomCode;
        this.isCreatingRoom = isCreatingRoom;

        try {
            this.chatDatabase = new ChatDatabase("jdbc:sqlite:chat.db");

            this.encryptionKey = EncryptionUtil.deriveKeyFromPassword(roomCode, roomCode.getBytes());

            initialize();

            loadPreviousMessages();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPreviousMessages() {
        try {
            ResultSet rs = chatDatabase.getMessages(roomCode);
            while (rs.next()) {
                String sender = rs.getString("sender");
                String timestamp = rs.getString("timestamp");
                String encryptedContent = rs.getString("content");

                String content = EncryptionUtil.decrypt(encryptedContent, encryptionKey);

                System.out.println("DEBUG: Encrypted message from DB: " + encryptedContent);
                System.out.println("DEBUG: Decrypted message: " + content);

                appendMessage("[" + timestamp + "] " + sender + ": " + content, Color.WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display() {
        frame.setVisible(true);
    }

    private void initialize() {
        // Main frame setup
        frame = new JFrame("Chat Application - " + roomCode);
        frame.setBounds(100, 100, 600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Top panel (Room code and Users button)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(45, 45, 45));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        JLabel roomCodeLabel = new JLabel("Room Code: " + roomCode, SwingConstants.CENTER);
        roomCodeLabel.setForeground(Color.WHITE);
        roomCodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(roomCodeLabel, BorderLayout.CENTER);

        JButton usersButton = new JButton("Users");
        usersButton.setFocusPainted(false);
        usersButton.setBackground(new Color(64, 64, 64));
        usersButton.setForeground(Color.WHITE);
        usersButton.addActionListener(e -> showUsersList());
        topPanel.add(usersButton, BorderLayout.EAST);

        JButton leaveButton = new JButton("Leave Chat");
        leaveButton.setFocusPainted(false);
        leaveButton.setBackground(new Color(255, 69, 58));
        leaveButton.setForeground(Color.WHITE);
        leaveButton.addActionListener(e -> leaveChat());
        topPanel.add(leaveButton, BorderLayout.WEST);

        // Text pane (Chat display)
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(54, 57, 63));
        textPane.setForeground(Color.WHITE);
        textPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textPane.setMargin(new Insets(10, 10, 10, 10));
        frame.getContentPane().add(new JScrollPane(textPane), BorderLayout.CENTER);

        // Bottom panel (Message input and Send button)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(47, 49, 54));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(new Color(64, 68, 75));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(64, 68, 75)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        bottomPanel.add(textField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.setFocusPainted(false);
        sendButton.setBackground(new Color(88, 101, 242));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        bottomPanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(e -> sendMessage());

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        startClient(roomCodeLabel);
    }

    private void startClient(JLabel roomCodeLabel) {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(roomCode);
            out.println(username + " has joined the chat!");

            new ReadThread(socket, roomCodeLabel).start();
        } catch (IOException ex) {
            appendMessage("Error connecting to the server.", Color.RED);
        }
    }


    private void sendMessage() {
        String message = textField.getText();
        if (!message.isEmpty()) {
            String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
            try {
                System.out.println("DEBUG: Original message: " + message);

                String encryptedMessage = EncryptionUtil.encrypt(message, encryptionKey);

                System.out.println("DEBUG: Encrypted message: " + encryptedMessage);

                chatDatabase.saveMessage(roomCode, username, timestamp, encryptedMessage);

                out.println(username + ": " + message);

                textField.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showUsersList() {
        if (userList.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No users in the room.", "Users", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder users = new StringBuilder("Users in room:\n");
        for (String user : userList) {
            users.append(user).append("\n");
        }
        JOptionPane.showMessageDialog(frame, users.toString(), "Users", JOptionPane.INFORMATION_MESSAGE);
    }

    private void appendMessage(String message, Color color) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String timestamp = formatter.format(new Date());

        StyledDocument doc = textPane.getStyledDocument();

        Style usernameStyle = doc.addStyle("UsernameStyle", null);
        StyleConstants.setForeground(usernameStyle, new Color(114, 137, 218));
        StyleConstants.setBold(usernameStyle, true);

        Style messageStyle = doc.addStyle("MessageStyle", null);
        StyleConstants.setForeground(messageStyle, Color.WHITE);

        Style timestampStyle = doc.addStyle("TimestampStyle", null);
        StyleConstants.setForeground(timestampStyle, Color.GRAY);
        StyleConstants.setFontSize(timestampStyle, 12);

        try {
            String[] parts = message.split(": ", 2);
            if (parts.length == 2) {
                String messageUsername = parts[0];
                String messageContent = parts[1];

                if (!userList.contains(messageUsername)) {
                    userList.add(messageUsername);
                }

                doc.insertString(doc.getLength(), "[" + timestamp + "] ", timestampStyle);
                doc.insertString(doc.getLength(), messageUsername + ": ", usernameStyle);
                doc.insertString(doc.getLength(), messageContent + "\n\n", messageStyle);
            } else {
                doc.insertString(doc.getLength(), message + "\n\n", messageStyle);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        textPane.setCaretPosition(doc.getLength());
    }

    private void leaveChat() {
        try {
            out.println(username + " has left the chat.");
            socket.close();
            frame.dispose();

            // Open the StartScreen class
            SwingUtilities.invokeLater(() -> {
                StartScreen startScreen = new StartScreen();
                startScreen.display();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadThread extends Thread {
        private Socket socket;
        private JLabel roomCodeLabel;

        public ReadThread(Socket socket, JLabel roomCodeLabel) {
            this.socket = socket;
            this.roomCodeLabel = roomCodeLabel;
        }

        public void run() {
            String response;
            try {
                while ((response = in.readLine()) != null) {
                    if (response.startsWith("Room code: ")) {
                        final String roomCodeFromServer = response.substring(11);
                        SwingUtilities.invokeLater(() -> {
                            roomCodeLabel.setText(roomCodeFromServer);
                            frame.setTitle("Chat Application - " + roomCodeFromServer);
                        });
                    } else {
                        appendMessage(response, Color.WHITE);
                    }
                }
            } catch (IOException ex) {
                appendMessage("Error reading from server.", Color.RED);
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClientGUI chatClient = new ChatClientGUI("Anonymous", "DefaultRoom", true);
            chatClient.display();
        });
    }
}
