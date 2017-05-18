package com.softhinkers_wallet.utils;

import com.softhinkers_wallet.models.AccountDetails;
import com.softhinkers_wallet.models.AccountUpgrade;
import com.softhinkers_wallet.models.EquivalentComponentResponse;
import com.softhinkers_wallet.models.GenerateKeys;
import com.softhinkers_wallet.models.QrHash;
import com.softhinkers_wallet.models.RegisterAccountResponse;
import com.softhinkers_wallet.models.RegisterBalanceResponse;
import com.softhinkers_wallet.models.ResponseBinFormat;
import com.softhinkers_wallet.models.TransactionIdResponse;
import com.softhinkers_wallet.models.TransactionResponse;
import com.softhinkers_wallet.models.TransactionSmartCoin;
import com.softhinkers_wallet.models.TransferResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.softhinkers_wallet.models.CCAssets;
import com.softhinkers_wallet.models.DecodeMemo;
import com.softhinkers_wallet.models.DecodeMemosArray;
import com.softhinkers_wallet.models.LtmFee;
import com.softhinkers_wallet.models.TradeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
/**
 * Created by adarsh on 05/01/17.
 */
public interface IWebService {

    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    @GET("/assets/")
    Call<CCAssets> getAssets();

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<AccountDetails> getAccount(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<AccountDetails> getAccountFromBin(@Body HashMap<String, Object> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<ResponseBinFormat> getBytesFromBrainKey(@Body HashMap<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/get_qr_hash_w_note/")
    Call<QrHash> getQrHashWithNote(@Body Map<String, String> params);

    @GET("/get_transactions/{accountId}/{orderId}")
    Call<TransactionSmartCoin[]> getTransactionSmartCoin(@Path("accountId") String accountId, @Path("orderId") String orderId);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<TransferResponse> getTransferResponse(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<DecodeMemo> getDecodedMemo(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<DecodeMemosArray> getDecodedMemosArray(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<GenerateKeys> getGeneratedKeys(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/api/v1/accounts")
    Call<RegisterAccountResponse> getReg(@Body Map<String, HashMap> params);

    @Headers({"Content-Type: application/json"})
    @POST("/v1/create")
    Call<RegisterAccountResponse> getRegLocal(@Body Map<String, HashMap> params);
    //TODO Shrikant

    @Headers({"Content-Type: application/json"})
    @POST("/v1/getBalance")
    Call<RegisterBalanceResponse> getRemoteBalance(@Body Map<String, Object> params);
    //TODO Shrikant

    @Headers({"Content-Type: application/json"})
    @POST("/v1/transfer")
    Call<TransactionResponse> getTransactionSendResponse(@Body Map<String, String> params);
    //TODO Shrikant

    @GET
    Call<Void> sendCallback(@Url String urlSubString, @Query("block") String block, @Query("trx") String trx);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<TradeResponse> getTradeResponse(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<AccountUpgrade> getAccountUpgrade(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<EquivalentComponentResponse> getEquivalentComponent(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<TransactionIdResponse> getTransactionIdComponent(@Body Map<String, String> params);

    @Headers({"Content-Type: application/json"})
    @POST("/")
    Call<LtmFee> getLtmFee(@Body Map<String, String> params);

    @GET("/{md5Email}.json")
    Call<Object> getGravatarProfile(@Path("md5Email") String md5Email);

}
