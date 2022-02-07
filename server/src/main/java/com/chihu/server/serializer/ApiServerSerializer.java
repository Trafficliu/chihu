package com.chihu.server.serializer;

import com.chihu.lib.serialize.Serializer;
import com.chihu.server.model.*;
import com.google.gson.Gson;

public class ApiServerSerializer {

    private static final Gson GSON = Serializer.defaultInstance();
    private static final Gson GSON_PRETTY_PRINT = Serializer.defaultInstancePrettyPrint();

    public static User toUser(String userStr) {
        return GSON.fromJson(userStr, User.class);
    }

    public static BusinessGroup toBusinessGroup(String businessGroupStr) {
        return GSON.fromJson(businessGroupStr, BusinessGroup.class);
    }

    public static BusinessEntity toBusinessEntity(String businessEntityStr) {
        return GSON.fromJson(businessEntityStr, BusinessEntity.class);
    }

    public static Dish toDish(String dishStr) {
        return GSON.fromJson(dishStr, Dish.class);
    }

    public static Address toAddress(String addressStr) {
        return GSON.fromJson(addressStr, Address.class);
    }

    public static String serialize(Object object) {
        return GSON.toJson(object);
    }

    public static String prettyPrint(Object object) {
        return GSON_PRETTY_PRINT.toJson(object);
    }

    public static Gson getGsonInstance() {
        return GSON;
    }
}
