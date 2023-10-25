using System.Collections;
using System.Collections.Generic;
using LitJson;
using UnityEngine;

public class IapManager : MonoBehaviour
{
#if UNITY_IPHONE
    [DllImport("__Internal")] public static extern void sendDataFromUnity(string jsonString);
#endif
    private static IapManager _instance;
    public static IapManager Instance
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
    private void SendJsonString(string jsonString)
    {
        Debug.Log("IapManager SendJsonString:" + jsonString);
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
        UnityPlayerNativeActivity.Call("sendDataFromUnity", jsonString);

#endif

#if UNITY_IPHONE

        sendDataFromUnity(jsonString);
#endif

    }
    void setTextFromNative(string newValue)
    {
        Debug.Log("IapManager setTextFromNative:" + newValue);
        JsonData jsonData = JsonMapper.ToObject(newValue);
        string key = (string)jsonData["key"];
        bool success = (bool)jsonData["success"];
    }
}
