package org.example.repository;

import org.example.domain.DuckType;
import org.example.interactions.DuckCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDbRepository implements Repository<Long, DuckCard> {
    private String dbUrl;
    private String username;
    private String password;

    public CardDbRepository(String dbUrl, String username, String password) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public DuckCard findOne(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        String sql = "SELECT * FROM cards WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String name = rs.getString("card_name");
                DuckType type = DuckType.valueOf(rs.getString("target_type"));
                DuckCard card = new DuckCard(name, type);
                card.setID(id);
                return card;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<DuckCard> findAll() {
        List<DuckCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("card_name");
                DuckType type = DuckType.valueOf(rs.getString("target_type"));
                DuckCard card = new DuckCard(name, type);
                card.setID(id);
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    @Override
    public DuckCard save(DuckCard entity) {
        if (entity == null) throw new IllegalArgumentException("Entity must not be null");
        String sql = "INSERT INTO cards (id, card_name, target_type) VALUES (?, ?, ?::duck_type)";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, entity.getID());
            statement.setString(2, entity.getCardName());
            statement.setString(3, entity.getTargetType().name());
            statement.executeUpdate();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
    }

    @Override
    public DuckCard delete(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        DuckCard toDelete = findOne(id);
        if (toDelete == null) return null;
        String sql = "DELETE FROM cards WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
            return toDelete;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public DuckCard update(DuckCard entity) {
        if (entity == null) throw new IllegalArgumentException("Entity must not be null");
        String sql = "UPDATE cards SET card_name = ?, target_type = ?::duck_type WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getCardName());
            statement.setString(2, entity.getTargetType().name());
            statement.setLong(3, entity.getID());
            int affected = statement.executeUpdate();
            return affected > 0 ? null : entity;
        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
    }
}