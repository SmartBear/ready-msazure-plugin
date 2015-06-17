package com.smartbear.msazuresupport.entities;

import com.smartbear.msazuresupport.utils.Helper;

import javax.json.JsonObject;
import java.util.List;

public class Subscription {
    public final String id;
    public final String key;

    private final User user;
    private final Product product;

    public Subscription(String id, String key, User user, Product product) {
        this.id = id;
        this.key = key;
        this.user = user;
        this.product = product;
    }

    public Subscription(JsonObject obj, List<User> users, List<Product> products) {
        this.id = obj.getString("id", null);
        this.key = obj.getString("primaryKey", null);

        final String userId = obj.getString("userId", null);
        final String productId = obj.getString("productId", null);

        user = Helper.<User>find(users, new Helper.Predicate<User>() {
            @Override
            public boolean execute(User value) {
                return value.id.equalsIgnoreCase(userId);
            }
        });
        product = Helper.<Product>find(products, new Helper.Predicate<Product>() {
            @Override
            public boolean execute(Product value) {
                return value.id.equalsIgnoreCase(productId);
            }
        });
    }


    public boolean associatedWithApi(String id) {
        return product.apis.indexOf(id) >= 0;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", user != null ? user.name : "unknown", product != null ? product.name : "unknown");
    }
}