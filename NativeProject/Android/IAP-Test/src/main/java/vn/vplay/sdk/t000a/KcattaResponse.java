package vn.vplay.sdk.t000a;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KcattaResponse {
    @SerializedName("success")
    @Expose
    private boolean success;

    @SerializedName("key")
    @Expose
    private String key;

    @SerializedName("value")
    @Expose
    private Object value;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getMessage() {
        return value;
    }

    public void setMessage(Object message) {
        this.value = message;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
