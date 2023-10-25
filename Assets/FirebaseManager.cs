using System.Collections;
using System.Collections.Generic;
using LitJson;
using System.Text;
using UnityEngine;

public class FirebaseManager : MonoBehaviour
{
    //https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event#LEVEL_START
    #region PARAMS
    public const string PARAM_ACHIEVEMENT_ID = "achievement_id";

    public const string PARAM_AD_FORMAT = "ad_format";

    public const string PARAM_AD_PLATFORM = "ad_platform";

    public const string PARAM_AD_SOURCE = "ad_source";

    public const string PARAM_AD_UNIT_NAME = "ad_unit_name";

    public const string PARAM_CHARACTER = "character";

    public const string PARAM_TRAVEL_CLASS = "travel_class";

    public const string PARAM_CONTENT_TYPE = "content_type";

    public const string PARAM_CURRENCY = "currency";

    public const string PARAM_COUPON = "coupon";

    public const string PARAM_START_DATE = "start_date";

    public const string PARAM_END_DATE = "end_date";

    public const string PARAM_EXTEND_SESSION = "extend_session";

    public const string PARAM_FLIGHT_NUMBER = "flight_number";

    public const string PARAM_GROUP_ID = "group_id";

    public const string PARAM_ITEM_CATEGORY = "item_category";

    public const string PARAM_ITEM_ID = "item_id";

    public const string PARAM_ITEM_NAME = "item_name";

    public const string PARAM_LOCATION = "location";

    public const string PARAM_LEVEL = "level";

    public const string PARAM_LEVEL_NAME = "level_name";

    public const string PARAM_METHOD = "method";

    public const string PARAM_NUMBER_OF_NIGHTS = "number_of_nights";

    public const string PARAM_NUMBER_OF_PASSENGERS = "number_of_passengers";

    public const string PARAM_NUMBER_OF_ROOMS = "number_of_rooms";

    public const string PARAM_DESTINATION = "destination";

    public const string PARAM_ORIGIN = "origin";

    public const string PARAM_PRICE = "price";

    public const string PARAM_QUANTITY = "quantity";

    public const string PARAM_SCORE = "score";

    public const string PARAM_SHIPPING = "shipping";

    public const string PARAM_TRANSACTION_ID = "transaction_id";

    public const string PARAM_SEARCH_TERM = "search_term";

    public const string PARAM_SUCCESS = "success";

    public const string PARAM_TAX = "tax";

    public const string PARAM_VALUE = "value";

    public const string PARAM_VIRTUAL_CURRENCY_NAME = "virtual_currency_name";

    public const string PARAM_CAMPAIGN = "campaign";

    public const string PARAM_SOURCE = "source";

    public const string PARAM_MEDIUM = "medium";

    public const string PARAM_TERM = "term";

    public const string PARAM_CONTENT = "content";

    public const string PARAM_ACLID = "aclid";

    public const string PARAM_CP1 = "cp1";

    public const string PARAM_CAMPAIGN_ID = "campaign_id";

    public const string PARAM_SOURCE_PLATFORM = "source_platform";

    public const string PARAM_CREATIVE_FORMAT = "creative_format";

    public const string PARAM_MARKETING_TACTIC = "marketing_tactic";

    public const string PARAM_ITEM_BRAND = "item_brand";

    public const string PARAM_ITEM_VARIANT = "item_variant";

    public const string PARAM_CREATIVE_NAME = "creative_name";

    public const string PARAM_CREATIVE_SLOT = "creative_slot";

    public const string PARAM_AFFILIATION = "affiliation";

    public const string PARAM_INDEX = "index";

    public const string PARAM_DISCOUNT = "discount";

    public const string PARAM_ITEM_CATEGORY2 = "item_category2";

    public const string PARAM_ITEM_CATEGORY3 = "item_category3";

    public const string PARAM_ITEM_CATEGORY4 = "item_category4";

    public const string PARAM_ITEM_CATEGORY5 = "item_category5";

    public const string PARAM_ITEM_LIST_ID = "item_list_id";

    public const string PARAM_ITEM_LIST_NAME = "item_list_name";

    public const string PARAM_ITEMS = "items";

    public const string PARAM_LOCATION_ID = "location_id";

    public const string PARAM_PAYMENT_TYPE = "payment_type";

    public const string PARAM_PROMOTION_ID = "promotion_id";

    public const string PARAM_PROMOTION_NAME = "promotion_name";

    public const string PARAM_SCREEN_CLASS = "screen_class";

    public const string PARAM_SCREEN_NAME = "screen_name";

    public const string PARAM_SHIPPING_TIER = "shipping_tier";
    #endregion
    #region EVENTS
    public const string EVENT_AD_IMPRESSION = "ad_impression";

    public const string EVENT_ADD_PAYMENT_INFO = "add_payment_info";

