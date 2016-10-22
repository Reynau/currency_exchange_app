package com.lopezreynau.currencyexchange;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xavi on 15/01/2016.
 */
public class CurrencyElement implements Parcelable {
    private String id;
    private float rate;
    private float ask;
    private float bid;

    public CurrencyElement(String id, float rate, float ask, float bid) {
        this.id = id;
        this.rate = rate;
        this.ask = ask;
        this.bid = bid;
    }

    private CurrencyElement(Parcel in) {
        id = in.readString();
        rate = in.readFloat();
        ask = in.readFloat();
        bid = in.readFloat();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeFloat(rate);
        dest.writeFloat(ask);
        dest.writeFloat(bid);
    }

    public static final Parcelable.Creator<CurrencyElement> CREATOR = new Parcelable.Creator<CurrencyElement>() {
        public CurrencyElement createFromParcel(Parcel in) {
            return new CurrencyElement(in);
        }

        public CurrencyElement[] newArray(int size) {
            return new CurrencyElement[size];

        }
    };

    public String getId() { return id; }
    public float getRate() { return rate; }
    public float getAsk() { return ask; }
    public float getBid() { return bid; }
}
