package vn.vplay.sdk.t000a;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.SkuDetails;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductInfo {
    @SerializedName("productType")
    @Expose
    private String productType; //inapp; subs consumable; non-consumable; subs
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
    @SerializedName("preferOfferId")
    @Expose
    private String preferOfferId;
    @SerializedName("preferBasePlanId")
    @Expose
    private String preferBasePlanId;

    private SkuDetails skuDetails;
    private ProductDetails productDetails;


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

    public ProductDetails getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(ProductDetails productDetails) {
        this.productDetails = productDetails;
    }

    public String getPreferOfferId() {
        return preferOfferId;
    }

    public void setPreferOfferId(String preferOfferId) {
        this.preferOfferId = preferOfferId;
    }


    public String getPreferBasePlanId() {
        return preferBasePlanId;
    }

    public void setPreferBasePlanId(String preferBaseId) {
        this.preferBasePlanId = preferBaseId;
    }

    public void clone(ProductInfo copyObj){
        this.productType = copyObj.getProductType();
        this.productId = copyObj.getProductId();
        this.productName = copyObj.getProductName();
        this.productDescription = copyObj.getProductDescription();
        this.productPrice = copyObj.getProductPrice();
        this.preferOfferId = copyObj.getPreferOfferId();
        this.preferBasePlanId = copyObj.getPreferBasePlanId();
        this.productDetails = copyObj.getProductDetails();
        this.skuDetails = copyObj.getSkuDetails();
    }

    public ProductDetails.SubscriptionOfferDetails findOfferDetail(){
        ProductDetails.SubscriptionOfferDetails item= null;
        if(productType.equals(KcattaConstants.PRODUCT_TYPE_SUBS) && productDetails!= null && productDetails.getSubscriptionOfferDetails().size()> 0) {
            if (preferBasePlanId == null || (preferBasePlanId != null && preferBasePlanId.isEmpty())) {
                item = productDetails.getSubscriptionOfferDetails().get(0);
            }
            if(preferBasePlanId != null && !preferBasePlanId.isEmpty()){
                for(int i = 0;i<productDetails.getSubscriptionOfferDetails().size();i++){
                    ProductDetails.SubscriptionOfferDetails curItem = productDetails.getSubscriptionOfferDetails().get(i);
                    if(curItem.getBasePlanId().equals(preferBasePlanId)){
                        item = curItem;
                        if((curItem.getOfferId() != null && curItem.getOfferId().equals(preferOfferId))){
                            item = curItem;
                            break;
                        }
                    }
                }
            }
        }
        return item;
    }
}
