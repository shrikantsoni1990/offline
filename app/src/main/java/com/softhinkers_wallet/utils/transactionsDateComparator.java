package com.softhinkers_wallet.utils;

import com.softhinkers_wallet.models.TransactionDetails;

import java.util.Comparator;

/**
 * Created by developer on 05/03/17.
 */
public class transactionsDateComparator implements Comparator<TransactionDetails> {
    @Override
    public int compare(TransactionDetails o1, TransactionDetails o2) {
        return o2.Date.compareTo(o1.Date);
    }
}
