package com.softhinkers_wallet.models;

import android.content.Context;

/**
 * Created by adarsh on 05/07/17.
 */
public class transactionsJsonSerializable {
    public String id;
    public String blockNumber;
    public java.util.Date Date;
    public Boolean Sent; // false : if received
    public String To;
    public String From;
    public String Memo;
    public double Amount;
    public String assetSymbol;
    public double fiatAmount;
    public String fiatAssetSymbol;
    public String eReceipt;
    private Context context;
}
