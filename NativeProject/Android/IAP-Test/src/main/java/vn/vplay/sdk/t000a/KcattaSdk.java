package vn.vplay.sdk.t000a;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.List;

public class KcattaSdk implements PurchasesUpdatedListener {
    private static final String TAG = KcattaSdk.class.getSimpleName();
    private static KcattaSdk INSTANCE;
    private UnityPlayer unityPlayer;
    private KcattaListener gameListener;
    private BillingClient billingClient;
    private Activity hostedActivity;
    private AppInfo appInfo;
    private List<ProductInfo> availableInappProducts;
    private List<ProductInfo> availableSubsProducts;
    private AdListener bannerAdListener;
    private AdListener interstitialAdListener;
    private AdListener rewardedAdListener;

    private AdView mBannerView = null;
    private InterstitialAd mInterstitialAd = null;
    private RewardedAd mRewardedAd = null;

    public static KcattaSdk getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KcattaSdk();
        }
        return INSTANCE;
    }

    public AppInfo getAppInfo(){
        return appInfo;
    }

    public boolean isBillingReady(){
        if(billingClient != null){
            return billingClient.isReady();
        }
        initBilling();
        return false;
    }

    public void requestPriceProduct2(List<ProductInfo> reqListProduct, String type){
        if(type.equals(BillingClient.ProductType.INAPP)){
            requestPriceProduct2(reqListProduct,type,availableInappProducts);
        }
        else if(type.equals(BillingClient.ProductType.SUBS)){
            requestPriceProduct2(reqListProduct,type,availableSubsProducts);
        }
    }

    private void requestPriceProduct2(List<ProductInfo> reqListProduct, String type, final List<ProductInfo> availableProducts){
        if (availableProducts != null) {
            if(availableProducts.size() > 0) {
                if (gameListener != null) {
                    gameListener.onGetProductsSuccess(availableProducts);
                }
                return;
            }
        }
        availableProducts.clear();
        Log.d("TAG", "reqListProduct count" + reqListProduct.size());
        if (isBillingReady()) {
            List<QueryProductDetailsParams.Product> skuList = new ArrayList<>();
            for (ProductInfo item : reqListProduct) {
                Log.d("TAG", "skuList.add" + item.getProductId());
                Log.d("TAG", "skuList.add" + item.getProductType());
                QueryProductDetailsParams.Product qProduct = QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(item.getProductId())
                        .setProductType(type).build();
                skuList.add(qProduct);
            }
            Log.d("TAG", "skuList.count" + skuList.size());

            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(skuList)
                            .build();
            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    new ProductDetailsResponseListener() {
                        public void onProductDetailsResponse(BillingResult billingResult,
                                                             List<ProductDetails> productDetailsList) {
                            // check billingResult
                            // process returned productDetailsList
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                                if (productDetailsList != null) {
                                    Log.d("TAG", "count products: " + productDetailsList.size());
                                    for (ProductDetails item :
                                            productDetailsList) {
                                        ProductInfo productInfo = new ProductInfo();
                                        productInfo.setProductDetails(item);
                                        productInfo.setProductId(item.getProductId());
                                        productInfo.setProductType(item.getProductType());
                                        if(item.getProductType().equals(BillingClient.ProductType.INAPP)){
                                            productInfo.setProductPrice(item.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                        }
                                        else{
                                            productInfo.setProductPrice(item.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice());
                                        }
                                        productInfo.setProductName(item.getTitle());
                                        productInfo.setProductDescription(item.getDescription());
                                        Log.d("TAG", item.toString());
                                        availableProducts.add(productInfo);
                                    }
                                }
                                Log.d("TAG", "list add count products: " + availableProducts.size());
                                if (gameListener != null) {
                                    gameListener.onGetProductsSuccess(availableProducts);
                                }
                            }
                            else{
                                Log.d("TAG", "error to get products: " + availableProducts.size());
                                if (gameListener != null) {
                                    gameListener.onGetProductError(new Error(billingResult.getDebugMessage()));
                                }
                            }
                        }
                    }
            );
        }
    }

    public void requestPriceProduct(List<ProductInfo> reqListProduct){
        if (availableInappProducts != null) {
            if(availableInappProducts.size() > 0) {
                if (gameListener != null) {
                    gameListener.onGetProductsSuccess(availableInappProducts);
                }
                return;
            }
        }
        if(availableInappProducts == null){
            availableInappProducts = new ArrayList<>();
        }
        availableInappProducts.clear();
        Log.d("TAG", "reqListProduct count" + reqListProduct.size());
        if (isBillingReady()) {
            List<String> skuList = new ArrayList<>();
            for (ProductInfo item : reqListProduct) {
                Log.d("TAG", "skuList.add" + item.getProductId());
                skuList.add(item.getProductId());
            }
            Log.d("TAG", "skuList.count" + skuList.size());
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                    Log.d("TAG", billingResult.getResponseCode() + "");
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if (skuDetailsList != null) {
                            Log.d("TAG", "count products: " + skuDetailsList.size());
                            for (SkuDetails item :
                                    skuDetailsList) {
                                ProductInfo productInfo = new ProductInfo();

                                productInfo.setProductId(item.getSku());
                                productInfo.setProductPrice(item.getPrice());
                                productInfo.setProductName(item.getTitle());
                                productInfo.setProductDescription(item.getDescription());
                                productInfo.setSkuDetails(item);
                                Log.d("TAG", item.getSku());
                                Log.d("TAG", item.getPrice());
                                Log.d("TAG", item.getTitle());
                                availableInappProducts.add(productInfo);
                            }
                        }
                        Log.d("TAG", "list add count products: " + availableInappProducts.size());
                        if (gameListener != null) {
                            gameListener.onGetProductsSuccess(availableInappProducts);
                        }
                    }
                    else{
                        Log.d("TAG", "error to get products: " + availableInappProducts.size());
                        if (gameListener != null) {
                            gameListener.onGetProductError(new Error(billingResult.getDebugMessage()));
                        }
                    }
                }
            });
        }
    }

    public void init(Activity hostedActivity, AppInfo appInfo){
            this.hostedActivity = hostedActivity;
            this.appInfo = appInfo;
            initBilling();
    }

    private void initAds(){
        if(this.hostedActivity != null) {
            MobileAds.initialize(this.hostedActivity, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.d(TAG, "onInitializationComplete");
                }
            });

            List<String> testDevices = new ArrayList<>();
            testDevices.add("32A371912B7A42FA62F47C90B83D6A4E");

            if(!this.appInfo.tagForChildDirectedTreatment) {
                RequestConfiguration requestConfiguration
                        = new RequestConfiguration.Builder()
                        .setTestDeviceIds(testDevices)
                        .build();
                MobileAds.setRequestConfiguration(requestConfiguration);
            }
            else{
                RequestConfiguration requestConfiguration
                        = new RequestConfiguration.Builder()
                        .setTestDeviceIds(testDevices)
                        .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                        .build();
                MobileAds.setRequestConfiguration(requestConfiguration);
            }
        }
    }

    public void setGameListener(KcattaListener gameListener){
        this.gameListener = gameListener;
    }

    public void setBannerAdListener(AdListener item){
        this.bannerAdListener = item;
    }

    public void setInterstitialAdListener(AdListener item){
        this.interstitialAdListener = item;
    }

    public void setRewardedAdListener(AdListener item){
        this.rewardedAdListener = item;
    }

    private BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingServiceDisconnected() {
            // Try to restart the connection on the next request to
            // Google Play by calling the startConnection() method.
            Log.d(TAG, "onBillingServiceDisconnected");
        }

        @Override
        public void onBillingSetupFinished(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // The BillingClient is ready. You can query purchases here.
                Log.d(TAG, "onBillingSetupFinished");
                Log.i(TAG, "try query iap");
                if (billingClient != null && billingClient.isReady()) {
                    queryPurchase();
                }
            }
        }
    };

    private void initBilling(){
        if (availableInappProducts == null) {
            availableInappProducts = new ArrayList<>();
        }
        if (availableSubsProducts == null) {
            availableSubsProducts = new ArrayList<>();
        }
        if (hostedActivity != null) {
            if (billingClient == null) {
                billingClient = BillingClient.newBuilder(hostedActivity)
                        .setListener(this)
                        .enablePendingPurchases()
                        .build();

                billingClient.startConnection(billingClientStateListener);
            } else {
                if (!billingClient.isReady())
                    billingClient.startConnection(billingClientStateListener);
            }
        }
    }

    public void payProduct2(String productId, String type){
        if(type.equals(BillingClient.ProductType.INAPP)) {
            if (availableInappProducts != null) {
                ProductInfo product = findProductInfo(availableInappProducts, productId);
                if (product != null) {
                    if (billingClient != null && billingClient.isReady()) {
                        /*BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(product.getSkuDetails())
                                .build();
                        int responseCode = billingClient.launchBillingFlow(hostedActivity, billingFlowParams).getResponseCode();*/
                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product.getProductDetails())
                                .build();
                        productDetailsParamsList.add(productDetailsParams);

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(hostedActivity, billingFlowParams).getResponseCode();

                    }
                } else {
                    if (gameListener != null) {
                        gameListener.onPayProductError(new Error(productId));
                    }
                }
            }
        }
        else if(type.equals(BillingClient.ProductType.SUBS)) {
            if (availableSubsProducts != null) {
                ProductInfo product = findProductInfo(availableSubsProducts, productId);
                if (product != null) {
                    if (billingClient != null && billingClient.isReady()) {
                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                        for ( ProductDetails.SubscriptionOfferDetails i : product.getProductDetails().getSubscriptionOfferDetails()) {
                            Log.d(TAG, "ProductDetails.SubscriptionOfferDetails: "+i.getOfferToken());
                            Log.d(TAG, "ProductDetails.SubscriptionOfferDetails: "+i.getOfferId());
                            Log.d(TAG, "ProductDetails.SubscriptionOfferDetails: "+i.getBasePlanId());
                            Log.d(TAG, "=======");
                        }
                        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product.getProductDetails())
                                .setOfferToken(product.getProductDetails().getSubscriptionOfferDetails().get(1).getOfferToken())
                                .build();
                        productDetailsParamsList.add(productDetailsParams);

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setObfuscatedProfileId("congtt")
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(hostedActivity, billingFlowParams).getResponseCode();

                    }
                } else {
                    if (gameListener != null) {
                        gameListener.onPayProductError(new Error(productId));
                    }
                }
            }
        }
    }

    public void payProduct(String productId){
        if(availableInappProducts != null){
            ProductInfo product = findProductInfo(availableInappProducts,productId);
            if (product != null) {
                if (billingClient != null && billingClient.isReady()) {
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(product.getSkuDetails())
                            .build();
                    int responseCode = billingClient.launchBillingFlow(hostedActivity, billingFlowParams).getResponseCode();

                }
            }
            else {
                if (gameListener != null) {
                    gameListener.onPayProductError(new Error(productId));
                }
            }
        }
    }

    private void queryPurchase(){
        if(billingClient != null && billingClient.isReady()){
            billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && purchases != null) {
                        for (Purchase purchase : purchases) {
                            handleQueryPurchase(purchase);
                        }
                    }
                }
            });
        }
    }

    public void queryHistoryPurchase(){
        if(billingClient != null && billingClient.isReady()){
            QueryPurchaseHistoryParams queryPurchaseHistoryParams = QueryPurchaseHistoryParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build();
            billingClient.queryPurchaseHistoryAsync(queryPurchaseHistoryParams, new PurchaseHistoryResponseListener() {
                @Override
                public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
                    if(list == null) return;
                    for (PurchaseHistoryRecord item: list){

                    }
                }
            });
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            if (purchases != null) {
                for (Purchase purchase : purchases) {
                    for (String productID : purchase.getSkus()
                    ) {
                    }
                }
            }
            Log.d(TAG,"Cancel payment");

        }
    }

    private void handleQueryPurchase(Purchase purchase) {
        Log.d(TAG, "handleQueryPurchase");
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            consumeAsync(purchase, purchase.getPurchaseToken());

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            if (gameListener != null) {
                gameListener.onPayProductError(new Error("Có lỗi trong quá trình thanh toán"));
            }

        } else {
            if (gameListener != null) {
                gameListener.onPayProductError(new Error("Có lỗi trong quá trình thanh toán"));
            }
            for (String productId : purchase.getSkus()
            ) {
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        Log.d(TAG, "handlePurchase");
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            String productID = purchase.getProducts().get(0);
            ProductInfo productInfo = findProductInfo(availableInappProducts,productID);
            if(productInfo.getProductType().equals(KcattaConstants.PRODUCT_TYPE_CONSUMABLE)) {
                consumeAsync(purchase, purchase.getPurchaseToken());
            }
            else{
                acknowledgeAsync(purchase, purchase.getPurchaseToken());
            }

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            if (gameListener != null) {
                gameListener.onPayProductError(new Error("Có lỗi trong quá trình thanh toán"));
            }

        } else {
            if (gameListener != null) {
                gameListener.onPayProductError(new Error("Có lỗi trong quá trình thanh toán"));
            }
            for (String productId : purchase.getSkus()
            ) {
            }
        }
    }

    private void acknowledgeAsync(final Purchase purchase, String purchasedToken) {
        if (billingClient != null && billingClient.isReady()) {
            AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "onAcknowledgePurchaseResponse OK");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                confirmTransaction(purchase);
                            }
                        },100);

                    }
                    else{
                        Log.d(TAG, "onAcknowledgePurchaseResponse fail " + billingResult.getDebugMessage());
                        Log.d(TAG, "onAcknowledgePurchaseResponse fail " + billingResult.getResponseCode());
                    }
                }
            };
            if(!purchase.isAcknowledged()){
                Log.d(TAG, "onAcknowledgePurchaseResponse request acknowledge");
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    private void consumeAsync(final Purchase purchase, String purchasedToken) {
        Log.d(TAG, "consumeAsync.purchasedToken:" + purchasedToken);
        if (billingClient != null && billingClient.isReady()) {
            ConsumeParams consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchasedToken)
                            .build();

            ConsumeResponseListener listener = new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "onConsumeResponse OK");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                confirmTransaction(purchase);
                            }
                        },100);

                    }
                    else{
                        Log.d(TAG, "onConsumeResponse fail " + billingResult.getDebugMessage());
                        Log.d(TAG, "onConsumeResponse fail " + billingResult.getResponseCode());
                    }
                }
            };

            billingClient.consumeAsync(consumeParams, listener);
        }
    }

    private void confirmTransaction(Purchase purchase){
        if(gameListener != null){
            String productId = "";
            if(purchase.getSkus().size() > 0){
                productId = purchase.getSkus().get(0);
            }
            gameListener.onPayProductSuccess(productId);
        }
    }
    public void destroySDK(){
        if(billingClient != null && billingClient.isReady()){
            billingClient.endConnection();
        }
    }
    private static ProductInfo findProductInfo(List<ProductInfo> listProduct,String appId){
        if(listProduct != null && listProduct.size() > 0){
            for (ProductInfo productInfo: listProduct
            ) {
                if(productInfo.getProductId().equals(appId)){
                    return productInfo;
                }
            }
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private void addOrCreateBannerAd(String bannerId, String orientation, String aligment){
        if(mBannerView == null && hostedActivity != null){
            mBannerView = new AdView(this.hostedActivity);
            mBannerView.setVisibility(View.GONE);
            if(mBannerView != null){
                mBannerView.setAdListener(bannerAdListener);
            }
            AdSize adSize = getAdSize(orientation);
            Log.d(TAG,"adSize "+adSize.getHeight());
            Log.d(TAG,"adSize "+adSize.getWidth());
            mBannerView.setAdSize(adSize);
            mBannerView.setAdUnitId(bannerId);
            this.addToRootView(this.hostedActivity,mBannerView,aligment);
        }
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        Bundle extras = new Bundle();
        /*if (this.mGDPR) {
            extras.putString("npa", "1");
        }
        if (this.is_designed_for_families) {
            extras.putBoolean("is_designed_for_families", true);
        }*/
        adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        mBannerView.loadAd(adRequestBuilder.build());
    }
    private void addToRootView(Activity activity, View bannerAd, String alignment) {
        RelativeLayout layout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        boolean mSafeArea = false;
        if (mSafeArea) {
            int[] cutouts = Utils.getSafeInsets(activity);
            lp.setMargins(cutouts[1], cutouts[3], cutouts[2], cutouts[0]);
        }
        activity.addContentView(layout, lp);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        if ("top".equalsIgnoreCase(alignment)) {
            params.addRule(10); //RelativeLayout.ALIGN_PARENT_TOP
            params.addRule(14); //RelativeLayout.CENTER_HORIZONTAL
        } else if ("bottom".equalsIgnoreCase(alignment)) {
            params.addRule(12); //RelativeLayout.ALIGN_PARENT_BOTTOM
            params.addRule(14); //RelativeLayout.CENTER_HORIZONTAL
        } else if ("right".equalsIgnoreCase(alignment)) {
            params.addRule(11); //RelativeLayout.ALIGN_PARENT_RIGHT
            params.addRule(15); //RelativeLayout.CENTER_VERTICAL
        } else if ("left".equalsIgnoreCase(alignment)) {
            params.addRule(9); //RelativeLayout.ALIGN_PARENT_LEFT
            params.addRule(15); //RelativeLayout.CENTER_VERTICAL
        } else if ("center".equalsIgnoreCase(alignment)) {
            params.addRule(13); //RelativeLayout.CENTER_IN_PARENT
        } else if ("top_left".equalsIgnoreCase(alignment) || "left_top".equalsIgnoreCase(alignment)) {
            params.addRule(10); //RelativeLayout.ALIGN_PARENT_TOP
            params.addRule(9); //RelativeLayout.ALIGN_PARENT_LEFT
        } else if ("top_right".equalsIgnoreCase(alignment) || "right_top".equalsIgnoreCase(alignment)) {
            params.addRule(10); //RelativeLayout.ALIGN_PARENT_TOP
            params.addRule(11); //RelativeLayout.ALIGN_PARENT_RIGHT
        } else if ("bottom_left".equalsIgnoreCase(alignment) || "left_bottom".equalsIgnoreCase(alignment)) {
            params.addRule(12); //RelativeLayout.ALIGN_PARENT_BOTTOM
            params.addRule(9); //RelativeLayout.ALIGN_PARENT_LEFT
        } else if ("bottom_right".equalsIgnoreCase(alignment) || "right_bottom".equalsIgnoreCase(alignment)) {
            params.addRule(12); //RelativeLayout.ALIGN_PARENT_BOTTOM
            params.addRule(11); //RelativeLayout.ALIGN_PARENT_RIGHT
        } else {
            params.addRule(10); //RelativeLayout.ALIGN_PARENT_TOP
            params.addRule(14); //RelativeLayout.CENTER_HORIZONTAL
        }
        layout.addView(bannerAd, params);
        layout.setFocusable(false);
    }

    private AdSize getAdSize(String orientation) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = this.hostedActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        Log.d(TAG,"adWidth "+adWidth);
        Log.d(TAG,"density "+density);
        Log.d(TAG,"widthPixels "+widthPixels);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        AdSize adSize;
        if(orientation.equals("landscape")) {
            adSize = AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(this.hostedActivity, adWidth);
            Log.d(TAG, "adSize h" + adSize.getHeight());
            Log.d(TAG, "adSize w " + adSize.getWidth());
            while (adSize.getHeight() > 65) {
                int newAdWidth = 65 * adSize.getWidth() / adSize.getHeight();
                adSize = AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(this.hostedActivity, newAdWidth);
                Log.d(TAG, "newadSize h" + adSize.getHeight());
                Log.d(TAG, "newadSize w " + adSize.getWidth());
            }
        }
        else{
            adSize = AdSize.getPortraitAnchoredAdaptiveBannerAdSize(this.hostedActivity, adWidth);
            Log.d(TAG, "adSize h" + adSize.getHeight());
            Log.d(TAG, "adSize w " + adSize.getWidth());
            while (adSize.getHeight() > 40) {
                int newAdWidth = 40 * adSize.getWidth() / adSize.getHeight();
                adSize = AdSize.getPortraitAnchoredAdaptiveBannerAdSize(this.hostedActivity, newAdWidth);
                Log.d(TAG, "newadSize h" + adSize.getHeight());
                Log.d(TAG, "newadSize w " + adSize.getWidth());
            }
        }
        return adSize;
    }
}
