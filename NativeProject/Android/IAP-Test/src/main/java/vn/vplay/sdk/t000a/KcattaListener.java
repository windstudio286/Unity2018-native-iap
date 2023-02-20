package vn.vplay.sdk.t000a;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardItem;

import java.util.List;

public interface KcattaListener {
    void onPayProductSuccess(String productId);
    void onPayProductError(Error error);
    void onGetProductsSuccess(List<ProductInfo> products);
    void onGetProductError(Error error);
    void onAdFailedToLoad(String adType, String adId, AdError error);
    void onAdLoaded(String adType,String adId);
    void onAdClosed(String adType,String adId);
    void onAdOpened(String adType,String adId);
    void onAdEarnedReward(String adType, String adId, int rewardAmount);
}
