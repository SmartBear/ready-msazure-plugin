package com.smartbear.msazuresupport.entities;

import com.eviware.soapui.support.StringUtils;

import javax.json.JsonObject;

public class ApiInfo {
    public final String name;
    public final String description;
    public final String path;
    public final String id;
    private String subscriptionKey;

    public ApiInfo(String name, String description, String path, String id) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.id = id;
        this.subscriptionKey = "";
    }

    public ApiInfo(JsonObject obj) {
        this.name = obj.getString("name", null);
        this.description = obj.getString("description", null);
        this.path = obj.getString("path", null);
        this.id = obj.getString("id", null);
        this.subscriptionKey = "";
    }

    public Boolean isValid() {
        return StringUtils.hasContent(name) && StringUtils.hasContent(path) && StringUtils.hasContent(id);
    }

    @Override
    public String toString() {
        return String.format("name = %s, path = %s, id = %s", name, path, id);
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }
}
