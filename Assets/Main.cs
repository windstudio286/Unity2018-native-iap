using System.Collections;
using System.Collections.Generic;
using System.Text;
using LitJson;
using UnityEngine;
using System.Runtime.InteropServices;
public class Main : MonoBehaviour
{
#if UNITY_IPHONE
    [DllImport("__Internal")] public static extern void sendDataFromUnity(string key,string value);
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

    public void onBtnDowngradeClick()
    {
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

    public void onBtnPayClick()
    {
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
        if(text != null)
        {
            text.text = newValue;
        }
    }
}
