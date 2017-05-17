package com.softhinkers_wallet.interfaces;

/**
 * Interface that must be implemented by any party that desires to be notified of the
 * 'lock release' event. This is defined as the event that takes place when the user
 * successfully releases the app lock by entering a correct pin number.
 *
 * Created by adarsh on 05/06/17.
 */
public interface LockListener {
    void onLockReleased();
}
