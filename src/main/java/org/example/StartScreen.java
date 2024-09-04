package org.example;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class StartScreen {

    private JFrame frame;
    private String username;
    private String roomCode;
    private boolean isCreatingRoom;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {
                StartScreen startScreen = new StartScreen();
                startScreen.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public StartScreen() {
        initialize();
    }

    private void initialize() {
        // Main frame setup
        frame = new JFrame("Chat Application - Start");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Welcome panel
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        welcomePanel.setBackground(new Color(60, 63, 65));

        JLabel welcomeLabel = new JLabel("Welcome to the Chat Application", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(welcomeLabel);

        JLabel instructionLabel = new JLabel("Enter your username:", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(Color.LIGHT_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Add space
        welcomePanel.add(instructionLabel);

        // Username text field
        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(200, 30));  // Fix the size
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setHorizontalAlignment(JTextField.CENTER);
        usernameField.setBackground(new Color(69, 73, 74));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Add space
        welcomePanel.add(usernameField);

        frame.getContentPane().add(welcomePanel, BorderLayout.CENTER);

        // Buttons for joining or creating a room
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(new Color(60, 63, 65));

        JButton joinButton = new JButton("Join a Room");
        joinButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        joinButton.setBackground(new Color(88, 101, 242));
        joinButton.setForeground(Color.WHITE);
        joinButton.addActionListener(e -> {
            username = usernameField.getText();
            if (validateUsername()) {
                isCreatingRoom = false;
                showRoomCodeDialog();
            }
        });
        buttonPanel.add(joinButton);

        JButton createButton = new JButton("Create a Room");
        createButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        createButton.setBackground(new Color(88, 101, 242));
        createButton.setForeground(Color.WHITE);
        createButton.addActionListener(e -> {
            username = usernameField.getText();
            if (validateUsername()) {
                isCreatingRoom = true;
                generateRoomCodeAndProceed();
            }
        });
        buttonPanel.add(createButton);

        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private boolean validateUsername() {
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void generateRoomCodeAndProceed() {
        roomCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        JOptionPane.showMessageDialog(frame, "Room Code: " + roomCode, "Room Created", JOptionPane.INFORMATION_MESSAGE);
        proceedToChat();
    }

    private void showRoomCodeDialog() {
        roomCode = JOptionPane.showInputDialog(frame, "Enter the room code:", "Join Room", JOptionPane.PLAIN_MESSAGE);
        if (roomCode != null && !roomCode.trim().isEmpty()) {
            proceedToChat();
        } else {
            JOptionPane.showMessageDialog(frame, "Room code cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void proceedToChat() {
        frame.setVisible(false);
        ChatClientGUI chatClientGUI = new ChatClientGUI(username, roomCode, isCreatingRoom);
        chatClientGUI.display();
    }

    void display(){
        frame.setVisible(true);
    }
}
