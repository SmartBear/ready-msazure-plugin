package com.smartbear.msazuresupport.entities;

import com.smartbear.msazuresupport.utils.Helper;

import javax.json.JsonObject;
import java.util.List;

public class Subscription {
    public enum KeyKind {
        PRIMARY,
        SECONDARY;

        @Override
        public String toString() {
            switch (this) {
                case PRIMARY: return "Primary";
                case SECONDARY: return "Secondary";
            }
            throw new AssertionError("Unexpected enumeration: " + this);
        }

        public String getKeyName() {
            switch (this) {
                case PRIMARY: return "primaryKey";
                case SECONDARY: return "secondaryKey";
            }
            throw new AssertionError("Unexpected enumeration: " + this);
        }
    }

    private final static String ACTIVE_STATE = "active";

    public final String id;
    public final String key;

    private final KeyKind keyKind;
    private final User user;
    private final Product product;
    private final String state;

    public Subscription(String id, String key, KeyKind keyKind, User user, Product product) {
        this.id = id;
        this.key = key;
        this.keyKind = keyKind;
        this.user = user;
        this.product = product;
        this.state = ACTIVE_STATE;
    }

    public Subscription(JsonObject obj, KeyKind keyKind, List<User> users, List<Product> products) {
        this.id = obj.getString("id", null);
        this.keyKind = keyKind;
        this.key = obj.getString(this.keyKind.getKeyName(), null);
        this.state = obj.getString("state", ACTIVE_STATE);

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
        return this.state.equalsIgnoreCase(ACTIVE_STATE) && product.apis.indexOf(id) >= 0;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] [%s]", user != null ? user.name : "unknown", product != null ? product.name : "unknown", keyKind);
    }
}