package vn.vplay.sdk.t000a;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KcattaResponse {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("message")
    @Expose
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
