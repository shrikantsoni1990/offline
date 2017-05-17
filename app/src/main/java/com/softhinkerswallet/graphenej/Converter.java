package com.softhinkerswallet.graphenej;

import android.util.Log;

import com.softhinkerswallet.graphenej.errors.IncompleteAssetError;

import java.math.BigDecimal;

import com.softhinkerswallet.graphenej.models.BucketObject;

/**
 * Converter class used to translate the market information contained in a BucketObject instance.
 *
 * Created by nelson on 12/23/16.
 */
public class Converter {
    private final String TAG = this.getClass().getName();
    public static final int OPEN_VALUE = 0;
    public static final int CLOSE_VALUE = 1;
    public static final int HIGH_VALUE = 2;
    public static final int LOW_VALUE = 3;

    public static final int BASE_TO_QUOTE = 100;
    public static final int QUOTE_TO_BASE = 101;

    private Asset base;
    private Asset quote;
    private BucketObject bucket;

    public Converter(Asset base, Asset quote, BucketObject bucket){
        this.base = base;
        this.quote = quote;
        this.bucket = bucket;
    }

    /**
     * Method used to obtain the equivalence between two assets considering their precisions
     * and given the specific time bucket passed in the constructor.
     *
     * The resulting double value will tell us how much of a given asset, a unit of
     * its pair is worth.
     *
     * The second argument is used to specify which of the assets should
     * be taken as a unit reference.
     *
     * For instance if used with the BASE_TO_QUOTE constant, this method will tell us how
     * many of the quote asset will make up for a unit of the base asset. And the opposite
     * is true for the QUOTE_TO_BASE contant.
     *
     * @param bucketAttribute: The desired bucket attribute to take in consideration. Can
     *                       be any of the following: OPEN_VALUE, CLOSE_VALUE, HIGH_VALUE or
     *                       LOW_VALUE.
     * @param direction: One of two constants 'BASE_TO_QUOTE' or 'QUOTE_TO_BASE' used to specify
     *                 which of the two assets is the one used as a unitary reference.
     * @return: double value representing how much of one asset, a unit of the paired asset
     * was worth at the point in time specified by the time bucket and the bucket parameter.
     */
    public double getConversionRate(int bucketAttribute, int direction){
        if(this.base.getPrecision() == -1 || this.quote.getPrecision() == -1){
            throw new IncompleteAssetError();
        }
        BigDecimal baseValue;
        BigDecimal quoteValue;
        switch (bucketAttribute){
            case OPEN_VALUE:
                baseValue = bucket.open_base;
                quoteValue = bucket.open_quote;
                break;
            case CLOSE_VALUE:
                baseValue = bucket.close_base;
                quoteValue = bucket.close_quote;
                break;
            case HIGH_VALUE:
                baseValue = bucket.high_base;
                quoteValue = bucket.high_quote;
                break;
            case LOW_VALUE:
                baseValue = bucket.low_base;
                quoteValue = bucket.low_quote;
                break;
            default:
                baseValue = bucket.close_base;
                quoteValue = bucket.close_quote;
        }
        double basePrecisionAdjusted = baseValue.divide(BigDecimal.valueOf((long) Math.pow(10, base.getPrecision()))).doubleValue();
        double quotePrecisionAdjusted = quoteValue.divide(BigDecimal.valueOf((long) Math.pow(10, quote.getPrecision()))).doubleValue();
        if(direction == QUOTE_TO_BASE){
            return basePrecisionAdjusted / quotePrecisionAdjusted;
        }else{
            return quotePrecisionAdjusted / basePrecisionAdjusted;
        }
    }

    public long convert(AssetAmount assetAmount, int bucketAttribute) {
        double conversionRate = 0;
        double precisionFactor = 0.0;
        if(assetAmount.getAsset().equals(this.base)){
            conversionRate = this.getConversionRate(bucketAttribute, BASE_TO_QUOTE);
            precisionFactor = Math.pow(10, this.quote.getPrecision()) / Math.pow(10, this.base.getPrecision());
        }else if(assetAmount.getAsset().equals(this.quote)){
            conversionRate = this.getConversionRate(bucketAttribute, QUOTE_TO_BASE);
            precisionFactor = Math.pow(10, this.base.getPrecision()) / Math.pow(10, this.quote.getPrecision());
        }
        long assetAmountValue = assetAmount.getAmount().longValue();
        Log.d(TAG,String.format("asset amount: %d, conversion rate: %f, precision: %f", assetAmountValue, conversionRate, precisionFactor));
        long convertedBaseValue = (long) (assetAmountValue * conversionRate * precisionFactor);
        return convertedBaseValue;
    }
}