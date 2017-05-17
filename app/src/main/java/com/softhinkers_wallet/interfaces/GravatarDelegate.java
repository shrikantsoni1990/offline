package com.softhinkers_wallet.interfaces;

import android.graphics.Bitmap;

import com.softhinkers_wallet.models.Gravatar;


/**
 * Created by adarsh on 05/08/17.
 */
public interface GravatarDelegate {
    void updateProfile(Gravatar myGravatar);

    void updateCompanyLogo(Bitmap logo);

    void failureUpdateProfile();

    void failureUpdateLogo();
}
