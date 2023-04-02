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
        NSMutableArray* listProducts = [[NSMutableArray alloc] init];
        ProductInfo* item1 = [[ProductInfo alloc] init];
        item1.productId = @"vn.vplay.sdk.t000i.demoproduct1";
        item1.productType = PRODUCT_TYPE_CONSUMABLE;
        [listProducts addObject:item1];
        [[Sdk sharedInstance] requestPriceProduct:[listProducts copy] withType:PRODUCT_TYPE_INAPP];
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
