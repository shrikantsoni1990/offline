package com.softhinkers_wallet.models;

import com.google.gson.Gson;

/**
 * Created by adarsh on 05/07/17.
 */
public class Keys {
    public String wif_priv_key;
    public String pub_key;
    public String brain_priv_key;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
