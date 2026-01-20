package org.example.repository;

import org.example.domain.Person;
import org.example.domain.validators.Validator;
import org.example.domain.validators.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Repository pentru entitati Person, persistat in baza de date PostgreSQL.
 * Implementeaza aceeasi interfata Repository ca si PersonFileRepository.
 */
public class PersonDbRepository implements Repository<Long, Person> {

    private String dbUrl;
    private String username;
    private String password;
    private Validator<Person> validator;

    /**
     * Creeaza repository-ul pentru Person.
     * @param dbUrl URL-ul bazei de date
     * @param username utilizatorul bazei de date
     * @param password parola bazei de date
     * @param validator validatorul entitatii Person
     */
    public PersonDbRepository(String dbUrl, String username, String password, Validator<Person> validator) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    /**
     * Metoda ajutatoare pentru a extrage un Person dintr-un ResultSet.
     */
    private Person extractPersonFromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String pass = rs.getString("password");
        String firstName = rs.getString("first_name");
        String secondName = rs.getString("second_name");


        Date dateOfBirth = rs.getDate("date_of_birth");

        String occupation = rs.getString("occupation");
        int empathyNivel = rs.getInt("empathy_nivel");

        Person person = new Person(username, email, pass, firstName,
                secondName, dateOfBirth, occupation, empathyNivel);
        person.setID(id);
        return person;
    }

    @Override
    public Person findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null");
        }
        String sql = "SELECT * FROM people INNER JOIN users ON people.id = users.id WHERE people.id = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extractPersonFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Person> findAll() {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM people INNER JOIN users ON people.id = users.id";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                persons.add(extractPersonFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return persons;
    }

    @Override
    public Person save(Person entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null");
        }
        try {
            validator.validate(entity);
        } catch (ValidationException e) {
            throw e;
        }

        String sqlUsers = "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)";
        String sqlPeople = "INSERT INTO people (id, first_name, second_name, date_of_birth, occupation, empathy_nivel) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmtUsers = connection.prepareStatement(sqlUsers);
                 PreparedStatement stmtPeople = connection.prepareStatement(sqlPeople)) {

                // Insert into users
                stmtUsers.setLong(1, entity.getID());
                stmtUsers.setString(2, entity.getUsername());
                stmtUsers.setString(3, entity.getEmail());
                stmtUsers.setString(4, entity.getPassword());
                stmtUsers.executeUpdate();

                // Insert into people
                stmtPeople.setLong(1, entity.getID());
                stmtPeople.setString(2, entity.getFirstName());
                stmtPeople.setString(3, entity.getSecondName());
                stmtPeople.setDate(4, new java.sql.Date(entity.getDateOfBirth().getTime()));
                stmtPeople.setString(5, entity.getOccupation());
                stmtPeople.setInt(6, entity.getEmpathyNivel());
                stmtPeople.executeUpdate();

                connection.commit(); // Commit transaction
                return null;

            } catch (SQLException e) {
                connection.rollback(); // Rollback on error
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
    public Person delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null");
        }

        Person personToDelete = findOne(id);
        if (personToDelete == null) {
            return null;
        }

        // Deleting from users will cascade delete from people
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            int rowsAffected = statement.executeUpdate();

            return (rowsAffected > 0) ? personToDelete : null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Person update(Person entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null");
        }
        try {
            validator.validate(entity);
        } catch (ValidationException e) {
            throw e;
        }

        String sqlUsers = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
        String sqlPeople = "UPDATE people SET first_name = ?, second_name = ?, date_of_birth = ?, " +
                "occupation = ?, empathy_nivel = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            connection.setAutoCommit(false);

            try (PreparedStatement stmtUsers = connection.prepareStatement(sqlUsers);
                 PreparedStatement stmtPeople = connection.prepareStatement(sqlPeople)) {

                // Update users
                stmtUsers.setString(1, entity.getUsername());
                stmtUsers.setString(2, entity.getEmail());
                stmtUsers.setString(3, entity.getPassword());
                stmtUsers.setLong(4, entity.getID());
                int rowsUsers = stmtUsers.executeUpdate();

                // Update people
                stmtPeople.setString(1, entity.getFirstName());
                stmtPeople.setString(2, entity.getSecondName());
                stmtPeople.setDate(3, new java.sql.Date(entity.getDateOfBirth().getTime()));
                stmtPeople.setString(4, entity.getOccupation());
                stmtPeople.setInt(5, entity.getEmpathyNivel());
                stmtPeople.setLong(6, entity.getID());
                int rowsPeople = stmtPeople.executeUpdate();

                if (rowsUsers > 0 || rowsPeople > 0) {
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
}