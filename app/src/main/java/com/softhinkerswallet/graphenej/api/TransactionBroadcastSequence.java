package com.softhinkerswallet.graphenej.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.softhinkerswallet.graphenej.Asset;
import com.softhinkerswallet.graphenej.AssetAmount;
import com.softhinkerswallet.graphenej.BlockData;
import com.softhinkerswallet.graphenej.RPC;
import com.softhinkerswallet.graphenej.Transaction;
import com.softhinkerswallet.graphenej.interfaces.WitnessResponseListener;
import com.softhinkerswallet.graphenej.models.ApiCall;
import com.softhinkerswallet.graphenej.models.BaseResponse;
import com.softhinkerswallet.graphenej.models.DynamicGlobalProperties;
import com.softhinkerswallet.graphenej.models.WitnessResponse;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class that will handle the transaction publication procedure.
 */
public class TransactionBroadcastSequence extends WebSocketAdapter {
    private final String TAG = this.getClass().getName();

    private final static int LOGIN_ID = 1;
    private final static int GET_NETWORK_BROADCAST_ID = 2;
    private final static int GET_NETWORK_DYNAMIC_PARAMETERS = 3;
    private final static int GET_REQUIRED_FEES = 4;
    private final static int BROADCAST_TRANSACTION = 5;

    private Asset feeAsset;
    private Transaction transaction;
    private WitnessResponseListener mListener;

    private int currentId = 1;
    private int broadcastApiId = -1;

    /**
     * Constructor of this class. The ids required
     * @param transaction: The transaction to be broadcasted.
     * @param listener: A class implementing the WitnessResponseListener interface. This should
     *                be implemented by the party interested in being notified about the success/failure
     *                of the transaction broadcast operation.
     */
    public TransactionBroadcastSequence(Transaction transaction, Asset feeAsset, WitnessResponseListener listener){
        this.transaction = transaction;
        this.feeAsset = feeAsset;
        this.mListener = listener;
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        ArrayList<Serializable> loginParams = new ArrayList<>();
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, RPC.CALL_LOGIN, loginParams, RPC.VERSION, currentId);
        websocket.sendText(loginCall.toJsonString());
    }

    @Override
    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame())
            Log.d(TAG, "<<< "+frame.getPayloadText());
        String response = frame.getPayloadText();
        Gson gson = new Gson();
        BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
        if(baseResponse.error != null){
            mListener.onError(baseResponse.error);
            websocket.disconnect();
        }else{
            currentId++;
            ArrayList<Serializable> emptyParams = new ArrayList<>();
            if(baseResponse.id == LOGIN_ID){
                ApiCall networkApiIdCall = new ApiCall(1, RPC.CALL_NETWORK_BROADCAST, emptyParams, RPC.VERSION, currentId);
                websocket.sendText(networkApiIdCall.toJsonString());
            }else if(baseResponse.id == GET_NETWORK_BROADCAST_ID){
                Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                broadcastApiId = witnessResponse.result;

                // Building API call to request dynamic network properties
                ApiCall getDynamicParametersCall = new ApiCall(0,
                        RPC.CALL_GET_DYNAMIC_GLOBAL_PROPERTIES,
                        emptyParams,
                        RPC.VERSION,
                        currentId);

                // Requesting network properties
                websocket.sendText(getDynamicParametersCall.toJsonString());
            }else if(baseResponse.id == GET_NETWORK_DYNAMIC_PARAMETERS){
                Type DynamicGlobalPropertiesResponse = new TypeToken<WitnessResponse<DynamicGlobalProperties>>(){}.getType();
                WitnessResponse<DynamicGlobalProperties> witnessResponse = gson.fromJson(response, DynamicGlobalPropertiesResponse);
                DynamicGlobalProperties dynamicProperties = witnessResponse.result;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = dateFormat.parse(dynamicProperties.time);

                // Adjusting dynamic block data to every transaction
                long expirationTime = (date.getTime() / 1000) + Transaction.DEFAULT_EXPIRATION_TIME;
                String headBlockId = dynamicProperties.head_block_id;
                long headBlockNumber = dynamicProperties.head_block_number;
                transaction.setBlockData(new BlockData(headBlockNumber, headBlockId, expirationTime));

                // Building a new API call to request fees information
                ArrayList<Serializable> accountParams = new ArrayList<>();
                accountParams.add((Serializable) transaction.getOperations());
                accountParams.add(this.feeAsset.getObjectId());
                ApiCall getRequiredFees = new ApiCall(0, RPC.CALL_GET_REQUIRED_FEES, accountParams, RPC.VERSION, currentId);

                // Requesting fee amount
                websocket.sendText(getRequiredFees.toJsonString());
            }else if(baseResponse.id ==  GET_REQUIRED_FEES){
                Type GetRequiredFeesResponse = new TypeToken<WitnessResponse<List<AssetAmount>>>(){}.getType();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetDeserializer());
                WitnessResponse<List<AssetAmount>> requiredFeesResponse = gsonBuilder.create().fromJson(response, GetRequiredFeesResponse);

                // Setting fees
                transaction.setFees(requiredFeesResponse.result);
                ArrayList<Serializable> transactions = new ArrayList<>();
                transactions.add(transaction);
                ApiCall call = new ApiCall(broadcastApiId,
                        RPC.CALL_BROADCAST_TRANSACTION,
                        transactions,
                        RPC.VERSION,
                        currentId);

                // Finally broadcasting transaction
                websocket.sendText(call.toJsonString());
            }else if(baseResponse.id >= BROADCAST_TRANSACTION){
                Type WitnessResponseType = new TypeToken<WitnessResponse<String>>(){}.getType();
                WitnessResponse<WitnessResponse<String>> witnessResponse = gson.fromJson(response, WitnessResponseType);
                mListener.onSuccess(witnessResponse);
                websocket.disconnect();
            }
        }
    }

    @Override
    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
        if(frame.isTextFrame()){
            Log.d(TAG, ">>> "+frame.getPayloadText());
        }
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        Log.e(TAG, "onError. cause: "+cause.getMessage());
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
        Log.e(TAG, "handleCallbackError. cause: "+cause.getMessage()+", error: "+cause.getClass());
        for (StackTraceElement element : cause.getStackTrace()){
            Log.e(TAG, element.getFileName()+"#"+element.getClassName()+":"+element.getLineNumber());
        }
        mListener.onError(new BaseResponse.Error(cause.getMessage()));
        websocket.disconnect();
    }
}