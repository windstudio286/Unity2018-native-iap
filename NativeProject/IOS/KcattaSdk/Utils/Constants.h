//
//  Constants.h
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Constants : NSObject

extern NSString * const KEY_DEVICE_ID;
extern NSString *const JSON_KEY;
extern NSString *const JSON_VALUE;
extern NSString *const JSON_RECEIVED_OBJECT;
extern NSString *const JSON_RECEIVED_FUNCTION;

extern NSString *const PRODUCT_TYPE_CONSUMABLE;
extern NSString *const PRODUCT_TYPE_NON_CONSUMABLE;
extern NSString *const PRODUCT_TYPE_SUBS;
extern NSString *const PRODUCT_TYPE_INAPP;

extern NSString *const PRODUCT_TYPE;

extern NSString *const PAYMENT;
extern NSString *const RESTORE;

extern int QUERY_PRODUCT_NOT_AVAILABLE;
extern int NO_INTERNET_CONNECTION;

extern NSString *const ADS_TYPE_BANNER;
extern NSString *const ADS_TYPE_INTERSTITIAL;
extern NSString *const ADS_TYPE_REWARDED;
@end

NS_ASSUME_NONNULL_END
