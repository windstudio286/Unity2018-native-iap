package vn.vplay.sdk.t000a;

import com.android.billingclient.api.SkuDetails;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductInfo {
    private String productType;
    @SerializedName("productId")
    @Expose
    private String productId;
    @SerializedName("productName")
    @Expose
    private String productName;
    @SerializedName("productDescription")
    @Expose
    private String productDescription;
    @SerializedName("productPrice")
    @Expose
    private String productPrice;
    private SkuDetails skuDetails;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public SkuDetails getSkuDetails() {
        return skuDetails;
    }

    public void setSkuDetails(SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
    }
}
