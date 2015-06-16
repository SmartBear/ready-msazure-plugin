package com.smartbear.msazuresupport.utils;

import com.smartbear.msazuresupport.Strings;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Helper {
    private Helper() {
    }

    public interface Predicate<T> {
        public boolean execute(T value);
    }

    public static <T> T find(List<? extends T> list, Predicate<? super T> match) {
        for (T elem: list) {
            if (match.execute(elem))
                return elem;
        }
        return null;
    }


    public interface EntityFactory<T> {
        public T create(JsonObject value);
    }

    public static <T> List<T> extractList(JsonObject obj, EntityFactory<T> factory) throws IOException {
        JsonValue value = obj.get("value");
        if (value == null || !(value instanceof JsonArray)) {
            throw new IOException(Strings.AzureRestApi.VALUE_ENTRY_ABSENT_ERROR);
        }

        ArrayList<T> result = new ArrayList<>();

        JsonArray productsArray = (JsonArray) value;
        for (javax.json.JsonValue it : productsArray) {
            if (it instanceof JsonObject) {
                T item = factory.create((JsonObject) it);
                result.add(item);
            }
        }

        return result;
    }
}
