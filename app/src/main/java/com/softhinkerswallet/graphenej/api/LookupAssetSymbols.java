package com.softhinkerswallet.graphenej.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.softhinkerswallet.graphenej.Asset;
import com.softhinkerswallet.graphenej.RPC;
import com.softhinkerswallet.graphenej.models.ApiCall;
import com.softhinkerswallet.graphenej.models.WitnessResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.softhinkerswallet.graphenej.interfaces.WitnessResponseListener;

/**
 * Created by nelson on 12/12/16.
 */
public class LookupAssetSymbols extends WebSocketAdapter {
    private WitnessResponseListener mListener;
    private List<Asset> assets;

    public LookupAssetSymbols(List<Asset> assets, WitnessResponseListener listener){
        this.assets = assets;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> params = new ArrayList<>();
        ArrayList<String> subArray = new ArrayList<>();
        for(Asset asset : this.assets){
            subArray.add(asset.getObjectId());
            params.add(subArray);
        }
        ApiCall loginCall = new ApiCall(0, RPC.CALL_LOOKUP_ASSET_SYMBOLS, params, RPC.VERSION, 0);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        System.out.println("<<< "+response);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Type LookupAssetSymbolsResponse = new TypeToken<WitnessResponse<List<Asset>>>(){}.getType();
        gsonBuilder.registerTypeAdapter(Asset.class, new Asset.AssetDeserializer());
        WitnessResponse<List<Asset>> witnessResponse = gsonBuilder.create().fromJson(response, LookupAssetSymbolsResponse);
        mListener.onSuccess(witnessResponse);
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            System.out.println(">>> "+frame.getPayloadText());
    }
}
