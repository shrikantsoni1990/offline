package com.softhinkerswallet.graphenej.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.softhinkerswallet.graphenej.models.BaseResponse;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.softhinkerswallet.graphenej.Asset;
import com.softhinkerswallet.graphenej.RPC;
import com.softhinkerswallet.graphenej.interfaces.WitnessResponseListener;
import com.softhinkerswallet.graphenej.models.ApiCall;
import com.softhinkerswallet.graphenej.models.BucketObject;
import com.softhinkerswallet.graphenej.models.WitnessResponse;

/**
 * Created by nelson on 12/22/16.
 */
public class GetMarketHistory extends WebSocketAdapter {
    private String TAG = this.getClass().getName();
    // Sequence of message ids
    private final static int LOGIN_ID = 1;
    private final static int GET_HISTORY_ID = 2;
    private final static int GET_HISTORY_DATA = 3;

    // API call parameters
    private Asset base;
    private Asset quote;
    private long bucket;
    private Date start;
    private Date end;

    // Internal attributes
    private WitnessResponseListener mListener;
    private WebSocket mWebsocket;
    private int currentId = 1;
    private int apiId = -1;
    private int counter = 0;

    public GetMarketHistory(Asset base, Asset quote, long bucket, Date start, Date end, WitnessResponseListener listener){
        this.base = base;
        this.quote = quote;
        this.bucket = bucket;
        this.start = start;
        this.end = end;
        this.mListener = listener;
    }

    public Asset getBase() {
        return base;
    }

    public void setBase(Asset base) {
        this.base = base;
    }

    public Asset getQuote() {
        return quote;
    }

    public void setQuote(Asset quote) {
        this.quote = quote;
    }

    public long getBucket() {
        return bucket;
    }

    public void setBucket(long bucket) {
        this.bucket = bucket;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getCount(){
        return this.counter;
    }

    public void disconnect(){
        if(mWebsocket != null && mWebsocket.isOpen()){
            mWebsocket.disconnect();
        }
    }

    /**
     * Retries the 'get_market_history' API call.
     * Hopefully with different 'start' and 'stop' parameters.
     */
    public void retry(){
        sendHistoricalMarketDataRequest();
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        mWebsocket = websocket;
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        String response = frame.getPayloadText();
        Log.d(TAG, "<<< "+response);
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            websocket.disconnect();
        }else{
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if(baseResponse.id == LOGIN_ID){
                ApiCall getRelativeAccountHistoryId = new ApiCall(1, RPC.CALL_HISTORY, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(getRelativeAccountHistoryId.toJsonString());
            } else if(baseResponse.id == GET_HISTORY_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                apiId = witnessResponse.result.intValue();
                sendHistoricalMarketDataRequest();
            }else if(baseResponse.id >= GET_HISTORY_DATA){
                GsonBuilder builder = new GsonBuilder();
                Type MarketHistoryResponse = new TypeToken<WitnessResponse<List<BucketObject>>>(){}.getType();
                builder.registerTypeAdapter(BucketObject.class, new BucketObject.BucketDeserializer());
                WitnessResponse<List<BucketObject>> marketHistoryResponse = builder.create().fromJson(response, MarketHistoryResponse);
                mListener.onSuccess(marketHistoryResponse);
            }
        }
    }

    /**
     * Actually sends the 'get_market_history' API call request. This method might be called multiple
     * times during the life-cycle of this instance because we might not have gotten anything
     * in the first requested interval.
     */
    private void sendHistoricalMarketDataRequest(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

        ArrayList<Serializable> params = new ArrayList<>();
        params.add(this.base.getObjectId());
        params.add(this.quote.getObjectId());
        params.add(this.bucket);
        params.add(dateFormat.format(this.start));
        params.add(dateFormat.format(this.end));

        ApiCall getRelativeAccountHistoryCall = new ApiCall(apiId, RPC.CALL_GET_MARKET_HISTORY, params, RPC.VERSION, currentId);
        mWebsocket.sendText(getRelativeAccountHistoryCall.toJsonString());

        counter++;
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            Log.d(TAG, ">>> "+frame.getPayloadText());
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        System.out.println("onError. Msg: "+cause.getMessage());
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        System.out.println("handleCallbackError. cause: "+cause.getMessage()+", error: "+cause.getClass());
        for (StackTraceElement element : cause.getStackTrace()){
            System.out.println(element.getFileName()+"#"+element.getClassName()+":"+element.getLineNumber());
        }
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}
