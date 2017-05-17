package com.softhinkers_wallet.interfaces;

/**
 * Created by adarsh on 05/08/17.
 */

import com.softhinkerswallet.graphenej.UserAccount;

/**
 * Interface implemented by the class that wishes to be informed about account update
 * operation results.
 */
public interface UpdatedAccountListener {
    public static final int INITIAL = 0;
    public static final int UPDATING = 1;
    public static final int SUCCESS = 2;
    public static final int FAILURE = 3;

    /**
     * Method called upon the result of a single account update operation.
     *
     * @param account: The account that was updated
     * @param status:  The current status of the update operation.
     */
    public void onUpdateStatusChange(UserAccount account, int status);

    /**
     * Method called once the update procedure is over.
     */
    public void onUpdateFinished();
}
