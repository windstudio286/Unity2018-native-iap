package vn.vplay.sdk.t000a;

import com.android.billingclient.api.Purchase;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardItem;

import java.util.List;

public interface KcattaListener {
    void onPayProductSuccess(Purchase item,String productId);
    void onPayProductError(int errCode,Error error);
    void onGetProductsInAppSuccess(List<ProductInfo> products);
    void onGetProductsSubsSuccess(List<ProductInfo> products);
    void onGetProductError(String typeProduct,Error error);
    void onAdFailedToLoad(String adType, String adId, AdError error);
    void onAdLoaded(String adType,String adId);
    void onAdClosed(String adType,String adId);
    void onAdOpened(String adType,String adId);
    void onAdEarnedReward(String adType, String adId, int rewardAmount);
    void onQueryProductInApp(Purchase item, boolean finishedQuery);
    void onQueryProductSubs(Purchase item, boolean finishedQuery);
    void onQueryError(int errCode,Error error);
}
