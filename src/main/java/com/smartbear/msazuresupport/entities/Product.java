package com.smartbear.msazuresupport.entities;

import javax.json.JsonObject;

public class Product {
    public final String id;
    public final String name;
    public final boolean subscriptionRequired;

    public Product(JsonObject obj) {
        this.id = obj.getString("id", null);
        this.name = obj.getString("name", null);
        this.subscriptionRequired = obj.getBoolean("subscriptionRequired", true);
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s", id, name);
    }
}
