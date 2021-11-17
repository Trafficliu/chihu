package com.chihu.server.model;

public enum UserType {
    EATER(1),
    OWNER(2),
    ADMIN(4);

    private int id;

    private UserType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
