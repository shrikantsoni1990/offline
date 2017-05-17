package com.softhinkerswallet.graphenej;

import com.softhinkerswallet.graphenej.interfaces.ByteSerializable;

/**
 * Created by adarsh on 05/01/17.
 */
public class Vote implements ByteSerializable {
    private int type;
    private int instance;

    public Vote(String vote){
        String[] parts = vote.split(":");
        assert(parts.length == 2);
        this.type = Integer.valueOf(parts[0]);
        this.instance = Integer.valueOf(parts[1]);
    }

    public Vote(int type, int instance){
        this.type = type;
        this.instance = instance;
    }

    @Override
    public String toString() {
        return String.format("%d:%d", this.type, this.instance);
    }

    @Override
    public byte[] toBytes() {
        return new byte[] { (byte) this.instance, (byte) this.type };
    }
}
