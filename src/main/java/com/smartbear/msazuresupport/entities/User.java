package com.smartbear.msazuresupport.entities;

import javax.json.JsonObject;

public class User {
    public final String id;
    public final String name;

    public User(JsonObject obj) {
        this.id = obj.getString("id", null);
        this.name = obj.getString("firstName", null);
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s", id, name);
    }
}
