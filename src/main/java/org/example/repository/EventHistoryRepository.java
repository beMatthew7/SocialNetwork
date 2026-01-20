package org.example.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventHistoryRepository {
    private String dbUrl;
    private String username;
    private String password;

    public EventHistoryRepository(String dbUrl, String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }

    public void addHistory(Long userId, String message) {
        String sql = "INSERT INTO event_history (user_id, message) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, message);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHistory(Long userId) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT message FROM event_history WHERE user_id = ? ORDER BY created_at ASC";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                history.add(rs.getString("message"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}