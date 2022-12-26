package vn.vplay.sdk.t000a;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unity3d.player.*;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UnityPlayerActivity extends Activity implements KcattaListener
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    protected HashMap<String,JSONObject> hashMapCmd;
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
                    ProductInfo prod2 = new ProductInfo();
                    prod2.setProductId("vn.vplay.sdk.t000a.demoproduct2");
                    productInfoList.add(prod1);
                    productInfoList.add(prod2);
                    KcattaSdk.GetInstance().RequestPriceProduct(productInfoList);
                }
                if(key.equals(KcattaCmd.PAY_PRODUCT)){
                    String value = jsonObject.getString(KcattaConstants.JSON_VALUE);
                    KcattaSdk.GetInstance().PayProduct(value);
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

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
        hashMapCmd = new HashMap<>();
        KcattaSdk sdk = KcattaSdk.GetInstance();
        sdk.Init(this);
        sdk.SetGameListener(this);

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
        KcattaSdk.GetInstance().destroySDK();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
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
    public void onPayProductSuccess(String productId) {
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
    public void onPayProductError(Error error) {
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
    public void onGetProductsSuccess(List<ProductInfo> products) {
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
    public void onGetProductError(Error error) {

    }
}
