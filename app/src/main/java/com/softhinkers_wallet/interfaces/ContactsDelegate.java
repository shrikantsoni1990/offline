package com.softhinkers_wallet.interfaces;

import java.io.Serializable;

/**
 * Created by adarsh on 05/08/17.
 */
public interface ContactsDelegate extends Serializable {
    void OnUpdate(String s, int id);
}
