package com.softhinkerswallet.graphenej.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.softhinkerswallet.graphenej.Asset;
import com.softhinkerswallet.graphenej.AssetAmount;
import com.softhinkerswallet.graphenej.BaseOperation;
import com.softhinkerswallet.graphenej.RPC;
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
public class GetRequiredFees extends WebSocketAdapter {

    private WitnessResponseListener mListener;
    private List<BaseOperation> operations;
    private Asset asset;

    public GetRequiredFees(List<BaseOperation> operations, Asset asset, WitnessResponseListener listener){
        this.operations = operations;
        this.asset = asset;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        accountParams.add((Serializable) this.operations);
        accountParams.add(this.asset.getObjectId());
        ApiCall getRequiredFees = new ApiCall(0, RPC.CALL_GET_REQUIRED_FEES, accountParams, "2.0", 1);
        websocket.sendText(getRequiredFees.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        try{
        String response = frame.getPayloadText();
        Gson gson = new Gson();

        Type GetRequiredFeesResponse = new TypeToken<WitnessResponse<List<AssetAmount>>>(){}.getType();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetDeserializer());
        WitnessResponse<List<AssetAmount>> witnessResponse = gsonBuilder.create().fromJson(response, GetRequiredFeesResponse);

        if(witnessResponse.error != null){
            mListener.onError(witnessResponse.error);
        }else{
            mListener.onSuccess(witnessResponse);
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
