package com.softhinkerswallet.graphenej;

import com.softhinkerswallet.graphenej.errors.MalformedTransactionException;

import org.bitcoinj.core.ECKey;


/**
 * Created by adarsh on 05/03/17.
 */
public abstract class TransactionBuilder {
    protected ECKey privateKey;
    protected BlockData blockData;

    public TransactionBuilder(){}

    public TransactionBuilder(ECKey privKey){
        this.privateKey = privKey;
    }

    public TransactionBuilder setBlockData(BlockData blockData){
        this.blockData = blockData;
        return this;
    }

    public abstract Transaction build() throws MalformedTransactionException;
}
