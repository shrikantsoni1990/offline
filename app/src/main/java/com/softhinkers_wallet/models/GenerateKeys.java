package com.softhinkers_wallet.models;

import com.google.gson.Gson;

/**
 * Created by adarsh on 05/07/17.
 */
public class GenerateKeys {
    public Keys keys;
    public String status;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
