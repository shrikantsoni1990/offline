package com.softhinkerswallet.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.softhinkerswallet.graphenej.Address;
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
 * Created by nelson on 11/15/16.
 */
public class GetAccountsByAddress extends WebSocketAdapter {

    private Address address;
    private WitnessResponseListener mListener;

    public GetAccountsByAddress(Address address, WitnessResponseListener listener) {
        this.address = address;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList();
        ArrayList<Serializable> addresses = new ArrayList();
        addresses.add(address.toString());
        params.add(addresses);
        ApiCall getAccountByAddress = new ApiCall(0, RPC.CALL_GET_KEY_REFERENCES, params, RPC.VERSION, 1);
        websocket.sendText(getAccountByAddress.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        System.out.println("<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        GsonBuilder builder = new GsonBuilder();

        Type GetAccountByAddressResponse = new TypeToken<WitnessResponse<List<List<UserAccount>>>>(){}.getType();
        builder.registerTypeAdapter(UserAccount.class, new UserAccount.UserAccountSimpleDeserializer());
        WitnessResponse<List<List<UserAccount>>> witnessResponse = builder.create().fromJson(response, GetAccountByAddressResponse);
        if (witnessResponse.error != null) {
            this.mListener.onError(witnessResponse.error);
        } else {
            this.mListener.onSuccess(witnessResponse);
        }
        websocket.disconnect();
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
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