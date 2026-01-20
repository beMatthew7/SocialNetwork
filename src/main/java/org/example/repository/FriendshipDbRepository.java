package org.example.repository;

import org.example.domain.Friendship;
import org.example.domain.validators.Validator;
import org.example.domain.validators.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendshipDbRepository implements FriendshipRepo {
    private String dbUrl;
    private String username;
    private String password;
    private Validator<Friendship> validator;

    public FriendshipDbRepository(String dbUrl, String username, String password, Validator<Friendship> validator) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Friendship findOne(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        String sql = "SELECT * FROM friendships WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Long userId1 = rs.getLong("user_id1");
                Long userId2 = rs.getLong("user_id2");
                // Conversie din SQL Timestamp in long (milisecunde)
                Timestamp ts = rs.getTimestamp("created_at");
                long createdAt = ts != null ? ts.getTime() : 0L;

                Friendship f = new Friendship(id, userId1, userId2, createdAt);
                return f;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Friendship> findAll() {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Long id = rs.getLong("id");
                Long userId1 = rs.getLong("user_id1");
                Long userId2 = rs.getLong("user_id2");

                Timestamp ts = rs.getTimestamp("created_at");
                long createdAt = ts != null ? ts.getTime() : 0L;

                friendships.add(new Friendship(id, userId1, userId2, createdAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Friendship save(Friendship entity) {
        if (entity == null) throw new IllegalArgumentException("Entity must not be null");
        try {
            validator.validate(entity);
        } catch (ValidationException e) {
            throw e;
        }

        String sql = "INSERT INTO friendships (id, user_id1, user_id2, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, entity.getID());
            statement.setLong(2, entity.getUserId1());
            statement.setLong(3, entity.getUserId2());

            // Conversie din long (milisecunde) in SQL Timestamp
            statement.setTimestamp(4, new Timestamp(entity.getCreatedAt()));

            statement.executeUpdate();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
    }

    @Override
    public Friendship delete(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        Friendship toDelete = findOne(id);
        if (toDelete == null) return null;

        String sql = "DELETE FROM friendships WHERE id = ?";
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
    public Friendship update(Friendship entity) {
        return null;
    }

    @Override
    public List<Friendship> findAllByUserId(Long userId) {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships WHERE user_id1 = ? OR user_id2 = ?";
        
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    Long u1 = rs.getLong("user_id1");
                    Long u2 = rs.getLong("user_id2");
                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : 0L;
                    
                    friendships.add(new Friendship(id, u1, u2, createdAt));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }
    public List<org.example.domain.User> findFriendsForUser(Long userId) {
        List<org.example.domain.User> friends = new ArrayList<>();
        
        // Selectam tot din users (u), people (p) si ducks (d)
        // Facem JOIN cu users pe id-ul prietenului
        // Prietenul este celalalt ID din perechea (user_id1, user_id2)
        String sql = "SELECT u.*, p.*, d.*, f.created_at as friendship_created_at " +
                     "FROM friendships f " +
                     "JOIN users u ON (f.user_id1 = u.id OR f.user_id2 = u.id) " +
                     "LEFT JOIN people p ON u.id = p.id " +
                     "LEFT JOIN ducks d ON u.id = d.id " +
                     "WHERE (f.user_id1 = ? OR f.user_id2 = ?) AND u.id != ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setLong(3, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    org.example.domain.User user = extractUserFromResultSet(rs);
                    if (user != null) {
                        friends.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    private org.example.domain.User extractUserFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id"); // din users (ambiguu daca nu specificam alias, dar in SELECT * e ok de obicei, sau luam u.id)
        // ResultSet-ul va avea coloane duplicate pentru id. De obicei driverul returneaza prima gasita sau ultima.
        // Mai sigur e sa folosim alias-uri in SQL, dar "SELECT *" e mai simplu. 
        // Totusi, u.id, p.id, d.id sunt toate egale.
        
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");

        // Verificam daca e Person (are first_name nenul)
        // Coloanele din LEFT JOIN vor fi NULL daca nu exista match
        String firstName = rs.getString("first_name");
        if (firstName != null) {
            // E persoana
            String secondName = rs.getString("second_name");
            java.sql.Date dob = rs.getDate("date_of_birth");
            String occupation = rs.getString("occupation");
            int empathy = rs.getInt("empathy_nivel");
            
            org.example.domain.Person p = new org.example.domain.Person(username, email, password, firstName, secondName, dob, occupation, empathy);
            p.setID(id);
            return p;
        } 
        
        // Verificam daca e Duck (are type nenul)
        String typeStr = rs.getString("type");
        if (typeStr != null) {
            // E rata
            org.example.domain.DuckType type = org.example.domain.DuckType.valueOf(typeStr);
            double speed = rs.getDouble("speed");
            double endurance = rs.getDouble("endurance");
            
            org.example.domain.Duck d;
            if (type == org.example.domain.DuckType.FLYING) {
                d = new org.example.domain.FlyingDuck(username, email, password, type, speed, endurance);
            } else if (type == org.example.domain.DuckType.SWIMMING) {
                d = new org.example.domain.SwimmingDuck(username, email, password, type, speed, endurance);
            } else {
                d = new org.example.domain.FlyingSwimmingDuck(username, email, password, type, speed, endurance);
            }
            d.setID(id);
            return d;
        }

        return null;
    }
    @Override
    public boolean areFriends(Long id1, Long id2) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id1);
            ps.setLong(2, id2);
            ps.setLong(3, id2);
            ps.setLong(4, id1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}