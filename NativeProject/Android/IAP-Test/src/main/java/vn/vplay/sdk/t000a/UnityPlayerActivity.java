package vn.vplay.sdk.t000a;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unity3d.player.*;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UnityPlayerActivity extends Activity implements KcattaListener
{
    private static final String ONESIGNAL_APP_ID = "509fba34-5690-491b-9bda-276958ff3881";
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

    // Override this in your custom UnityPlayerActivity to tweak the command line arguments passed to the Unity Android Player
    // The command line arguments are passed as a string, separated by spaces
    // UnityPlayerActivity calls this from 'onCreate'
    // Supported: -force-gles20, -force-gles30, -force-gles31, -force-gles31aep, -force-gles32, -force-gles, -force-vulkan
    // See https://docs.unity3d.com/Manual/CommandLineArguments.html
    // @param cmdLine the current command line arguments, may be null
    // @return the modified command line string or null
    protected String updateUnityCommandLineArguments(String cmdLine)
    {
        return cmdLine;
    }

    protected HashMap<String, JSONObject> hashMapCmd;

    public String remoteConfigGetString(String remoteKey) {
        return FirebaseRemoteConfig.getInstance().getString(remoteKey);
    }
    public void sendDataFromUnity(String json) {
        Log.i("BEM","sendDataFromUnity: "+json);
        //receivedObject: IAPUnitySingleton
        //receivedFunc: sendDataFromNative
        //key: GET_PRODUCT
        //value: optional
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(jsonObject.has(KcattaConstants.JSON_KEY)){
                String key = jsonObject.getString(KcattaConstants.JSON_KEY);
                if(hashMapCmd != null){
                    hashMapCmd.put(key,jsonObject);
                }
                if(key.equals(KcattaCmd.GET_PRODUCT)){
                    List<ProductInfo> productInfoList = new ArrayList<>();
                    ProductInfo prod1 = new ProductInfo();
                    prod1.setProductId("vn.vplay.sdk.t000a.demoproduct1");
                    prod1.setProductType(KcattaConstants.PRODUCT_TYPE_CONSUMABLE);
                    ProductInfo prod2 = new ProductInfo();
                    prod2.setProductId("vn.vplay.sdk.t000a.demoproduct2");
                    prod2.setProductType(KcattaConstants.PRODUCT_TYPE_CONSUMABLE);
                    ProductInfo prod3 = new ProductInfo();
                    prod3.setProductId("vn.vplay.sdk.t000a.subs1");
                    prod3.setProductType(KcattaConstants.PRODUCT_TYPE_SUBS);
                    ProductInfo prod4 = new ProductInfo();
                    prod4.setProductId("vn.vplay.sdk.t000a.removeads");
                    prod4.setProductType(KcattaConstants.PRODUCT_TYPE_NON_CONSUMABLE);
                    ProductInfo prod5 = new ProductInfo();
                    prod5.setProductId("vn.vplay.sdk.t000a.subs2");
                    prod5.setProductType(KcattaConstants.PRODUCT_TYPE_SUBS);
                    productInfoList.add(prod1);
                    productInfoList.add(prod2);
                    productInfoList.add(prod3);
                    productInfoList.add(prod4);
                    productInfoList.add(prod5);
                    KcattaSdk.getInstance().requestPriceProductV5(productInfoList,BillingClient.ProductType.INAPP);
                    /*this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //KcattaSdk.getInstance().addOrCreateBannerAd("ca-app-pub-3940256099942544/6300978111","bottom");
                            //KcattaSdk.getInstance().createInterstitalAd("ca-app-pub-3940256099942544/8691691433");
                            KcattaSdk.getInstance().createRewardedAd("ca-app-pub-3940256099942544/5224354917");
                        }
                    });*/
                }
                if(key.equals(KcattaCmd.PAY_PRODUCT)){
                    String value = jsonObject.getString(KcattaConstants.JSON_VALUE);
                    value = "vn.vplay.sdk.t000a.subs1";
                    //value = "vn.vplay.sdk.t000a.removeads";
                    //KcattaSdk.getInstance().payProductV5(value,BillingClient.ProductType.SUBS);
                    KcattaSdk.getInstance().payProductV5("vn.vplay.sdk.t000a.subs1","basic-2-auto-renewal","offer-vip-user-week-custom",BillingClient.ProductType.SUBS);

                }
                if(key.equals(KcattaCmd.UPGRADE_PRODUCT)){
                    String value = jsonObject.getString(KcattaConstants.JSON_VALUE);
                    value = "vn.vplay.sdk.t000a.subs1";
                    //KcattaSdk.getInstance().updatePayV5("vn.vplay.sdk.t000a.subs1","basic-2-auto-renewal","offer-vip-user-week-custom","vn.vplay.sdk.t000a.subs2");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            KcattaSdk.getInstance().hideBannerAd();
                        }
                    });
                }
                if(key.equals(KcattaCmd.DOWNGRADE_PRODUCT)){
                    //KcattaSdk.getInstance().queryPurchase(BillingClient.ProductType.SUBS);
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //KcattaSdk.getInstance().showBannerAd();
                            //KcattaSdk.getInstance().showIntestitialAd();
                            KcattaSdk.getInstance().showRewardedAd();
                        }
                    });
                }
                if(key.equals(KcattaCmd.LOAD_ADS)){
                    JSONObject valueObject = jsonObject.getJSONObject(KcattaConstants.JSON_VALUE);
                    String adId = valueObject.getString("adId");
                    int adType = valueObject.getInt("adType");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(adType == KcattaConstants.INT_BANNER_TYPE){
                                KcattaSdk.getInstance().addOrCreateBannerAd(adId,"top");
                            }
                            if(adType == KcattaConstants.INT_INTERSTITIAL_TYPE){
                                KcattaSdk.getInstance().createInterstitalAd(adId);
                            }
                            if(adType == KcattaConstants.INT_REWARD_TYPE){
                                KcattaSdk.getInstance().createRewardedAd(adId);
                            }
                        }
                    });
                }
                if(key.equals(KcattaCmd.SHOW_ADS)){
                    JSONObject valueObject = jsonObject.getJSONObject(KcattaConstants.JSON_VALUE);
                    String adId = valueObject.getString("adId");
                    int adType = valueObject.getInt("adType");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(adType == KcattaConstants.INT_BANNER_TYPE){
                                KcattaSdk.getInstance().showBannerAd();
                            }
                            if(adType == KcattaConstants.INT_INTERSTITIAL_TYPE){
                                KcattaSdk.getInstance().showIntestitialAd();
                            }
                            if(adType == KcattaConstants.INT_REWARD_TYPE){
                                KcattaSdk.getInstance().showRewardedAd();
                            }
                        }
                    });
                }
                if(key.equals(KcattaCmd.HIDE_ADS)){
                    JSONObject valueObject = jsonObject.getJSONObject(KcattaConstants.JSON_VALUE);
                    String adId = valueObject.getString("adId");
                    int adType = valueObject.getInt("adType");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(adType == KcattaConstants.INT_BANNER_TYPE){
                                KcattaSdk.getInstance().hideBannerAd();
                            }
                        }
                    });
                }
                if(key.equals(KcattaCmd.LOG_EVENT)){
                    JSONObject valueObject = jsonObject.getJSONObject(KcattaConstants.JSON_VALUE);
                    String eventName = valueObject.getString("eventName");
                    JSONObject parametersObject = valueObject.getJSONObject("parameters");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bundle params = Utils.convertJsonToBundle(parametersObject);
                                Utils.logFirebaseEvent(UnityPlayerActivity.this,eventName,params);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                if(key.equals(KcattaCmd.LOG_USER_PROPS)){
                    JSONObject valueObject = jsonObject.getJSONObject(KcattaConstants.JSON_VALUE);
                    String nameProp = valueObject.getString("nameProp");
                    String valueProp = valueObject.getString("valueProp");
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Utils.logUserProperty(UnityPlayerActivity.this,nameProp,valueProp);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                if(key.equals(KcattaCmd.FETCH_ACTIVE)){
                    FirebaseRemoteConfig.getInstance().fetchAndActivate()
                            .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull Task<Boolean> task) {
                                    if (task.isSuccessful()) {
                                        boolean updated = task.getResult();
                                        Log.d("BEM", "Config params updated: " + updated);
                                        Toast.makeText(UnityPlayerActivity.this, "Fetch and activate succeeded",
                                                Toast.LENGTH_SHORT).show();
                                        /*for (Map.Entry<String, FirebaseRemoteConfigValue> e : FirebaseRemoteConfig.getInstance().getAll().entrySet()) {
                                            Log.d("BEM", "e.getKey(): " + e.getKey());
                                            Log.d("BEM", "e.getValue(): " + e.getValue().asString());

                                        }*/
                                        if(hashMapCmd != null){
                                            JSONObject json = hashMapCmd.get(KcattaCmd.FETCH_ACTIVE);
                                            if(json != null){
                                                String receivedObject = null;
                                                String receivedFunc = null;
                                                try {
                                                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                                                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                if(receivedObject != null && receivedFunc != null){
                                                    Log.i("BEM","sendDataFromNative");
                                                    if(mUnityPlayer != null){
                                                        Gson gson = new GsonBuilder()
                                                                .excludeFieldsWithoutExposeAnnotation()
                                                                .create();
                                                        KcattaResponse response = new KcattaResponse();
                                                        response.setSuccess(true);
                                                        response.setKey(KcattaCmd.FETCH_ACTIVE);
                                                        String jsonData = gson.toJson(response);
                                                        Log.i("BEM",jsonData);
                                                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(UnityPlayerActivity.this, "Fetch failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        String cmdLine = updateUnityCommandLineArguments(getIntent().getStringExtra("unity"));
        getIntent().putExtra("unity", cmdLine);

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        hashMapCmd = new HashMap<>();
        KcattaSdk sdk = KcattaSdk.getInstance();
        AppInfo appInfo = new AppInfo();
        appInfo.oneSignalId = ONESIGNAL_APP_ID;
        sdk.init(this,appInfo);
        sdk.setGameListener(this);
        /*
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications();*/
        //Config Remote Config
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.destroy();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
        Log.i("BEM","onPause");
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
        Log.i("BEM","onResume");
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
        Log.i("BEM","onStop");
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }

    @Override
    public void onPayProductSuccess(Purchase item,String productId) {
        Log.i("BEM","onPayProductSuccess productId: "+productId);
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.PAY_PRODUCT);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.PAY_PRODUCT);
                        response.setMessage(productId);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onPayProductError(int errorCode,Error error) {
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.PAY_PRODUCT);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(false);
                        response.setKey(KcattaCmd.PAY_PRODUCT);
                        response.setMessage(error.getMessage());
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onGetProductsSubsSuccess(List<ProductInfo> products) {

    }

    @Override
    public void onGetProductsInAppSuccess(List<ProductInfo> products) {
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.GET_PRODUCT);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.GET_PRODUCT);
                        response.setMessage(products);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }

            }
        }
    }

    @Override
    public void onGetProductError(String productType,Error error) {
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.PAY_PRODUCT);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(false);
                        response.setKey(KcattaCmd.GET_PRODUCT);
                        response.setMessage("");
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onAdFailedToLoad(String adType, String adId, AdError error) {
        Log.i("BEM","onAdFailedToLoad adType: "+adType);
        Log.i("BEM","onAdFailedToLoad adId: "+adId);
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.LOAD_ADS);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.LOAD_ADS);
                        AdInfo adInfo = new AdInfo();
                        adInfo.setState(0);
                        adInfo.setAdId(adId);
                        response.setMessage(adInfo);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onAdLoaded(String adType, String adId) {
        Log.i("BEM","onAdLoaded adType: "+adType);
        Log.i("BEM","onAdLoaded adId: "+adId);
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.LOAD_ADS);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.LOAD_ADS);
                        AdInfo adInfo = new AdInfo();
                        adInfo.setState(1);
                        adInfo.setAdId(adId);
                        response.setMessage(adInfo);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onAdClosed(String adType, String adId) {
        Log.i("BEM","onAdClosed adType: "+adType);
        Log.i("BEM","onAdClosed adId: "+adId);
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.SHOW_ADS);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.SHOW_ADS);
                        AdInfo adInfo = new AdInfo();
                        adInfo.setState(102);
                        adInfo.setAdId(adId);
                        response.setMessage(adInfo);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onAdOpened(String adType, String adId) {
        Log.i("BEM","onAdOpened adType: "+adType);
        Log.i("BEM","onAdOpened adId: "+adId);
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.SHOW_ADS);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.SHOW_ADS);
                        AdInfo adInfo = new AdInfo();
                        adInfo.setState(100);
                        adInfo.setAdId(adId);
                        response.setMessage(adInfo);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onAdEarnedReward(String adType, String adId, int rewardAmount) {
        Log.i("BEM","onAdEarnedReward adType: "+adType);
        Log.i("BEM","onAdEarnedReward adId: "+adId);
        if(hashMapCmd != null){
            JSONObject json = hashMapCmd.get(KcattaCmd.SHOW_ADS);
            if(json != null){
                String receivedObject = null;
                String receivedFunc = null;
                try {
                    receivedObject = json.getString(KcattaConstants.JSON_RECEIVED_OBJECT);
                    receivedFunc = json.getString(KcattaConstants.JSON_RECEIVED_FUNCTION);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(receivedObject != null && receivedFunc != null){
                    Log.i("BEM","sendDataFromNative");
                    if(mUnityPlayer != null){
                        Gson gson = new GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                        KcattaResponse response = new KcattaResponse();
                        response.setSuccess(true);
                        response.setKey(KcattaCmd.SHOW_ADS);
                        AdInfo adInfo = new AdInfo();
                        adInfo.setState(103);
                        adInfo.setAdId(adId);
                        adInfo.setData(rewardAmount);
                        response.setMessage(adInfo);
                        String jsonData = gson.toJson(response);
                        Log.i("BEM",jsonData);
                        mUnityPlayer.UnitySendMessage(receivedObject,receivedFunc,jsonData);
                    }
                }
            }
        }
    }

    @Override
    public void onQueryProductInApp(List<Purchase> items) {
        for(Purchase item : items) {
            Log.i("BEM", "onQueryProductInApp");
            Log.i("BEM", "item:" + item.getProducts().get(0));
            Log.i("BEM", "time:" + item.getPurchaseTime());
            Log.i("BEM", "orderId:" + item.getOrderId());
            Log.i("BEM", "getObfuscatedProfileId:" + item.getAccountIdentifiers().getObfuscatedProfileId());
            Log.i("BEM", "getObfuscatedAccountId:" + item.getAccountIdentifiers().getObfuscatedAccountId());
            Log.i("BEM", "getOriginalJson:" + item.getOriginalJson());
        }
    }

    @Override
    public void onQueryProductSubs(List<Purchase> items) {
        for(Purchase item : items) {
            Log.i("BEM", "onQueryProductSubs");
            Log.i("BEM", "item:" + item.getProducts().get(0));
        }
    }

    @Override
    public void onQueryError(int errCode,Error error) {
        Log.i("BEM","errCode: "+errCode);
        Log.i("BEM","onQueryError: "+error.getMessage());
    }
}
