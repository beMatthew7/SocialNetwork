package org.example.repository;

import org.example.domain.Friendship;
import java.util.List;

public interface FriendshipRepo extends Repository<Long, Friendship> {
    List<Friendship> findAllByUserId(Long userId);
    boolean areFriends(Long userId1, Long userId2);
}
