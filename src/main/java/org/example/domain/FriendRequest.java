package org.example.domain;

import org.example.service.FriendRequestService;
import org.example.service.FriendshipService;

import java.time.LocalDateTime;

public class FriendRequest extends Entity<Long>{
    private User from;
    private User to;
    private RequestStatus status = RequestStatus.PENDING;
    private LocalDateTime date = LocalDateTime.now();

    public FriendRequest(User from, User to){
        this.from = from;
        this.to = to;
    }

    public FriendRequest(User from, User to, RequestStatus status, LocalDateTime date) {
        this.from = from;
        this.to = to;
        this.status = status;
        this.date = date;
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
