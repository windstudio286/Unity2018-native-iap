package vn.vplay.sdk.t000a;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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
    private List<ProductInfo> availableProducts;
    public static KcattaSdk GetInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KcattaSdk();
        }
        return INSTANCE;
    }

    public boolean isBillingReady(){
        if(billingClient != null){
            return billingClient.isReady();
        }
        initBilling();
        return false;
    }

    public void RequestPriceProduct(List<ProductInfo> reqListProduct){
        if (availableProducts != null) {
            if(availableProducts.size() > 0) {
                if (gameListener != null) {
                    gameListener.onGetProductsSuccess(availableProducts);
                }
                return;
            }
        }
        if(availableProducts == null){
            availableProducts = new ArrayList<>();
        }
        availableProducts.clear();
        Log.d("TAG", "reqListProduct count" + reqListProduct.size());
        if (billingClient.isReady()) {
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
                            availableProducts.add(productInfo);
                        }
                    }
                    Log.d("TAG", "list add count products: " + availableProducts.size());
                    if (gameListener != null) {
                        gameListener.onGetProductsSuccess(availableProducts);
                    }
                }
            });
        }
    }

    public void Init(Activity hostedActivity){
            this.hostedActivity = hostedActivity;
            initBilling();
    }

    public void SetGameListener(KcattaListener gameListener){
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
                if (billingClient != null && billingClient.isReady()) {

                }
                queryPurchase();
            }
        }
    };

    private void initBilling(){
        if (availableProducts == null) {
            availableProducts = new ArrayList<>();
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

    public void PayProduct(String productId){
        if(availableProducts != null){
            ProductInfo product = findProductInfo(availableProducts,productId);
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
}
