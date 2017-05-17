package com.softhinkers_wallet.models;

/**
 * Created by adarsh on 05/04/17.
 */
public interface BalanceItemsListener {
    public void onNewBalanceItem(BalanceItemsEvent event);

    public void onBalanceItemRemoved(BalanceItemsEvent event);

    public void onBalanceItemUpdated(BalanceItemsEvent event);
}
