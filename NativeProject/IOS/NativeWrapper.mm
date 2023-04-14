//
//  NativeWrapper.m
//  Unity-iPhone
//
//  Created by TranCong on 01/04/2023.
//

#import "NativeWrapper.h"
#import "UnityAppController.h"
#import "Sdk.h"
@implementation NativeWrapper
void sendDataFromUnity (const char* key, const char* value)
{
    NSString *keyStr = [[NSString alloc] initWithUTF8String:key];
    NSString *valueStr = [[NSString alloc] initWithUTF8String:value];
    NSLog(@"Key: %@",keyStr);
    NSLog(@"Value: %@",valueStr);
    if([keyStr isEqual:@"GET_PRODUCT"]){
        NSMutableArray* listProducts = [[NSMutableArray alloc] init];
        ProductInfo* item1 = [[ProductInfo alloc] init];
        item1.productId = @"vn.vplay.sdk.t000i.demoproduct1";
        item1.productType = PRODUCT_TYPE_CONSUMABLE;
        [listProducts addObject:item1];
        ProductInfo* item2 = [[ProductInfo alloc] init];
        item2.productId = @"vn.vplay.sdk.t000i.noads";
        item2.productType = PRODUCT_TYPE_NON_CONSUMABLE;
        [listProducts addObject:item2];
        
        NSMutableArray* list2Products = [[NSMutableArray alloc] init];
        
        ProductInfo* item3 = [[ProductInfo alloc] init];
        item3.productId = @"vn.vplay.sdk.t000a.subs2";
        item3.productType = PRODUCT_TYPE_SUBS;
        [list2Products addObject:item3];
        
        //get list product
        //[[Sdk sharedInstance] requestPriceProduct:[listProducts copy] withType:PRODUCT_TYPE_INAPP];
        //[[Sdk sharedInstance] requestPriceProduct:[list2Products copy] withType:PRODUCT_TYPE_SUBS];
        
        //load ads banner
        //[[Sdk sharedInstance] loadBanner:@"ca-app-pub-3940256099942544/2934735716"];
        
        //load interstital
        //[[Sdk sharedInstance] loadInterstitialAd:@"ca-app-pub-3940256099942544/5135589807"];
        
        //load reward
        [[Sdk sharedInstance] loadRewardedAd:@"ca-app-pub-3940256099942544/1712485313"];
    }
    if([keyStr isEqual:@"PAY_PRODUCT"]){
        //[[Sdk sharedInstance] payProduct:@"vn.vplay.sdk.t000i.noads" withOfferId:nil forProductType:PRODUCT_TYPE_INAPP];
        //[[Sdk sharedInstance] payProduct:@"vn.vplay.sdk.t000a.subs2" withOfferId:nil forProductType:PRODUCT_TYPE_SUBS];
        
        //show banner
        //[[Sdk sharedInstance] showBanner];
        
        //show interstital
        //[[Sdk sharedInstance] showInterstitial];
        
        //show reward
        [[Sdk sharedInstance] showRewarded];
    }
    if([keyStr isEqual:@"UPGRADE_PRODUCT"]){
        //[[Sdk sharedInstance] restoreProducts];
        
        //hide banner
        [[Sdk sharedInstance] hideBanner];
    }
    if([keyStr isEqual:@"DOWNGRADE_PRODUCT"]){
        [[Sdk sharedInstance] payProduct:@"vn.vplay.sdk.t000a.subs2" withOfferId:nil forProductType:PRODUCT_TYPE_SUBS];
    }
    //Send from native to unity object
    //UnitySendMessage("Main Camera","setTextFromNative","[{}]");
}
@end

/*extern "C" {
 void sendDataFromUnity (const char* key, const char* value)
 {
 NSString *keyStr = [[NSString alloc] initWithUTF8String:key];
 NSString *valueStr = [[NSString alloc] initWithUTF8String:value];
 NSLog(@"Key: %@",keyStr);
 NSLog(@"Value: %@",valueStr);
 UnitySendMessage("Main Camera","setTextFromNative","[{}]");
 }
 }*/
