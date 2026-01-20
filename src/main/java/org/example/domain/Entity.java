package org.example.domain;

public abstract class Entity<ID> {
    private ID id;

    public ID getID() {
        return id;
    }

    public void setID(ID id) {
        this.id = id;
    }
}



