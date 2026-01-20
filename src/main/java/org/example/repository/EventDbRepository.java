package org.example.repository;

import org.example.domain.*;
import org.example.interactions.Event;
import org.example.interactions.RaceEvent;
import org.example.domain.validators.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDbRepository implements Repository<Long, Event> {
    private String dbUrl;
    private String username;
    private String password;
    private Repository<Long, Person> personRepo;
    private Repository<Long, Duck> duckRepo;

    public EventDbRepository(String dbUrl, String username, String password,
                             Repository<Long, Person> personRepo, Repository<Long, Duck> duckRepo) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.personRepo = personRepo;
        this.duckRepo = duckRepo;
    }

    @Override
    public Event findOne(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");

        Event event = null;
        String sql = "SELECT * FROM events WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("event_type");
                String creatorIdStr = rs.getString("creator_id");
                User organizer = null;
                if (creatorIdStr != null) {
                    Long creatorId = Long.parseLong(creatorIdStr);
                    organizer = personRepo.findOne(creatorId);
                    if (organizer == null) {
                        organizer = duckRepo.findOne(creatorId);
                    }
                }

                if ("RaceEvent".equals(type)) {
                    RaceEvent raceEvent = new RaceEvent(name, organizer);
                    raceEvent.setID(id);
                    loadRaceDetails(raceEvent, connection);
                    event = raceEvent;
                } else {
                    // Aici poti trata si alte tipuri de evenimente
                }

                if (event != null) {
                    loadSubscribers(event, connection);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return event;
    }

    private void loadRaceDetails(RaceEvent event, Connection conn) throws SQLException {
        String sqlLanes = "SELECT * FROM event_lanes WHERE event_id = ? ORDER BY lane_index";
        try (PreparedStatement ps = conn.prepareStatement(sqlLanes)) {
            ps.setLong(1, event.getID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                event.addLane(new Lane(rs.getInt("length")));
            }
        }

        // Load Participants
        String sqlParts = "SELECT duck_id FROM event_participants WHERE event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlParts)) {
            ps.setLong(1, event.getID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Long duckId = rs.getLong("duck_id");
                Duck duck = duckRepo.findOne(duckId);
                if (duck != null) {
                    event.addParticipant(duck);
                }
            }
        }
    }

    private void loadSubscribers(Event event, Connection conn) throws SQLException {
        String sql = "SELECT user_id FROM event_subscribers WHERE event_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, event.getID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                User user = personRepo.findOne(userId);
                if (user == null) {
                    user = duckRepo.findOne(userId);
                }
                if (user != null) {
                    event.subscribe(user);
                }
            }
        }
    }

    @Override
    public Iterable<Event> findAll() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id FROM events";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                events.add(findOne(rs.getLong("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public Event save(Event entity) {
        if (entity == null) throw new IllegalArgumentException("Entity must not be null");

        String sql = "INSERT INTO events (id, name, event_type, creator_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
        if (entity.getID() == null) {
            entity.setID(getNextId(connection));
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, entity.getID());
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getClass().getSimpleName());// "RaceEvent"
            if (entity.getOrganizer() != null) {
                ps.setLong(4, entity.getOrganizer().getID());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.executeUpdate();
        }

        if (entity instanceof RaceEvent) {
            saveRaceDetails((RaceEvent) entity, connection);
        }


        saveSubscribers(entity, connection);

        return null;
    } catch (SQLException e) {
        e.printStackTrace();
        return entity;
    }
}

private Long getNextId(Connection conn) throws SQLException {
    String sql = "SELECT MAX(id) FROM events";
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getLong(1) + 1;
        }
        return 1L;
    }
}

    @Override
    public Event update(Event entity) {
        if (entity == null) throw new IllegalArgumentException("Entity must not be null");

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            String sql = "UPDATE events SET name = ? WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, entity.getName());
                ps.setLong(2, entity.getID());
                ps.executeUpdate();
            }

            deleteDetails(entity.getID(), connection);


            if (entity instanceof RaceEvent) {
                saveRaceDetails((RaceEvent) entity, connection);
            }
            saveSubscribers(entity, connection);

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
    }

    @Override
    public Event delete(Long id) {
        Event toDelete = findOne(id);
        if (toDelete == null) return null;

        String sql = "DELETE FROM events WHERE id = ?";
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

    private void deleteDetails(Long eventId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event_lanes WHERE event_id=?")) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event_participants WHERE event_id=?")) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event_subscribers WHERE event_id=?")) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        }
    }

    private void saveRaceDetails(RaceEvent event, Connection conn) throws SQLException {
        // Save Lanes
        String sqlLane = "INSERT INTO event_lanes (event_id, lane_index, length) VALUES (?, ?, ?)";
        List<Lane> lanes = event.getLanes();
        try (PreparedStatement ps = conn.prepareStatement(sqlLane)) {
            for (int i = 0; i < lanes.size(); i++) {
                ps.setLong(1, event.getID());
                ps.setInt(2, i);
                ps.setInt(3, lanes.get(i).getDistanta());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        // Save Participants
        String sqlPart = "INSERT INTO event_participants (event_id, duck_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlPart)) {
            for (Duck d : event.getParticipant()) {
                ps.setLong(1, event.getID());
                ps.setLong(2, d.getID());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveSubscribers(Event event, Connection conn) throws SQLException {
        String sql = "INSERT INTO event_subscribers (event_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (User u : event.getSubscribers()) {
                ps.setLong(1, event.getID());
                ps.setLong(2, u.getID());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}