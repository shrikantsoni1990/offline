package com.softhinkers_wallet.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adarsh on 05/08/17.
 */
public interface IExchangeRate {
    void callback_exchange_rate(JSONObject obj, int id) throws JSONException;
}
