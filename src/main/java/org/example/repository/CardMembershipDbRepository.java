package org.example.repository;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class CardMembershipDbRepository extends CardMembershipRepository {
    private String dbUrl;
    private String username;
    private String password;

    public CardMembershipDbRepository(String dbUrl, String username, String password) {
        super(""); // Nu folosim fisier, trimitem string gol
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public void saveMembership(Long cardId, Long duckId) {
        String sql = "INSERT INTO card_memberships (card_id, duck_id) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            statement.setLong(2, duckId);
            statement.executeUpdate();
        } catch (SQLException e) {
            // Ignoram duplicatele
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMembership(Long cardId, Long duckId) {
        String sql = "DELETE FROM card_memberships WHERE card_id = ? AND duck_id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            statement.setLong(2, duckId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Long> getMemberIds(Long cardId) {
        Set<Long> memberIds = new HashSet<>();
        String sql = "SELECT duck_id FROM card_memberships WHERE card_id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                memberIds.add(rs.getLong("duck_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberIds;
    }

    // Aceasta metoda este esentiala pentru CardService-ul tau actualizat
    public Long getCardIdForDuck(Long duckId) {
        String sql = "SELECT card_id FROM card_memberships WHERE duck_id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, duckId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getLong("card_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}