package org.example.repository;

import org.example.domain.FriendRequest;

import java.util.List;

public interface FriendRequestRepository extends Repository<Long, FriendRequest> {
    FriendRequest findOneByUsers(Long fromId, Long toId);
    List<FriendRequest> getRequestForUser(Long userId);
    List<FriendRequest> getRequestsToUser(Long userId);
    FriendRequest findOneByUsersPending(Long fromId, Long toId);
}
