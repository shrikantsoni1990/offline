package com.softhinkers_wallet.adapters;

import java.util.Comparator;

import com.softhinkers_wallet.database.HistoricalTransferEntry;

/**
 * Created by adarsh on 05/14/17.
 */

public class TransferDateComparator implements Comparator<HistoricalTransferEntry> {

    @Override
    public int compare(HistoricalTransferEntry lhs, HistoricalTransferEntry rhs) {
        return (int) (lhs.getTimestamp() - rhs.getTimestamp());
    }
}
