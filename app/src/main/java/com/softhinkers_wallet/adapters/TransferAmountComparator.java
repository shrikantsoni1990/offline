package com.softhinkers_wallet.adapters;

import com.google.common.primitives.UnsignedLong;

import java.util.Comparator;

import com.softhinkers_wallet.database.HistoricalTransferEntry;

/**
 * Created by adarsh on 05/14/17.
 */

public class TransferAmountComparator implements Comparator<HistoricalTransferEntry> {

    @Override
    public int compare(HistoricalTransferEntry lhs, HistoricalTransferEntry rhs) {
        UnsignedLong lhsAmount = lhs.getHistoricalTransfer().getOperation().getTransferAmount().getAmount();
        UnsignedLong rhsAmount = rhs.getHistoricalTransfer().getOperation().getTransferAmount().getAmount();
        return lhsAmount.compareTo(rhsAmount);
    }
}
