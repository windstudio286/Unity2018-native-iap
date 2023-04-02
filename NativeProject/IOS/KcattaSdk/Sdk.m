//
//  Sdk.m
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import "Sdk.h"
#import "SKProduct+LocalizedPrice.h"
#import "SKProductsRequest+Tag.h"
@interface Sdk()<SKProductsRequestDelegate>

@property (nonatomic, weak) id<SdkDelegate> delegate;
@property (nonatomic, weak) UIApplication* uiApplicaion;
@property (nonatomic, strong) NSMutableDictionary* dictProducts;
@property (nonatomic, strong) AppInfo* appInfo;


@end

@implementation Sdk
{
    BOOL isProcessingPayment;
    SKProductsRequest *requestProduct;
}
+ (Sdk *)sharedInstance{
    static Sdk *_sharedInstance = nil;
    static dispatch_once_t oncePredicate;
    dispatch_once(&oncePredicate, ^{
        _sharedInstance = [[Sdk alloc] init];
    });
    return _sharedInstance;
}
- (instancetype)init {
    if (self = [super init]) {
        self.dictProducts = [[NSMutableDictionary alloc] init];
    }
    return self;
}
-(void)dealloc{
    
}
-(void)initWithDelegate:(id<SdkDelegate>)delegate appInfo:(AppInfo *)appInfo application:(UIApplication *)application launchOptions:(NSDictionary *)launchOptions{
    
    self.delegate = delegate;
    self.appInfo = appInfo;
    self.uiApplicaion = application;
}

-(void)requestPriceProduct:(NSArray<ProductInfo *> *)listProducts withType:(NSString *)typeProduct{
    if([typeProduct isEqual:PRODUCT_TYPE_INAPP]){
        NSMutableArray *inAppProducts = [self.dictProducts objectForKey:PRODUCT_TYPE_INAPP];
        if(inAppProducts != NULL && inAppProducts.count > 0){
            if(self.delegate != NULL){
                NSArray *results = [inAppProducts copy];
                [self.delegate didGetProductsInAppSuccess:results];
            }
        }else{
            
            if(self->requestProduct != NULL){
                [self->requestProduct cancel];
                self->requestProduct = NULL;
            }
            
            NSMutableArray * productIdentifiers = [[NSMutableArray alloc] init];
            NSMutableArray* inAppProducts = [[NSMutableArray alloc] init];
            for (int i=0; i<listProducts.count; i++) {
                ProductInfo* item = [listProducts objectAtIndex:i];
                NSString* productId= [[NSString alloc] initWithString:item.productId];
                [productIdentifiers addObject:productId];
                [inAppProducts addObject:item];
            }
            [self.dictProducts setValue:inAppProducts forKey:PRODUCT_TYPE_INAPP];
            
            SKProductsRequest *productsRequest = [[SKProductsRequest alloc]
                                                  initWithProductIdentifiers:[NSSet setWithArray:productIdentifiers]];
            // Keep a strong reference to the request.
            self->requestProduct = productsRequest;
            productsRequest.tagValue = PRODUCT_TYPE_INAPP;
            productsRequest.delegate = self;
            [productsRequest start];
        }
    }
    else{
        
    }
}

-(void)request:(SKRequest *)request didFailWithError:(NSError *)error{
    NSLog(@"productsRequest fail");
    
}
-(void)requestDidFinish:(SKRequest *)request{
    NSLog(@"productsRequest finish");
}
//SKProductsRequestDelegate
-(void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response{
    NSLog(@"productsRequest success");
    NSArray<SKProduct *> *products = response.products;
    for (NSString *invalidIdentifier in response.invalidProductIdentifiers) {
        // Handle any invalid product identifiers.
        NSLog(@"invalidIdentifier: %@",invalidIdentifier);
    }
    NSString* productType = request.tagValue;
    NSMutableArray* inAppProducts = [self.dictProducts objectForKey:productType];
    for (int i=0; i < products.count; i++) {
        SKProduct *item = [products objectAtIndex:i];
        NSLog(@"item.price: %f",item.price.floatValue);
        NSLog(@"item.localizedTitle: %@",item.localizedTitle);
        NSLog(@"item.localizedPrice: %@",[item localizedPrice]);
        NSLog(@"item.currencyCode: %@",[item currencyCode]);
        NSLog(@"item.productIdentifier: %@",item.productIdentifier);
        NSLog(@"item.localizedDescription: %@",item.localizedDescription);
        for (int j=0; j< inAppProducts.count; j++) {
            ProductInfo* prodInfo = [inAppProducts objectAtIndex:j];
            if([item.productIdentifier isEqual:[prodInfo productId]]){
                prodInfo.price = item.price;
                prodInfo.localizedPrice = [item localizedPrice];
                prodInfo.priceLocale = item.priceLocale;
                prodInfo.currencyCode = [item currencyCode];
            }
        }
    }
    if(self.delegate != NULL){
        NSArray * results = [inAppProducts copy];
        [self.delegate didGetProductsInAppSuccess:results];
    }
}
@end
