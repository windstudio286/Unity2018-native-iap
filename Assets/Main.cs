using System.Collections;
using System.Collections.Generic;
using System.Text;
using LitJson;
using UnityEngine;
using System.Runtime.InteropServices;
public class Main : MonoBehaviour
{
#if UNITY_IPHONE
    [DllImport("__Internal")] public static extern void sendDataFromUnity(string key, string value);
#endif
    public UnityEngine.UI.Text text;
    // Start is called before the first frame update
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

    }

    public void onBtnLogEventClick()
    {
        JsonData parameters = new JsonData();
        parameters[FirebaseManager.PARAM_LEVEL_NAME] = "level_100";
        FirebaseManager.Instance.LogEvent(FirebaseManager.EVENT_LEVEL_START, parameters);
    }
    public void onBtnFetchAndActiveClick()
    {
        FirebaseManager.Instance.remoteConfigFetchAndActiveAction = (success) =>
        {
            Debug.Log("onBtnFetchAndActiveClick: " + success);
            Debug.Log("onBtnFetchAndActiveClick: "+FirebaseManager.Instance.RemoteConfigGetString("level_1"));
        };
        FirebaseManager.Instance.RemoteConfigFetchAndActive();
    }

    public void onBtnDowngradeClick()
    {
        if (AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdBanner()))
        {
            AdManager.Instance.ShowAd(AdManager.Instance.GetDefaultAdBanner());
        }
        return;
        StringBuilder sb = new StringBuilder();
        JsonWriter writer = new JsonWriter(sb);
        writer.WriteObjectStart();
        writer.WritePropertyName("receivedObject");
        writer.Write(this.gameObject.name);
        writer.WritePropertyName("receivedFunc");
        writer.Write("setTextFromNative");
        writer.WritePropertyName("key");
        writer.Write("DOWNGRADE_PRODUCT");
        writer.WritePropertyName("value");
        writer.Write("vn.vplay.sdk.t000a.demoproduct1");
        writer.WriteObjectEnd();
        Debug.Log("onBtnPayClick:" + sb.ToString());
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
        UnityPlayerNativeActivity.Call("sendDataFromUnity", sb.ToString());

#endif

#if UNITY_IPHONE

        sendDataFromUnity("DOWNGRADE_PRODUCT", sb.ToString());

#endif
    }

    public void onBtnUpgradeClick()
    {
        if (!AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdBanner()))
        {
            AdInfo adInfo = new AdInfo();
            adInfo.adType = AdManager.BannerAd;
            adInfo.adViewOnLoaded = (adId, isLoaded) =>
            {
                Debug.Log("Banner adViewOnLoaded:" + adId);
                Debug.Log("Banner adViewOnLoaded:" + isLoaded);
            };
            adInfo.adViewOnFail = (adId, message) =>
            {
                Debug.Log("Banner adViewOnFail:" + adId);
                Debug.Log("Banner adViewOnFail:" + message);
            };
            AdManager.Instance.LoadAdById(AdManager.Instance.GetDefaultAdBanner(), adInfo);
        }
        return;
        StringBuilder sb = new StringBuilder();
        JsonWriter writer = new JsonWriter(sb);
        writer.WriteObjectStart();
        writer.WritePropertyName("receivedObject");
        writer.Write(this.gameObject.name);
        writer.WritePropertyName("receivedFunc");
        writer.Write("setTextFromNative");
        writer.WritePropertyName("key");
        writer.Write("UPGRADE_PRODUCT");
        writer.WritePropertyName("value");
        writer.Write("vn.vplay.sdk.t000a.demoproduct1");
        writer.WriteObjectEnd();
        Debug.Log("onBtnPayClick:" + sb.ToString());
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
        UnityPlayerNativeActivity.Call("sendDataFromUnity", sb.ToString());

#endif

#if UNITY_IPHONE

        sendDataFromUnity("UPGRADE_PRODUCT", sb.ToString());

