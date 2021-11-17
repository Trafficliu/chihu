package com.chihu.server.serializer;

import com.chihu.lib.serialize.Serializer;
import com.chihu.server.model.User;
import com.google.gson.Gson;

public class ApiServerSerializer {

    private static final Gson GSON = Serializer.defaultInstance();
    private static final Gson GSON_PRETTY_PRINT = Serializer.defaultInstancePrettyPrint();

    public static User toUser(String userStr) {
        return GSON.fromJson(userStr, User.class);
    }

    public static String prettyPrint(Object object) {
        return GSON_PRETTY_PRINT.toJson(object);
    }
}
