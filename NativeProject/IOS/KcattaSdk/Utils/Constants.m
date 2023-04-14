//
//  Constants.m
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import "Constants.h"

@implementation Constants

NSString *const JSON_KEY = @"key";
NSString *const JSON_VALUE = @"value";
NSString *const JSON_RECEIVED_OBJECT = @"receivedObject";
NSString *const JSON_RECEIVED_FUNCTION = @"receivedFunc";

NSString *const PRODUCT_TYPE_CONSUMABLE = @"consumable";
NSString *const PRODUCT_TYPE_NON_CONSUMABLE = @"non-consumable" ;
NSString *const PRODUCT_TYPE_SUBS = @"subs";
NSString *const PRODUCT_TYPE_INAPP = @"inapp";

NSString *const PRODUCT_TYPE = @"productType";

NSString *const PAYMENT = @"PAYMENT";
NSString *const RESTORE = @"RESTORE";

NSString *const ADS_TYPE_BANNER = @"bannerAd";
NSString *const ADS_TYPE_INTERSTITIAL = @"interstitialAd";
NSString *const ADS_TYPE_REWARDED = @"rewardedAd";

int QUERY_PRODUCT_NOT_AVAILABLE = -99;
int NO_INTERNET_CONNECTION = -100;
@end
