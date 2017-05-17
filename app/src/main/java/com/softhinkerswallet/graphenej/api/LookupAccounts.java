package com.softhinkerswallet.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.softhinkerswallet.graphenej.RPC;
import com.softhinkerswallet.graphenej.UserAccount;
import com.softhinkerswallet.graphenej.interfaces.WitnessResponseListener;
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
 * Created by henry on 07/12/16.
 */
public class LookupAccounts extends WebSocketAdapter {

    public static final int DEFAULT_MAX = 1000;
    private final String accountName;
    private int maxAccounts = DEFAULT_MAX;
    private final WitnessResponseListener mListener;

    public LookupAccounts(String accountName, WitnessResponseListener listener){
        this.accountName = accountName;
        this.maxAccounts = DEFAULT_MAX;
        this.mListener = listener;
    }

    public LookupAccounts(String accountName, int maxAccounts, WitnessResponseListener listener){
        this.accountName = accountName;
        this.maxAccounts  = maxAccounts;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add(this.accountName);
        accountParams.add(this.maxAccounts);
        ApiCall getAccountByName = new ApiCall(0, RPC.CALL_LOOKUP_ACCOUNTS, accountParams, RPC.VERSION, 1);
        websocket.sendText(getAccountByName.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        try{
            String response = frame.getPayloadText();


        Type LookupAccountsResponse = new TypeToken<WitnessResponse<List<UserAccount>>>(){}.getType();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountDeserializer());
        WitnessResponse<List<UserAccount>> witnessResponse = builder.create().fromJson(response, LookupAccountsResponse);
        if(witnessResponse.error != null){
            this.mListener.onError(witnessResponse.error);
        }else{
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