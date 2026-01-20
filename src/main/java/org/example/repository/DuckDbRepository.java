package org.example.repository;

import org.example.domain.*;
import org.example.domain.validators.Validator;
import org.example.domain.validators.ValidationException;
import org.example.paging.Page;
import org.example.paging.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pentru entitati Duck, persistat in baza de date PostgreSQL.
 * Implementeaza aceeasi interfata Repository ca si DuckFileRepository.
 */
public class DuckDbRepository implements DuckRepo, PagingRepository<Long,Duck> {

    private String dbUrl;
    private String username;
    private String password;
    private Validator<Duck> validator;



    /**
     * Creeaza repository-ul pentru Duck.
     * @param dbUrl URL-ul bazei de date (ex. "jdbc:postgresql://localhost:5432/dbname")
     * @param username utilizatorul bazei de date
     * @param password parola bazei de date
     * @param validator validatorul entitatii Duck
     */
    public DuckDbRepository(String dbUrl, String username, String password, Validator<Duck> validator) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * Metoda ajutatoare pentru a extrage un Duck dintr-un ResultSet.
     * Se ocupa de maparea coloanelor si de recrearea tipului corect de rata.
     */
    private Duck extractDuckFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String pass = rs.getString("password");
        DuckType type = DuckType.valueOf(rs.getString("type"));
        double speed = rs.getDouble("speed");
        double endurance = rs.getDouble("endurance");
        // long cardId = rs.getLong("card_id"); // O putem citi daca e nevoie

        Duck duck;

        if (type == DuckType.FLYING) {
            duck = new FlyingDuck(username, email, pass, type, speed, endurance);
        } else if (type == DuckType.SWIMMING) {
            duck = new SwimmingDuck(username, email, pass, type, speed, endurance);
        } else { // FLYING_AND_SWIMMING
            duck = new FlyingSwimmingDuck(username, email, pass, type, speed, endurance);
        }
        duck.setID(id);

        return duck;
    }


    @Override
    public Duck findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null");
        }

        String sql = "SELECT * FROM ducks INNER JOIN users ON ducks.id = users.id WHERE ducks.id = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extractDuckFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Duck> findAll() {
        List<Duck> ducks = new ArrayList<>();
        String sql = "SELECT * FROM ducks INNER JOIN users ON ducks.id = users.id";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                ducks.add(extractDuckFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ducks;
    }

    @Override
    public Duck save(Duck entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null");
        }
        try {
            validator.validate(entity);
        } catch (ValidationException e) {
            throw e;
        }


        String sqlUsers = "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)";
        String sqlDucks = "INSERT INTO ducks (id, type, speed, endurance, card_id) VALUES (?, ?::duck_type, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtUsers = connection.prepareStatement(sqlUsers);
                 PreparedStatement stmtDucks = connection.prepareStatement(sqlDucks)) {

                // Insert into users
                stmtUsers.setLong(1, entity.getID());
                stmtUsers.setString(2, entity.getUsername());
                stmtUsers.setString(3, entity.getEmail());
                stmtUsers.setString(4, entity.getPassword());
                stmtUsers.executeUpdate();

                // Insert into ducks
                stmtDucks.setLong(1, entity.getID());
                stmtDucks.setString(2, entity.getType().name());
                stmtDucks.setDouble(3, entity.getSpeed());
                stmtDucks.setDouble(4, entity.getEndurance());
                if (entity.getCard() != null && entity.getCard().getID() != null) {
                    stmtDucks.setObject(5, entity.getCard().getID());
                } else {
                    stmtDucks.setNull(5, Types.BIGINT);
                }
                stmtDucks.executeUpdate();

                connection.commit();
                return null;

            } catch (SQLException e) {
                connection.rollback();
                if (e.getSQLState().equals("23505")) {
                    return entity;
                }
                e.printStackTrace();
                return entity;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
    }

    @Override
    public Duck delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null");
        }

        Duck duckToDelete = findOne(id);
        if (duckToDelete == null) {
            return null;
        }

        // Delete from users, cascade will handle ducks
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();

            return (rowsAffected > 0) ? duckToDelete : null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Duck update(Duck entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null");
        }
        try {
            validator.validate(entity);
        } catch (ValidationException e) {
            throw e;
        }

        String sqlUsers = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
        String sqlDucks = "UPDATE ducks SET type = ?::duck_type, speed = ?, endurance = ?, card_id = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtUsers = connection.prepareStatement(sqlUsers);
                 PreparedStatement stmtDucks = connection.prepareStatement(sqlDucks)) {

                // Update users
                stmtUsers.setString(1, entity.getUsername());
                stmtUsers.setString(2, entity.getEmail());
                stmtUsers.setString(3, entity.getPassword());
                stmtUsers.setLong(4, entity.getID());
                int rowsUsers = stmtUsers.executeUpdate();

                // Update ducks
                stmtDucks.setString(1, entity.getType().name());
                stmtDucks.setDouble(2, entity.getSpeed());
                stmtDucks.setDouble(3, entity.getEndurance());
                if (entity.getCard() != null && entity.getCard().getID() != null) {
                    stmtDucks.setObject(4, entity.getCard().getID());
                } else {
                    stmtDucks.setNull(4, Types.BIGINT);
                }
                stmtDucks.setLong(5, entity.getID());
                int rowsDucks = stmtDucks.executeUpdate();

                if (rowsUsers > 0 || rowsDucks > 0) {
                    connection.commit();
                    return null;
                } else {
                    connection.rollback();
                    return entity;
                }

            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return entity;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
    }

    public List<Duck> findByCardId(Long cardId) {
        List<Duck> ducks = new ArrayList<>();
        String sql = "SELECT * FROM ducks INNER JOIN users ON ducks.id = users.id WHERE card_id = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, cardId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ducks.add(extractDuckFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ducks;
    }
    @Override
    public Iterable<Duck> findByType(DuckType type) {
        List<Duck> ducks = new ArrayList<>();
        String sql = "SELECT * FROM ducks INNER JOIN users ON ducks.id = users.id WHERE type = ?::duck_type";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ducks.add(extractDuckFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ducks;
    }

    @Override
    public Page<Duck> findAllOnPage(Pageable pageable) {
        List<Duck> ducks = new ArrayList<>();

        int limit = pageable.getPageSize();
        int offset = (pageable.getPageNumber() - 1) * limit;

        String sql = "SELECT * FROM ducks INNER JOIN users ON ducks.id = users.id LIMIT ? OFFSET ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            statement.setInt(2, offset);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ducks.add(extractDuckFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Page<>(ducks, countAll());
    }

    private int countAll() {
        String sql = "SELECT COUNT(*) AS count FROM ducks";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    @Override
    public Page<Duck> findAllOnPage(Pageable pageable, DuckType type) {
        List<Duck> ducks = new ArrayList<>();

        int limit = pageable.getPageSize();
        int offset = (pageable.getPageNumber() - 1) * limit;

        String sql = "SELECT * FROM ducks INNER JOIN users ON ducks.id = users.id WHERE type = ?::duck_type LIMIT ? OFFSET ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type.name());
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                ducks.add(extractDuckFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new Page<>(ducks, countByType(type));
    }

    private int countByType(DuckType type) {
        String sql = "SELECT COUNT(*) AS count FROM ducks WHERE type = ?::duck_type";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type.name());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}