package com.softhinkers_wallet.interfaces;

import com.softhinkers_wallet.models.TransactionDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adarsh on 05/08/17.
 */

public interface AssetDelegate {
    void isUpdate(ArrayList<String> id, ArrayList<String> sym, ArrayList<String> pre, ArrayList<String> am);

    void isAssets();

    void TransactionUpdate(List<TransactionDetails> transactionDetails, int nos);

    void getLifetime(String s, int id);

    void loadAll();

    void transactionsLoadComplete(List<TransactionDetails> transactionDetails, int newTransactionsLoaded);

    void transactionsLoadMessageStatus(String message);

    void transactionsLoadFailure(String reason);

    void loadAgain();
}
