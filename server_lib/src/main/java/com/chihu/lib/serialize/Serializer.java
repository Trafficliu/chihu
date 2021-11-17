package com.chihu.lib.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.Date;

public class Serializer {
    private final static Gson GSON = new GsonBuilder().serializeNulls()
                                                      .setDateFormat(DateFormat.LONG)
                                                      .create();

    private final static Gson GSON_PRETTY_PRINT = new GsonBuilder().serializeNulls()
                                                                   .setDateFormat(DateFormat.LONG)
                                                                   .setPrettyPrinting()
                                                                   .create();

    public static Gson defaultInstance() {
        return GSON;
    }

    public static Gson defaultInstancePrettyPrint() {
        return GSON_PRETTY_PRINT;
    }
}
