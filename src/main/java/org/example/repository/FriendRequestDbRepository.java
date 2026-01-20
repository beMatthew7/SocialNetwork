package org.example.repository;
import org.example.domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class FriendRequestDbRepository implements FriendRequestRepository {
    private String url;
    private String username;
    private String password;
    private Repository<Long, Person> personRepo;
    private DuckRepo duckRepo;
    public FriendRequestDbRepository(String url, String username, String password, Repository<Long, Person> personRepo, DuckRepo duckRepo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.personRepo = personRepo;
        this.duckRepo = duckRepo;
    }
    private User findUser(Long id) {
        User u = personRepo.findOne(id);
        if (u == null) u = duckRepo.findOne(id);
        return u;
    }
    @Override
    public FriendRequest findOne(Long id) {
        String sql = "SELECT * FROM friend_requests WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFriendRequest(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private FriendRequest extractFriendRequest(ResultSet rs) throws SQLException {
        User from = findUser(rs.getLong("from_user_id"));
        User to = findUser(rs.getLong("to_user_id"));
        RequestStatus status = RequestStatus.valueOf(rs.getString("status"));
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        FriendRequest fr = new FriendRequest(from, to, status, date);
        fr.setID(rs.getLong("id"));
        return fr;
    }

    public FriendRequest findOneByUsers(Long fromId, Long toId) {
        String sql = "SELECT * FROM friend_requests WHERE from_user_id = ? AND to_user_id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, fromId);
            ps.setLong(2, toId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractFriendRequest(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    @Override
    public Iterable<FriendRequest> findAll() {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                requests.add(extractFriendRequest(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return requests;
    }
    @Override
    public FriendRequest save(FriendRequest entity) {
        String sql = "INSERT INTO friend_requests (from_user_id, to_user_id, status, date) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, entity.getFrom().getID());
            ps.setLong(2, entity.getTo().getID());
            ps.setString(3, entity.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    @Override
    public FriendRequest delete(Long id) {
        return null;
    }
    @Override
    public FriendRequest update(FriendRequest entity) {
        String sql = "UPDATE friend_requests SET status = ?, date = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getStatus().name());
            ps.setTimestamp(2, Timestamp.valueOf(entity.getDate()));
            ps.setLong(3, entity.getID());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<FriendRequest> getRequestForUser(Long userId) {
        /*Intoarcem toate cereriel de prieteni date de un user
         * param: userId - id ul userului pentru care cautam cererile
         * return: Lista cu RequestStatus
         * */
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests WHERE from_user_id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(extractFriendRequest(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return requests;
    }
    @Override
    public List<FriendRequest> getRequestsToUser(Long userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM friend_requests WHERE to_user_id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(extractFriendRequest(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return requests;
    }

    private ResultSet executeQueryOneId(String sql, Long id) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return rs;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public FriendRequest findOneByUsersPending(Long fromId, Long toId) {
        String sql = "SELECT * FROM friend_requests WHERE from_user_id = ? AND to_user_id = ? AND status = 'PENDING'";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, fromId);
            ps.setLong(2, toId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractFriendRequest(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}