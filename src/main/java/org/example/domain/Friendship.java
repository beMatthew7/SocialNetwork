package org.example.domain;

public class Friendship extends Entity<Long> {
    private Long userId1;
    private Long userId2;
    private long createdAt;

    public Friendship(Long id, Long userId1, Long userId2, long createdAt) {
        this.setID(id);
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.createdAt = createdAt;
    }

    public Long getId() { return getID(); }
    public Long getUserId1() { return userId1; }
    public Long getUserId2() { return userId2; }
    public long getCreatedAt() { return createdAt; }
}
