package vn.vplay.sdk.t000a;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

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
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.List;

public class KcattaSdk implements PurchasesUpdatedListener {
    private static final String TAG = KcattaSdk.class.getSimpleName();
    private static KcattaSdk INSTANCE;
    private KcattaListener gameListener;
    private BillingClient billingClient;
    private Activity hostedActivity;
    private AppInfo appInfo;
    private List<ProductInfo> availableInappProducts;
    private List<ProductInfo> availableSubsProducts;

    private AdView mBannerView = null;
    private InterstitialAd mInterstitialAd = null;
    private RewardedAd mRewardedAd = null;

    public static KcattaSdk getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KcattaSdk();
        }
        return INSTANCE;
    }

    public Activity getHostedActivity(){
        return hostedActivity;
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

    public void requestPriceProductV5(List<ProductInfo> reqListProduct, String type){
        if(type.equals(BillingClient.ProductType.INAPP)){
            requestPriceProductV5(reqListProduct,type,availableInappProducts);
        }
        else if(type.equals(BillingClient.ProductType.SUBS)){
            requestPriceProductV5(reqListProduct,type,availableSubsProducts);
        }
    }

    private void requestPriceProductV5(List<ProductInfo> reqListProduct, String type,List<ProductInfo> availableProducts){
        if (availableProducts != null) {
            if(availableProducts.size() > 0) {
                if (gameListener != null) {
                    if(type.equals(BillingClient.ProductType.INAPP)){
                        gameListener.onGetProductsInAppSuccess(availableProducts);
                    }
                    else{
                        gameListener.onGetProductsSubsSuccess(availableProducts);
                    }

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
                                        for(ProductInfo refTtem : reqListProduct){
                                            if(item.getProductId().equals(refTtem.getProductId())){
                                                ProductInfo productInfo = new ProductInfo();
                                                productInfo.clone(refTtem);
                                                productInfo.setProductDetails(item);
                                                /*if(
                                                        productInfo.getProductType().equals(KcattaConstants.PRODUCT_TYPE_CONSUMABLE) ||
                                                        productInfo.getProductType().equals(KcattaConstants.PRODUCT_TYPE_NON_CONSUMABLE)
                                                ){
                                                    String priceInApp = item.getOneTimePurchaseOfferDetails().getFormattedPrice();
                                                    productInfo.setProductPrice(priceInApp);
                                                }
                                                else{
                                                    ProductDetails.SubscriptionOfferDetails itemDetail = productInfo.findOfferDetail();
                                                    Log.d("TAG", "getOfferId: "+itemDetail.getOfferId());
                                                    Log.d("TAG", "getBasePlanId: "+itemDetail.getBasePlanId());
                                                    String priceInApp = itemDetail.getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                                                    Log.d("TAG", "priceInApp: "+priceInApp);
                                                    productInfo.setProductPrice(priceInApp);
                                                }*/
                                                productInfo.setProductName(item.getTitle());
                                                productInfo.setProductDescription(item.getDescription());
                                                availableProducts.add(productInfo);
                                            }
                                        }

                                    }
                                }
                                Log.d("TAG", "list add count products: " + availableProducts.size());
                                if (gameListener != null) {
                                    if(type.equals(BillingClient.ProductType.INAPP)){
                                        gameListener.onGetProductsInAppSuccess(availableProducts);
                                    }
                                    else{
                                        gameListener.onGetProductsSubsSuccess(availableProducts);
                                    }
                                }
                            }
                            else{
                                Log.d("TAG", "error to get products: " + availableProducts.size());
                                if (gameListener != null) {
                                    gameListener.onGetProductError(type,new Error(billingResult.getDebugMessage()));
                                }
                            }
                        }
                    }
            );
        }
    }

    public void requestPriceProductV4(List<ProductInfo> reqListProduct){
        if (availableInappProducts != null) {
            if(availableInappProducts.size() > 0) {
                if (gameListener != null) {
                    gameListener.onGetProductsInAppSuccess(availableInappProducts);
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
                            gameListener.onGetProductsInAppSuccess(availableInappProducts);
                        }
                    }
                    else{
                        Log.d("TAG", "error to get products: " + availableInappProducts.size());
                        if (gameListener != null) {
                            gameListener.onGetProductError(BillingClient.SkuType.INAPP,new Error(billingResult.getDebugMessage()));
                        }
                    }
                }
            });
        }
    }

    public void init(Activity hostedActivity, AppInfo appInfo){
        this.hostedActivity = hostedActivity;
        this.appInfo = appInfo;
        initOneSignal();
        initBilling();
        initAds();
    }

    private void initAds(){
        if(this.hostedActivity != null) {
            MobileAds.initialize(this.hostedActivity, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.d(TAG, "onInitializationComplete");
                }
            });

            if(this.appInfo.testDevices != null) {
                if (!this.appInfo.tagForChildDirectedTreatment) {
                    RequestConfiguration requestConfiguration
                            = new RequestConfiguration.Builder()
                            .setTestDeviceIds(this.appInfo.testDevices)
                            .build();
                    MobileAds.setRequestConfiguration(requestConfiguration);
                } else {
                    RequestConfiguration requestConfiguration
                            = new RequestConfiguration.Builder()
                            .setTestDeviceIds(this.appInfo.testDevices)
                            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                            //.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                            .build();
                    MobileAds.setRequestConfiguration(requestConfiguration);
                }
            }
        }
    }

    public void setGameListener(KcattaListener gameListener){
        this.gameListener = gameListener;
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
            }
        }
    };

    private void initOneSignal(){
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this.hostedActivity);
        OneSignal.setAppId(appInfo.oneSignalId);

        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications();
    }

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

    /**
     * This function to upgrade or downgrade payment
     * @param newProductId
     * @param oldProductId
     */
    public void updatePayV5(String newProductId,String newBasePlanId,String newOfferId,String oldProductId) {

        final ProductInfo oldProductInfo = findProductInfo(availableSubsProducts, oldProductId);
        final ProductInfo newProductInfo = findProductInfo(availableSubsProducts, newProductId);
        /*newProductInfo.setPreferOfferId(newOfferId);
        newProductInfo.setPreferBasePlanId(newBasePlanId);*/
        if (oldProductInfo != null && newProductInfo != null) {
            if (billingClient != null && billingClient.isReady()) {
                QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build();
                billingClient.queryPurchasesAsync(queryPurchasesParams, new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (list.size() > 0) {
                                for (int i=0;i<list.size();i++){
                                    Purchase purchaseItem = list.get(i);
                                    String productId = purchaseItem.getProducts().get(0);
                                    if(productId != null && productId.equals(oldProductId)){

                                        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                                        //find best offer
                                        ProductDetails.SubscriptionOfferDetails offerDetail = newProductInfo.findOfferDetail(newBasePlanId,newOfferId);
                                        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(newProductInfo.getProductDetails())
                                                .setOfferToken(offerDetail.getOfferToken())
                                                .build();
                                        productDetailsParamsList.add(productDetailsParams);
                                        //replace old purchase
                                        BillingFlowParams.SubscriptionUpdateParams subscriptionUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                                .setOldPurchaseToken(purchaseItem.getPurchaseToken())
                                                .setReplaceProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_FULL_PRICE)
                                                .build();

                                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                .setProductDetailsParamsList(productDetailsParamsList)
                                                .setSubscriptionUpdateParams(subscriptionUpdateParams)
                                                .build();

                                        int responseCode = billingClient.launchBillingFlow(hostedActivity, billingFlowParams).getResponseCode();
                                    }
                                }

                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Pay product by product id and type product
     * @param productId
     * @param preferBasePlanId
     * @param preferOfferId
     * @param type : BillingClient.ProductType.INAPP or BillingClient.ProductType.SUBS
     */
    public void payProductV5(String productId,String preferBasePlanId,String preferOfferId, String type){
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
                        gameListener.onPayProductError(KcattaConstants.UN_KNOW,new Error(productId));
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
                        ProductDetails.SubscriptionOfferDetails offerDetail = product.findOfferDetail(preferBasePlanId,preferOfferId);
                        //for ( ProductDetails.SubscriptionOfferDetails i : product.getProductDetails().getSubscriptionOfferDetails()) {
                            Log.d(TAG, "ProductDetails.SubscriptionOfferDetails: "+offerDetail.getOfferToken());
                            Log.d(TAG, "ProductDetails.SubscriptionOfferDetails: "+offerDetail.getOfferId());
                            Log.d(TAG, "ProductDetails.SubscriptionOfferDetails: "+offerDetail.getBasePlanId());
                            Log.d(TAG, "=======");
                        //}
                        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(product.getProductDetails())
                                .setOfferToken(offerDetail.getOfferToken())
                                .build();
                        productDetailsParamsList.add(productDetailsParams);

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                //.setObfuscatedAccountId("congtt")
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();
                        int responseCode = billingClient.launchBillingFlow(hostedActivity, billingFlowParams).getResponseCode();

                    }
                } else {
                    if (gameListener != null) {
                        gameListener.onPayProductError(KcattaConstants.UN_KNOW,new Error(productId));
                    }
                }
            }
        }
    }

    public void payProductV4(String productId){
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
                    gameListener.onPayProductError(KcattaConstants.UN_KNOW,new Error(productId));
                }
            }
        }
    }

    public void queryPurchase(String productType){
        if(!Utils.isOnline(hostedActivity)){
            if (gameListener != null) {
                gameListener.onQueryError(KcattaConstants.NO_INTERNET_CONNECTION,new Error("No Internet Connection"));
            }
            return;
        }
        if(billingClient != null && billingClient.isReady()){
            QueryPurchasesParams queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(productType).build();
            billingClient.queryPurchasesAsync(queryPurchasesParams, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        if(list.size() > 0) {
                            for (int i = 0; i < list.size(); i++) {
                                Purchase item = list.get(i);
                                boolean lastItem = false;
                                if (i == list.size() - 1) {
                                    lastItem = true;
                                }
                                if (productType.equals(BillingClient.ProductType.INAPP)) {
                                    if (gameListener != null) {
                                        gameListener.onQueryProductInApp(item, lastItem);
                                    }
                                } else if (productType.equals(BillingClient.ProductType.SUBS)) {
                                    if (gameListener != null) {
                                        gameListener.onQueryProductSubs(item, lastItem);
                                    }
                                }
                            }
                        }
                        else{
                            if (gameListener != null) {
                                gameListener.onQueryError(KcattaConstants.QUERY_PRODUCT_NOT_AVAILABLE,new Error("Product is not available"));
                            }
                        }
                    }
                    else{
                        Log.d(TAG,"billingResult.getResponseCode():" +billingResult.getResponseCode());
                        if (gameListener != null) {
                            gameListener.onQueryError(billingResult.getResponseCode(),new Error(billingResult.getDebugMessage()));
                        }
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

    private void handlePurchase(Purchase purchase) {
        Log.d(TAG, "handlePurchase");
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            String productID = purchase.getProducts().get(0);
            ProductInfo productInfo = findProductInfo(availableInappProducts,productID);
            if(productInfo == null){
                productInfo = findProductInfo(availableSubsProducts,productID);
            }
            if(productInfo.getProductType().equals(KcattaConstants.PRODUCT_TYPE_CONSUMABLE)) {
                consumeAsync(purchase, purchase.getPurchaseToken());
            }
            else{
                acknowledgeAsync(purchase, purchase.getPurchaseToken());
            }

        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            if (gameListener != null) {
                gameListener.onPayProductError(KcattaConstants.UN_KNOW,new Error("Có lỗi trong quá trình thanh toán"));
            }

        } else {
            if (gameListener != null) {
                gameListener.onPayProductError(KcattaConstants.UN_KNOW,new Error("Có lỗi trong quá trình thanh toán"));
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
            gameListener.onPayProductSuccess(purchase,productId);
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

    public void createRewardedAd(final String unitId){
        if(mRewardedAd == null) {
            //Bundle extras = new Bundle();
            //add https://developers.google.com/admob/android/test-creative-types
            //extras.putString("ft_ctype", "video_app_install");

            AdRequest adRequest = new AdRequest.Builder()
                    //.addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            RewardedAd.load(this.hostedActivity, unitId,
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.toString());
                            if(gameListener != null){
                                gameListener.onAdFailedToLoad(KcattaConstants.ADS_TYPE_REWARDED,unitId,loadAdError);
                            }
                            mRewardedAd = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            Log.d(TAG, "Ad was loaded.");
                            mRewardedAd = rewardedAd;
                            if(gameListener != null){
                                gameListener.onAdLoaded(KcattaConstants.ADS_TYPE_REWARDED,unitId);
                            }
                            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    // Called when a click is recorded for an ad.
                                    Log.d(TAG, "Ad was clicked.");
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    Log.d(TAG, "Ad dismissed fullscreen content.");
                                    if(gameListener != null){
                                        gameListener.onAdClosed(KcattaConstants.ADS_TYPE_REWARDED,unitId);
                                    }
                                    mRewardedAd = null;
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    // Called when ad fails to show.
                                    Log.e(TAG, "Ad failed to show fullscreen content.");
                                    if(gameListener != null){
                                        gameListener.onAdFailedToLoad(KcattaConstants.ADS_TYPE_REWARDED,unitId,adError);
                                    }

                                    mRewardedAd = null;
                                }

                                @Override
                                public void onAdImpression() {
                                    // Called when an impression is recorded for an ad.
                                    Log.d(TAG, "Ad recorded an impression.");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when ad is shown.
                                    Log.d(TAG, "Ad showed fullscreen content.");
                                    if(gameListener != null){
                                        gameListener.onAdOpened(KcattaConstants.ADS_TYPE_REWARDED,unitId);
                                    }
                                }
                            });
                        }
                    });
        }
    }

    public void createInterstitalAd(final String unitId){
        if(mInterstitialAd == null) {
            AdRequest adRequestBuilder = new AdRequest.Builder().build();
            InterstitialAd.load(this.hostedActivity, unitId, adRequestBuilder, new InterstitialAdLoadCallback() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, "Interstitial onAdFailedToLoad");
                    super.onAdFailedToLoad(loadAdError);
                    if(gameListener != null){
                        gameListener.onAdFailedToLoad(KcattaConstants.ADS_TYPE_INTERSTITIAL,unitId,loadAdError);
                    }
                    mInterstitialAd = null;
                }

                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    Log.d(TAG, "Interstitial onAdLoaded");
                    super.onAdLoaded(interstitialAd);
                    if(gameListener != null){
                        gameListener.onAdLoaded(KcattaConstants.ADS_TYPE_INTERSTITIAL,unitId);
                    }
                    mInterstitialAd = interstitialAd;
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG, "Ad dismissed fullscreen content.");
                            if(gameListener != null){
                                gameListener.onAdClosed(KcattaConstants.ADS_TYPE_INTERSTITIAL,unitId);
                            }
                            mInterstitialAd = null;
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when ad fails to show.
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            if(gameListener != null){
                                gameListener.onAdFailedToLoad(KcattaConstants.ADS_TYPE_INTERSTITIAL,unitId,adError);
                            }
                            mInterstitialAd = null;
                        }

                        @Override
                        public void onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.");
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.");
                            if(gameListener != null){
                                gameListener.onAdOpened(KcattaConstants.ADS_TYPE_INTERSTITIAL,unitId);
                            }
                        }
                    });
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    public void addOrCreateBannerAd(final String bannerId, String aligment){
        if(mBannerView == null && hostedActivity != null){
            mBannerView = new AdView(this.hostedActivity);
            mBannerView.setVisibility(View.GONE);
            if(mBannerView != null){
                mBannerView.setAdListener(new AdListener() {
                    public void onAdClosed() {
                        Log.d(TAG,"onAdClosed");
                        if(gameListener != null){
                            gameListener.onAdClosed(KcattaConstants.ADS_TYPE_BANNER,bannerId);
                        }
                    }

                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        String msg = loadAdError.getMessage();
                        Log.d(TAG,"onAdFailedToLoad " + msg);
                        if(gameListener != null){
                            gameListener.onAdFailedToLoad(KcattaConstants.ADS_TYPE_BANNER,bannerId,loadAdError);
                        }
                    }

                    public void onAdLeftApplication() {
                        Log.d(TAG,"onAdLeftApplication");
                    }

                    public void onAdLoaded() {
                        Log.d(TAG, "onAdLoaded");
                        if(gameListener != null){
                            gameListener.onAdLoaded(KcattaConstants.ADS_TYPE_BANNER,bannerId);
                        }
                    }

                    public void onAdOpened() {
                        Log.d(TAG,"onAdOpened");
                        if(gameListener != null){
                            gameListener.onAdOpened(KcattaConstants.ADS_TYPE_BANNER,bannerId);
                        }
                    }
                });
            }
            String orientation = "landscape";
            int orient= this.hostedActivity.getResources().getConfiguration().orientation;
            if (orient == Configuration.ORIENTATION_LANDSCAPE) {
                // In landscape
                orientation = "landscape";
            } else {
                // In portrait
                orientation = "portrait";
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

    public void hideBannerAd(){
        if(mBannerView != null){
            mBannerView.setVisibility(View.GONE);
        }
    }

    public void showBannerAd(){
        if(mBannerView != null){
            mBannerView.setVisibility(View.VISIBLE);
        }
    }

    public void showIntestitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this.hostedActivity);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }
    public void showRewardedAd(){
        if (mRewardedAd != null) {
            mRewardedAd.show(this.hostedActivity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    if(gameListener != null){
                        gameListener.onAdEarnedReward(KcattaConstants.ADS_TYPE_REWARDED,mRewardedAd.getAdUnitId(),rewardAmount);
                    }
                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }
}
