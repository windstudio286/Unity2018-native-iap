using System.Collections.Generic;
using System.Runtime.InteropServices;
using LitJson;
using System.Text;
using UnityEngine;

public class AdInfo
{
    public int adType;
    public bool isLoaded;
    public System.Action<string, bool> adViewOnLoaded;
    public System.Action<string, string> adViewOnFail;
    public System.Action<string> adViewOnDidPresent;
    public System.Action<string> adViewOnDismissPresent;
    public System.Action<string, int> adViewOnReward;
}

public class AdManager : MonoBehaviour
{
    public const int BannerAd = 0;
    public const int RewardAd = 1;
    public const int InterstitialAd = 2;

#if UNITY_IPHONE
    [DllImport("__Internal")] public static extern void sendDataFromUnity(string jsonString);
#endif
    private Dictionary<string, AdInfo> adHashMap = new Dictionary<string, AdInfo>();
    private Dictionary<string, int> confAdHashMap = new Dictionary<string, int>();
    private static AdManager _instance;
    public static AdManager Instance
    {
        get
        {
            return _instance;
        }
    }
    void Awake()
    {
        //Check if there is already an instance of AdsManager
        if (_instance == null)
            //if not, set it to this.
            _instance = this;
        //If instance already exists:
        else if (_instance != this)
            //Destroy this, this enforces our singleton pattern so there can only be one instance of AdsManager.
            Destroy(gameObject);

        //Set AdsManager to DontDestroyOnLoad so that it won't be destroyed when reloading our scene.
        DontDestroyOnLoad(gameObject);
    }
    void configAd()
    {
        if (confAdHashMap == null)
        {
            confAdHashMap = new Dictionary<string, int>();
        }
        confAdHashMap.Clear();
        confAdHashMap.Add(GetDefaultAdBanner(), BannerAd);
        confAdHashMap.Add(GetDefaultAdInterstitial(), InterstitialAd);
        confAdHashMap.Add(GetDefaultAdReward(), RewardAd);
    }
    private void SendJsonString(string jsonString)
    {
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
        UnityPlayerNativeActivity.Call("sendDataFromUnity", jsonString);

#endif

#if UNITY_IPHONE

        sendDataFromUnity(jsonString);
#endif

    }

    public void LoadAdById(string adId, AdInfo adInfo)
    {
        if (adHashMap.ContainsKey(adId))
        {
            adHashMap.Remove(adId);
        }
        if (adInfo != null)
        {
            adHashMap.Add(adId, adInfo);
            StringBuilder sb = new StringBuilder();
            JsonWriter writer = new JsonWriter(sb);
            writer.WriteObjectStart();
            writer.WritePropertyName("receivedObject");
            writer.Write(this.gameObject.name);
            writer.WritePropertyName("receivedFunc");
            writer.Write("setTextFromNative");
            writer.WritePropertyName("key");
            writer.Write("LOAD_ADS");
            writer.WritePropertyName("value");
            writer.WriteObjectStart();
            writer.WritePropertyName("adId");
            writer.Write(adId);
            writer.WritePropertyName("adType");
            writer.Write(adInfo.adType);
            writer.WriteObjectEnd();
            writer.WriteObjectEnd();
            Debug.Log("LoadAdById:" + sb.ToString());
            SendJsonString(sb.ToString());
        }
    }

    public void ShowAd(string adId)
    {
        if (isAvailableAd(adId))
        {
            AdInfo adInfo = adHashMap[adId];
            StringBuilder sb = new StringBuilder();
            JsonWriter writer = new JsonWriter(sb);
            writer.WriteObjectStart();
            writer.WritePropertyName("receivedObject");
            writer.Write(this.gameObject.name);
            writer.WritePropertyName("receivedFunc");
            writer.Write("setTextFromNative");
            writer.WritePropertyName("key");
            writer.Write("SHOW_ADS");
            writer.WritePropertyName("value");
            writer.WriteObjectStart();
            writer.WritePropertyName("adId");
            writer.Write(adId);
            writer.WritePropertyName("adType");
            writer.Write(adInfo.adType);
            writer.WriteObjectEnd();
            writer.WriteObjectEnd();
            Debug.Log("LoadAdById:" + sb.ToString());
            SendJsonString(sb.ToString());
        }
    }
    public void HideAd(string adId)
    {
        if (isAvailableAd(adId))
        {
            AdInfo adInfo = adHashMap[adId];
            StringBuilder sb = new StringBuilder();
            JsonWriter writer = new JsonWriter(sb);
            writer.WriteObjectStart();
            writer.WritePropertyName("receivedObject");
            writer.Write(this.gameObject.name);
            writer.WritePropertyName("receivedFunc");
            writer.Write("setTextFromNative");
            writer.WritePropertyName("key");
            writer.Write("HIDE_ADS");
            writer.WritePropertyName("value");
            writer.WriteObjectStart();
            writer.WritePropertyName("adId");
            writer.Write(adId);
            writer.WritePropertyName("adType");
            writer.Write(adInfo.adType);
            writer.WriteObjectEnd();
            writer.WriteObjectEnd();
            Debug.Log("LoadAdById:" + sb.ToString());
            SendJsonString(sb.ToString());
        }
    }

