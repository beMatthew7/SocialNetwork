package org.example.repository;
import org.example.domain.Message;
import org.example.domain.User;
import org.example.repository.Repository;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
public class MessageDbRepository implements Repository<Long, Message> {
    private String url;
    private String username;
    private String password;
    private Repository<Long, org.example.domain.Person> personRepo;
    private org.example.repository.DuckRepo duckRepo;

    public MessageDbRepository(String url, String username, String password, Repository<Long, org.example.domain.Person> personRepo, org.example.repository.DuckRepo duckRepo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.personRepo = personRepo;
        this.duckRepo = duckRepo;
    }

    public List<Message> findConversation(Long user1Id, Long user2Id) {
        List<Message> messages = new ArrayList<>();
        String sql = """
            SELECT * FROM messages 
            WHERE (from_user_id = ? AND to_user_id = ?) 
               OR (from_user_id = ? AND to_user_id = ?)
            ORDER BY date ASC
        """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, user1Id);
            ps.setLong(2, user2Id);
            ps.setLong(3, user2Id);
            ps.setLong(4, user1Id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(extractMessage(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Message findOne(Long id) {
        if (id == null) return null;
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) messages.add(extractMessage(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Message save(Message entity) {
        String sql = "INSERT INTO messages (from_user_id, to_user_id, message, date, reply_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            

            ps.setLong(1, entity.getFrom().getID());
            ps.setLong(2, entity.getTo().get(0).getID());
            ps.setString(3, entity.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            
            if (entity.getReply() != null) {
                ps.setLong(5, entity.getReply().getID());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Message delete(Long aLong) { return null; }

    @Override
    public Message update(Message entity) { return null; }

    private Message extractMessage(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long fromId = rs.getLong("from_user_id");
        Long toId = rs.getLong("to_user_id");
        String text = rs.getString("message");
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        Long replyId = rs.getLong("reply_id");

        User from = personRepo.findOne(fromId);
        if (from == null) {
            from = duckRepo.findOne(fromId);
        }

        User to = personRepo.findOne(toId);
        if (to == null) {
            to = duckRepo.findOne(toId);
        }
        


        Message msg = new Message(from, Collections.singletonList(to), text);
        msg.setID(id);
        msg.setDate(date);
        
        if (replyId != 0) {
             Message replyMsg = findOne(replyId);
             msg.setReply(replyMsg);
        }
        
        return msg;
    }
}