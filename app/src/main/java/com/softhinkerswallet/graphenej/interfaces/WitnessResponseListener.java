package com.softhinkerswallet.graphenej.interfaces;

import com.softhinkerswallet.graphenej.models.BaseResponse;
import com.softhinkerswallet.graphenej.models.WitnessResponse;

/**
 * Class used to represent any listener to network requests.
 */
public interface WitnessResponseListener {

    void onSuccess(WitnessResponse response);

    void onError(BaseResponse.Error error);
}
