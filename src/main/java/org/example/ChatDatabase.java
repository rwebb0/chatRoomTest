package org.example;

import java.sql.*;

public class ChatDatabase {
    private Connection connection;

    public ChatDatabase(String dbUrl) throws SQLException {
        this.connection = DriverManager.getConnection(dbUrl);
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() throws SQLException {
        String createMessagesTableSQL = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_id TEXT NOT NULL, " +
                "sender TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, " +
                "content TEXT NOT NULL);";
        Statement stmt = connection.createStatement();
        stmt.execute(createMessagesTableSQL);

        String createRoomInfoTableSQL = "CREATE TABLE IF NOT EXISTS room_info (" +
                "room_id TEXT PRIMARY KEY, " +
                "salt BLOB NOT NULL);";
        stmt.execute(createRoomInfoTableSQL);
    }

    public void saveMessage(String roomId, String sender, String timestamp, String content) throws SQLException {
        String sql = "INSERT INTO messages (room_id, sender, timestamp, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            pstmt.setString(2, sender);
            pstmt.setString(3, timestamp);
            pstmt.setString(4, content);
            pstmt.executeUpdate();
        }
    }

    public ResultSet getMessages(String roomId) throws SQLException {
        String sql = "SELECT sender, timestamp, content FROM messages WHERE room_id = ? ORDER BY id ASC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, roomId);
        return pstmt.executeQuery();
    }

    public void saveRoomSalt(String roomId, byte[] salt) throws SQLException {
        String sql = "INSERT OR REPLACE INTO room_info (room_id, salt) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            pstmt.setBytes(2, salt);
            pstmt.executeUpdate();
        }
    }

    public byte[] getRoomSalt(String roomId) throws SQLException {
        String sql = "SELECT salt FROM room_info WHERE room_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("salt");
            }
            return null;  // No salt found for this room
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
