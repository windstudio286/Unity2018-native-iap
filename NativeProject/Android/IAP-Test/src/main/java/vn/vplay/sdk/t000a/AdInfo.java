package vn.vplay.sdk.t000a;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdInfo {
    @SerializedName("state")
    @Expose
    private int state; //inapp; subs consumable; non-consumable; subs
    @SerializedName("adId")
    @Expose
    private String adId;

    @SerializedName("message")
    @Expose
    private String message;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    @SerializedName("data")
    @Expose
    private int data;
}
