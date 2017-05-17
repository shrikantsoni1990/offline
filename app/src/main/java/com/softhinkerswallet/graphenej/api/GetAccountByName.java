package com.softhinkerswallet.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.softhinkerswallet.graphenej.AccountOptions;
import com.softhinkerswallet.graphenej.Authority;
import com.softhinkerswallet.graphenej.RPC;
import com.softhinkerswallet.graphenej.interfaces.WitnessResponseListener;
import com.softhinkerswallet.graphenej.models.AccountProperties;
import com.softhinkerswallet.graphenej.models.ApiCall;
import com.softhinkerswallet.graphenej.models.BaseResponse;
import com.softhinkerswallet.graphenej.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nelson on 11/15/16.
 */
public class GetAccountByName extends WebSocketAdapter {

    private String accountName;
    private WitnessResponseListener mListener;

    public GetAccountByName(String accountName, WitnessResponseListener listener){
        this.accountName = accountName;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_GET_ACCOUNT_BY_NAME, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        try {
            String response = frame.getPayloadText();
            GsonBuilder builder = new GsonBuilder();

            Type GetAccountByNameResponse = new TypeToken<WitnessResponse<AccountProperties>>() {
            }.getType();
            builder.registerTypeAdapter(Authority.class, new Authority.AuthorityDeserializer());
            builder.registerTypeAdapter(AccountOptions.class, new AccountOptions.AccountOptionsDeserializer());
            WitnessResponse<AccountProperties> witnessResponse = builder.create().fromJson(response, GetAccountByNameResponse);

            if (witnessResponse.error != null) {
                this.mListener.onError(witnessResponse.error);
            } else {
                this.mListener.onSuccess(witnessResponse);
            }
        }catch(Exception e){}

        websocket.disconnect();
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}
