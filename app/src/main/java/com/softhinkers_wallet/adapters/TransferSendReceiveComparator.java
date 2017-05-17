package com.softhinkers_wallet.adapters;

import com.softhinkerswallet.graphenej.TransferOperation;

import java.util.Comparator;

import com.softhinkers_wallet.database.HistoricalTransferEntry;
import com.softhinkerswallet.graphenej.UserAccount;

/**
 * Created by adarsh on 05/14/17.
 */

public class TransferSendReceiveComparator implements Comparator<HistoricalTransferEntry> {
    private String TAG = this.getClass().getName();

    private UserAccount me;

    public TransferSendReceiveComparator(UserAccount userAccount) {
        this.me = userAccount;
    }

    @Override
    public int compare(HistoricalTransferEntry lhs, HistoricalTransferEntry rhs) {
        TransferOperation lhsOperation = lhs.getHistoricalTransfer().getOperation();
        boolean isOutgoing = lhsOperation.getFrom().getObjectId().equals(me.getObjectId());
        if (isOutgoing)
            return -1;
        else
            return 1;
    }
}