    public const string EVENT_ADD_TO_CART = "add_to_cart";

    public const string EVENT_ADD_TO_WISHLIST = "add_to_wishlist";

    public const string EVENT_APP_OPEN = "app_open";

    public const string EVENT_BEGIN_CHECKOUT = "begin_checkout";

    public const string EVENT_CAMPAIGN_DETAILS = "campaign_details";

    public const string EVENT_GENERATE_LEAD = "generate_lead";

    public const string EVENT_JOIN_GROUP = "join_group";

    public const string EVENT_LEVEL_END = "level_end";

    public const string EVENT_LEVEL_START = "level_start";

    public const string EVENT_LEVEL_UP = "level_up";

    public const string EVENT_LOGIN = "login";

    public const string EVENT_POST_SCORE = "post_score";

    public const string EVENT_SEARCH = "search";

    public const string EVENT_SELECT_CONTENT = "select_content";

    public const string EVENT_SHARE = "share";

    public const string EVENT_SIGN_UP = "sign_up";

    public const string EVENT_SPEND_VIRTUAL_CURRENCY = "spend_virtual_currency";

    public const string EVENT_TUTORIAL_BEGIN = "tutorial_begin";

    public const string EVENT_TUTORIAL_COMPLETE = "tutorial_complete";

    public const string EVENT_UNLOCK_ACHIEVEMENT = "unlock_achievement";

    public const string EVENT_VIEW_ITEM = "view_item";

    public const string EVENT_VIEW_ITEM_LIST = "view_item_list";

    public const string EVENT_VIEW_SEARCH_RESULTS = "view_search_results";

    public const string EVENT_EARN_VIRTUAL_CURRENCY = "earn_virtual_currency";

    public const string EVENT_SCREEN_VIEW = "screen_view";

    public const string EVENT_REMOVE_FROM_CART = "remove_from_cart";

    public const string EVENT_ADD_SHIPPING_INFO = "add_shipping_info";

    public const string EVENT_PURCHASE = "purchase";

    public const string EVENT_REFUND = "refund";

    public const string EVENT_SELECT_ITEM = "select_item";

    public const string EVENT_SELECT_PROMOTION = "select_promotion";

    public const string EVENT_VIEW_CART = "view_cart";

    public const string EVENT_VIEW_PROMOTION = "view_promotion";
    #endregion

#if UNITY_IPHONE
    [DllImport("__Internal")] public static extern void sendDataFromUnity(string jsonString);
#endif
    public System.Action<bool> remoteConfigFetchAndActiveAction;
    public bool isActived;
    private static FirebaseManager _instance;
    public static FirebaseManager Instance
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
        Debug.Log("FirebaseManager SendJsonString:" + jsonString);
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
        Debug.Log("FirebaseManager setTextFromNative:" + newValue);
        JsonData jsonData = JsonMapper.ToObject(newValue);
        string key = (string)jsonData[Constants.key];
        bool success = (bool)jsonData[Constants.success];
        if (success && key.Equals(Constants.ACTION_FETCH_ACTIVE))
        {
            if(remoteConfigFetchAndActiveAction != null)
            {
                isActived = success;
                remoteConfigFetchAndActiveAction(success);
            }
        }
    }

    public void LogEvent(string eventName,LitJson.JsonData parametersJson)
    {
        JsonData jsonDataRoot = new JsonData();
        jsonDataRoot[Constants.receivedObject] = this.gameObject.name;
        jsonDataRoot[Constants.receivedFunc] = Constants.setTextFromNative;
        jsonDataRoot[Constants.key] = Constants.ACTION_LOG_EVENT;

        JsonData jsonDataValue = new JsonData();
        jsonDataValue["eventName"] = eventName;
        jsonDataValue["parameters"] = parametersJson;
        jsonDataRoot[Constants.value] = jsonDataValue;

        SendJsonString(jsonDataRoot.ToJson());
    }

    public void RemoteConfigFetchAndActive()
    {
        JsonData jsonDataRoot = new JsonData();
        jsonDataRoot[Constants.receivedObject] = this.gameObject.name;
        jsonDataRoot[Constants.receivedFunc] = Constants.setTextFromNative;
        jsonDataRoot[Constants.key] = Constants.ACTION_FETCH_ACTIVE;
        SendJsonString(jsonDataRoot.ToJson());
    }

    public string RemoteConfigGetString(string keyRemote)
    {
        string retValue = "";
#if UNITY_ANDROID

        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject UnityPlayerNativeActivity = jc.GetStatic<AndroidJavaObject>("currentActivity");
        retValue =  UnityPlayerNativeActivity.Call<string>("remoteConfigGetString", keyRemote);

#endif
#if UNITY_IOS
        retValue =  "";
#endif
        return retValue;
    }
}
