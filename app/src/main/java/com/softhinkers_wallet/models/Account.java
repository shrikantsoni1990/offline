package com.softhinkers_wallet.models;

/**
 * Created by adarsh on 05/04/17.
 */
//public class Account {
//    public String ownerKey;
//    public String name;
//    public String activeKey;
//    public String referrer;
//    public String refcode;
//    public String memoKey;
//}
public class Account {
    public Account(String string) {
        name=string;
    }
    public String ownerKey;
    public Long acountId;
    public String name;
    public String activeKey;
    public String referrer;
    public String refcode;
    public String memoKey;

}
