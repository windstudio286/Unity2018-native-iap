//
//  Sdk.h
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>
#import "ProductInfo.h"
#import "AppInfo.h"
#import "Constants.h"
#import "TSLanguageManager.h"
#import "Utils.h"
#import "TransactionInfo.h"
#import <OneSignal/OneSignal.h>
#import <FirebaseCore/FirebaseCore.h>
#import <FirebaseAnalytics/FirebaseAnalytics.h>
NS_ASSUME_NONNULL_BEGIN

@protocol SdkDelegate <NSObject>
- (void)didPurchaseSuccess:(TransactionInfo *_Nonnull)transaction withProduct:(ProductInfo*) productInfo;

- (void)didPurchaseFailed:(int) errorCode purchaseError:(NSString*_Nullable)error;

- (void)didGetProductsInAppSuccess:(NSArray<ProductInfo*>* _Nullable) results;

- (void)didGetProductsSubsSuccess:(NSArray<ProductInfo*>* _Nullable) results;

- (void)didGetProductsError:(NSString*) productType withError:(NSError*) error;

- (void)didQueryProductInApp:(NSArray<TransactionInfo*>*) listTrans;

- (void)didQueryProductSubs:(NSArray<TransactionInfo*>*) listTrans;

- (void)didQueryError:(int) errorCode withError:(NSString*_Nullable)error;

@optional
- (void)onAdLoaded:(NSString*) adType withAdId:(NSString*_Nullable) adId;
- (void)onAdFailedToLoad:(NSString*) adType withAdId:(NSString*_Nullable) adId withError:(NSError*) error;
- (void)onAdOpened:(NSString*) adType withAdId:(NSString*_Nullable) adId;
- (void)onAdClosed:(NSString*) adType withAdId:(NSString*_Nullable) adId;
- (void)onAdEarnedReward:(NSString*) adType withAdId:(NSString*_Nullable) adId withAmount:(int) amount;
- (void)onAllowATT;
- (void)onDisallowATT;
@end

@interface Sdk : NSObject
+(Sdk*_Nonnull) sharedInstance;
// first initialization for sdk
-(void)initWithDelegate:(id<SdkDelegate>_Nullable)delegate
                appInfo:(AppInfo*_Nonnull)appInfo
            application:(UIApplication *_Nonnull)application
          launchOptions:(NSDictionary *_Nonnull)launchOptions;
// Request price of list product with corresponding type
// typeProduct: inapp or subs
-(void)requestPriceProduct:(NSArray<ProductInfo*>* _Nullable) listProducts withType:(NSString*) typeProduct;
-(void)payProduct:(NSString*) productId withBasePlanId:(NSString*) basePlanId withOfferId:(NSString*) offerID forProductType:(NSString*) productType;
-(void)restoreProducts;
-(NSDictionary*) getDictProducts;
-(ProductInfo*) findProductInfobyId:(NSString*) productId;
-(NSString*) getOneSignalUserId;
-(void) postOneSignalNotification:(NSString*) jsonString onSuccess:(OSResultSuccessBlock _Nullable)successBlock onFailure:(OSFailureBlock _Nullable)failureBlock;
- (void)loadBanner:(NSString*) adUnitId;
- (void)loadInterstitialAd:(NSString*) adUnitId;
- (void)loadRewardedAd:(NSString*) adUnitId;
- (void)showBanner;
- (void)hideBanner;
- (void)showRewarded;
- (void)showInterstitial;
-(void) trackingEvent:(NSString*) eventName withParams:(NSDictionary*) params;
-(NSString*) getSubscriptionPrice:(NSString*) productId withBasePlanId:(NSString*) basePlanId withOfferId:(NSString*) offerId;
@end

NS_ASSUME_NONNULL_END
