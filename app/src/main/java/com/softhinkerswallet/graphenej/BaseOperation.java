package com.softhinkerswallet.graphenej;

import com.softhinkerswallet.graphenej.interfaces.ByteSerializable;
import com.softhinkerswallet.graphenej.interfaces.JsonSerializable;

/**
 * Created by adarsh on 05/01/17.
 */
public abstract class BaseOperation implements ByteSerializable, JsonSerializable {

    protected OperationType type;

    public BaseOperation(OperationType type){
        this.type = type;
    }

    public byte getId() {
        return (byte) this.type.ordinal();
    }

    public abstract void setFee(AssetAmount assetAmount);

    public abstract byte[] toBytes();
}