    public bool isAvailableAd(string adId)
    {
        bool isAvailabe = false;
        if (adHashMap.ContainsKey(adId))
        {
            AdInfo adInfo = adHashMap[adId];
            if (adInfo != null)
            {
                isAvailabe = adInfo.isLoaded;
            }
        }
        return isAvailabe;
    }

    void setTextFromNative(string newValue)
    {
        Debug.Log("AdManager setTextFromNative:" + newValue);
        JsonData jsonData = JsonMapper.ToObject(newValue);
        string key = (string)jsonData["key"];
        bool success = (bool)jsonData["success"];
        if (success && key.Equals("LOAD_ADS"))
        {
            JsonData jsonValueData = jsonData["value"];
            string adId = (string)jsonValueData["adId"];
            int state = (int)jsonValueData["state"];
            if (adHashMap.ContainsKey(adId))
            {
                AdInfo adInfo = adHashMap[adId];

                if (state == 1)
                {
                    adInfo.isLoaded = true;
                    if (adInfo.adViewOnLoaded != null)
                    {
                        adInfo.adViewOnLoaded(adId, true);
                    }
                }
                else
                {
                    if (adInfo.adViewOnFail != null)
                    {
                        string msg = (string)jsonValueData["message"];
                        adInfo.adViewOnFail(adId, msg);
                    }
                }
            }
        }
        if (success && key.Equals("SHOW_ADS"))
        {
            JsonData jsonValueData = jsonData["value"];
            string adId = (string)jsonValueData["adId"];
            Debug.Log("AdManager adId:" + adId);
            int state = (int)jsonValueData["state"];
            Debug.Log("AdManager state:" + state);
            if (adHashMap.ContainsKey(adId))
            {
                AdInfo adInfo = adHashMap[adId];
                Debug.Log("AdManager adInfo.adType:" + adInfo.adType);
                switch (state)
                {
                    case 100:
                        if (adInfo.adType == AdManager.InterstitialAd ||
                            adInfo.adType == AdManager.RewardAd
                            )
                        {
                            adInfo.isLoaded = false;
                        }
                        if (adInfo.adViewOnDidPresent != null)
                        {
                            adInfo.adViewOnDidPresent(adId);
                        }
                        break;
                    case 102:
                        if (adInfo.adViewOnDismissPresent != null)
                        {
                            adInfo.adViewOnDismissPresent(adId);
                        }
                        break;
                    case 103:
                        int data = (int)jsonValueData["data"];
                        if (adInfo.adViewOnReward != null)
                        {
                            adInfo.adViewOnReward(adId, data);
                        }
                        break;
                    default:
                        // code block
                        break;

                }
            }
        }
    }

    public string GetDefaultAdBanner()
    {
        string adId = "";
#if UNITY_ANDROID
        adId = "ca-app-pub-3940256099942544/6300978111";
#endif
#if UNITY_IPHONE
        adId = "ca-app-pub-3940256099942544/6300978111";
#endif
        return adId;
    }

    public string GetDefaultAdInterstitial()
    {
        string adId = "";
#if UNITY_ANDROID
        adId = "ca-app-pub-3940256099942544/8691691433";
#endif
#if UNITY_IPHONE
        adId = "ca-app-pub-3940256099942544/8691691433";
#endif
        return adId;
    }

    public string GetDefaultAdReward()
    {
        string adId = "";
#if UNITY_ANDROID
        adId = "ca-app-pub-3940256099942544/5224354917";
#endif
#if UNITY_IPHONE
        adId = "ca-app-pub-3940256099942544/5224354917";
#endif
        return adId;
    }

    public int GetAdTypeById(string adId)
    {
        return confAdHashMap[adId];
    }
    // Start is called before the first frame update
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

    }
}
