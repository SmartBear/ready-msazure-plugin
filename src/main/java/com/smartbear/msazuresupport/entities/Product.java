package com.smartbear.msazuresupport.entities;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Product {
    public final String id;
    public final String name;
    public final boolean subscriptionRequired;
    public final List<String> apis;

    public Product(String id, String name, boolean subscriptionRequired, String... apis) {
        this.id = id;
        this.name = name;
        this.subscriptionRequired = subscriptionRequired;
        this.apis = Arrays.asList(apis);
    }

    public Product(JsonObject obj, List<String> apis) {
        this.id = obj.getString("id", null);
        this.name = obj.getString("name", null);
        this.subscriptionRequired = obj.getBoolean("subscriptionRequired", true);
        this.apis = apis;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, apis=%s", id, name, apis);
    }
}
