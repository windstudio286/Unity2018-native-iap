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
NS_ASSUME_NONNULL_BEGIN

@protocol SdkDelegate <NSObject,UIApplicationDelegate>
/*- (void)didPurchaseSuccess:(SKPaymentTransaction *_Nonnull)transaction withProductId:(NSString*) productId;

- (void)didPurchaseFailed:(int) errorCode purchaseError:(NSString*_Nullable)error;*/

- (void)didGetProductsInAppSuccess:(NSArray<ProductInfo*>* _Nullable) results;

- (void)didGetProductsSubsSuccess:(NSArray<ProductInfo*>* _Nullable) results;

- (void)didGetProductsError:(NSString*) productType withError:(NSError*) error;
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

@end

NS_ASSUME_NONNULL_END
