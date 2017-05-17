package com.softhinkers_wallet.smartcoinswallet;

import com.softhinkerswallet.graphenej.BrainKey;
import com.softhinkerswallet.graphenej.UserAccount;

/**
 * Created by adarsh on 05/03/17.
 */
public class UpdateAccountTask {
    private BrainKey brainKey;
    private UserAccount account;
    private boolean updateOwner;
    private boolean updateMemo;

    UpdateAccountTask(UserAccount account, BrainKey brainKey) {
        this.account = account;
        this.brainKey = brainKey;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public boolean isUpdateOwner() {
        return updateOwner;
    }

    public void setUpdateOwner(boolean updateOwner) {
        this.updateOwner = updateOwner;
    }

    public boolean isUpdateMemo() {
        return updateMemo;
    }

    public void setUpdateMemo(boolean updateMemo) {
        this.updateMemo = updateMemo;
    }

    public BrainKey getBrainKey() {
        return brainKey;
    }
}