#endif
    }

    public void onBtnLoadRewardedAdClick()
    {
        if (!AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdReward()))
        {
            AdInfo adInfo = new AdInfo();
            adInfo.adType = AdManager.RewardAd;
            adInfo.adViewOnLoaded = (adId, isLoaded) =>
            {
                Debug.Log("Reward adViewOnLoaded:" + adId);
                Debug.Log("Reward adViewOnLoaded:" + isLoaded);
            };
            adInfo.adViewOnFail = (adId, message) =>
            {
                Debug.Log("Reward adViewOnFail:" + adId);
                Debug.Log("Reward adViewOnFail:" + message);
            };
            adInfo.adViewOnDidPresent = (adId) =>
            {
                Debug.Log("Reward adViewOnDidPresent:" + adId);
            };
            adInfo.adViewOnDismissPresent = (adId) =>
            {
                Debug.Log("Reward adViewOnDismissPresent:" + adId);
            };
            adInfo.adViewOnReward = (adId,data) =>
            {
                Debug.Log("Reward adViewOnReward:" + adId);
                Debug.Log("Reward adViewOnReward:" + data);
            };
            AdManager.Instance.LoadAdById(AdManager.Instance.GetDefaultAdReward(), adInfo);
        }
    }

    public void onBtnShowRewardedAdClick()
    {
        if (AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdReward()))
        {
            AdManager.Instance.ShowAd(AdManager.Instance.GetDefaultAdReward());
        }
    }
    public void onBtnHideBannerAdClick()
    {
        if (AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdBanner()))
        {
            AdManager.Instance.HideAd(AdManager.Instance.GetDefaultAdBanner());
        }
    }

        public void onBtnPayClick()
    {
        if (AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdInterstitial()))
        {
            AdManager.Instance.ShowAd(AdManager.Instance.GetDefaultAdInterstitial());
        }
        return;
        StringBuilder sb = new StringBuilder();
        JsonWriter writer = new JsonWriter(sb);
        writer.WriteObjectStart();
        writer.WritePropertyName("receivedObject");
        writer.Write(this.gameObject.name);
        writer.WritePropertyName("receivedFunc");
        writer.Write("setTextFromNative");
        writer.WritePropertyName("key");
        writer.Write("PAY_PRODUCT");
        writer.WritePropertyName("value");
        writer.Write("vn.vplay.sdk.t000a.demoproduct1");
        writer.WriteObjectEnd();
        Debug.Log("onBtnPayClick:" + sb.ToString());
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
        UnityPlayerNativeActivity.Call("sendDataFromUnity", sb.ToString());

#endif
#if UNITY_IPHONE

        sendDataFromUnity("PAY_PRODUCT", sb.ToString());

#endif
    }

    public void onButtonClick()
    {
        if (!AdManager.Instance.isAvailableAd(AdManager.Instance.GetDefaultAdInterstitial()))
        {
            AdInfo adInfo = new AdInfo();
            adInfo.adType = AdManager.InterstitialAd;
            adInfo.adViewOnLoaded = (adId, isLoaded) =>
            {
                Debug.Log("Interstital adViewOnLoaded:" + adId);
                Debug.Log("Interstital adViewOnLoaded:" + isLoaded);
            };
            adInfo.adViewOnFail = (adId, message) =>
            {
                Debug.Log("Interstital adViewOnFail:" + adId);
                Debug.Log("Interstital adViewOnFail:" + message);
            };
            adInfo.adViewOnDidPresent = (adId) =>
            {
                Debug.Log("Interstital adViewOnDidPresent:" + adId);
            };
            adInfo.adViewOnDismissPresent = (adId) =>
            {
                Debug.Log("Interstital adViewOnDismissPresent:" + adId);
            };
            AdManager.Instance.LoadAdById(AdManager.Instance.GetDefaultAdInterstitial(), adInfo);
        }
        return;
        StringBuilder sb = new StringBuilder();
        JsonWriter writer = new JsonWriter(sb);
        writer.WriteObjectStart();
        writer.WritePropertyName("receivedObject");
        writer.Write(this.gameObject.name);
        writer.WritePropertyName("receivedFunc");
        writer.Write("setTextFromNative");
        writer.WritePropertyName("key");
        writer.Write("GET_PRODUCT");
        writer.WriteObjectEnd();
        Debug.Log("onButtonClick:" + sb.ToString());
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
			AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
			UnityPlayerNativeActivity.Call("sendDataFromUnity", sb.ToString());

#endif
#if UNITY_IPHONE

        sendDataFromUnity("GET_PRODUCT", sb.ToString());

#endif
    }

    void setTextFromNative(string newValue)
    {
        if (text != null)
        {
            text.text = newValue;
        }
    }
}
