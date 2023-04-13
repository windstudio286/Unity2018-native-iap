//
//  Sdk.m
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import "Sdk.h"
#import "SKProduct+LocalizedPrice.h"
#import "SKProductsRequest+Tag.h"
#import "PaymentView.h"
#import <OneSignal/OneSignal.h>
@interface Sdk()<SKProductsRequestDelegate>

@property (nonatomic, weak) id<SdkDelegate> delegate;
@property (nonatomic, weak) UIApplication* uiApplicaion;
@property (nonatomic, strong) NSMutableDictionary* dictProducts;
@property (nonatomic, strong) AppInfo* appInfo;


@end

@implementation Sdk
{
    BOOL isProcessingPayment;
    SKProductsRequest *requestProductInApp;
    SKProductsRequest *requestProductSubs;
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
    //setup OneSignal
    // Remove this method to stop OneSignal Debugging
    [OneSignal setLogLevel:ONE_S_LL_VERBOSE visualLevel:ONE_S_LL_NONE];
      
      // OneSignal initialization
    [OneSignal initWithLaunchOptions:launchOptions];
    [OneSignal setAppId:appInfo.oneSignalId];

      // promptForPushNotifications will show the native iOS notification permission prompt.
      // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 8)
    [OneSignal promptForPushNotificationsWithUserResponse:^(BOOL accepted) {
        NSLog(@"User accepted notifications: %d", accepted);
    }];
    
}

-(void)requestPriceProduct:(NSArray<ProductInfo *> *)listProducts withType:(NSString *)typeProduct{
    NSMutableArray *inAppProducts = [self.dictProducts objectForKey:typeProduct];
    if(inAppProducts != NULL && inAppProducts.count > 0){
        if(self.delegate != NULL){
            NSArray *results = [inAppProducts copy];
            if([typeProduct isEqual:PRODUCT_TYPE_INAPP]){
                [self.delegate didGetProductsInAppSuccess:results];
            }
            else{
                [self.delegate didGetProductsSubsSuccess:results];
            }
        }
    }else{
        
        if([typeProduct isEqual:PRODUCT_TYPE_INAPP]){
            if(self->requestProductInApp != NULL){
                [self->requestProductInApp cancel];
                self->requestProductInApp = NULL;
            }
        }
        else{
            if(self->requestProductSubs != NULL){
                [self->requestProductSubs cancel];
                self->requestProductSubs = NULL;
            }
        }
        NSMutableArray * productIdentifiers = [[NSMutableArray alloc] init];
        NSMutableArray* inAppProducts = [[NSMutableArray alloc] init];
        for (int i=0; i<listProducts.count; i++) {
            ProductInfo* item = [listProducts objectAtIndex:i];
            NSString* productId= [[NSString alloc] initWithString:item.productId];
            [productIdentifiers addObject:productId];
            [inAppProducts addObject:item];
        }
        [self.dictProducts setValue:inAppProducts forKey:typeProduct];
        
        SKProductsRequest *productsRequest = [[SKProductsRequest alloc]
                                              initWithProductIdentifiers:[NSSet setWithArray:productIdentifiers]];
        // Keep a strong reference to the request.
        if([typeProduct isEqual:PRODUCT_TYPE_INAPP]){
            self->requestProductInApp = productsRequest;
        }
        else{
            self->requestProductSubs = productsRequest;
        }
        productsRequest.tagValue = typeProduct;
        productsRequest.delegate = self;
        [productsRequest start];
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
        if (@available(iOS 11.2, *)) {
            if(item.introductoryPrice != NULL){
                SKProductDiscount* discountItem = item.introductoryPrice;
                NSLog(@"item discount.price: %f",discountItem.price.floatValue);
                if(discountItem.paymentMode == SKProductDiscountPaymentModeFreeTrial){
                    NSLog(@"item discount.paymentMode: SKProductDiscountPaymentModeFreeTrial");
                }
                if(discountItem.paymentMode == SKProductDiscountPaymentModePayAsYouGo){
                    NSLog(@"item discount.paymentMode: SKProductDiscountPaymentModePayAsYouGo");
                }
                if(discountItem.paymentMode == SKProductDiscountPaymentModePayUpFront){
                    NSLog(@"item discount.paymentMode: SKProductDiscountPaymentModePayUpFront");
                }

                if(discountItem.type == SKProductDiscountTypeIntroductory){
                    NSLog(@"item discount.type: SKProductDiscountTypeIntroductory");
                }
                if(discountItem.type == SKProductDiscountTypeSubscription){
                    NSLog(@"item discount.type: SKProductDiscountTypeSubscription");
                }
                if(discountItem.subscriptionPeriod == NULL){
                    NSLog(@"item discount.subscriptionPeriod: NULL");
                }
                if(discountItem.subscriptionPeriod == SKProductPeriodUnitDay){
                    NSLog(@"item discount.subscriptionPeriod: SKProductPeriodUnitDay");
                }
                if(discountItem.subscriptionPeriod == SKProductPeriodUnitWeek){
                    NSLog(@"item discount.subscriptionPeriod: SKProductPeriodUnitWeek");
                }
                if(discountItem.subscriptionPeriod == SKProductPeriodUnitMonth){
                    NSLog(@"item discount.subscriptionPeriod: SKProductPeriodUnitMonth");
                }
                if(discountItem.subscriptionPeriod == SKProductPeriodUnitYear){
                    NSLog(@"item discount.subscriptionPeriod: SKProductPeriodUnitYear");
                }
                NSLog(@"item discount.numberOfPeriods: %d",discountItem.numberOfPeriods);
                NSLog(@"item discount.identifier: %@",discountItem.identifier);
            }
        } else {
            // Fallback on earlier versions
        }
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
        if([productType isEqual:PRODUCT_TYPE_INAPP]){
            [self.delegate didGetProductsInAppSuccess:results];
        }
        else{
            [self.delegate didGetProductsSubsSuccess:results];
        }
    }
}
-(void)payProduct:(NSString *)productId withOfferId:(NSString *)offerID forProductType:(NSString *)productType{
    NSMutableArray* products = [self.dictProducts objectForKey:productType];
    ProductInfo* findProductItem = nil;
    if(products == NULL){
        [self showDialogConfirm:[TSLanguageManager localizedString:@"Không có sản phẩm"] withAction:nil];
        return;
    }
    if([products count] == 0){
        [self showDialogConfirm:[TSLanguageManager localizedString:@"Không có sản phẩm"] withAction:nil];
        return;
    }
    else{
        if(products != NULL){
            for (int j=0; j< products.count; j++) {
                ProductInfo* productItem = [products objectAtIndex:j];
                if([productItem.productId isEqual:productId]){
                    findProductItem = productItem;
                    break;
                }
            }
        }
        if(findProductItem == nil){
            [self showDialogConfirm:[TSLanguageManager localizedString:@"Không có sản phẩm"] withAction:nil];
            return;
        }
    }
    if(findProductItem != NULL){
        [self showPaymentUI:findProductItem];
    }
}
-(void)showDialogConfirm:(NSString *)msg withAction:(void (^)(UIAlertAction*))action{
    UIAlertController * alert = [UIAlertController
                                 alertControllerWithTitle:[TSLanguageManager localizedString:@"Thông báo"]
                                 message:msg
                                 preferredStyle:UIAlertControllerStyleAlert];
    
    //Add Buttons
    
    UIAlertAction* yesButton = [UIAlertAction
                                actionWithTitle:[TSLanguageManager localizedString:@"Đồng ý"]
                                style:UIAlertActionStyleDefault
                                handler:action];
    [alert addAction:yesButton];
    UIViewController * rootView = [Utils topViewController];
    [rootView presentViewController:alert animated:YES completion:nil];
}
-(void) showPaymentUI:(ProductInfo*)productInfo{
    NSLog(@"showPaymentUI");
    if(!self ->isProcessingPayment){
        self ->isProcessingPayment = TRUE;
        UIViewController *topView = [Utils topViewController];
        
        PaymentView *customView =(PaymentView*) [Utils loadViewFromNibFile:[PaymentView class] universalWithNib:@"PaymentView"];
        customView.productInfo = productInfo;
        customView.mode = PAYMENT;
        customView.delegate = self.delegate;
        customView.callback = ^(NSString* identifier) {
            NSLog(@"Hide showPaymentUI %@",identifier );
            self ->isProcessingPayment = FALSE;
        };
        customView.translatesAutoresizingMaskIntoConstraints = NO;
        topView.view.tag = 200;
        [topView.view addSubview:customView];
        [Utils addConstraintForChild:topView.view andChild:customView withLeft:0 withTop:0 andRight:0 withBottom:0];
    }
}
-(void)restoreProducts{
    NSLog(@"restoreProducts");
    if(!self ->isProcessingPayment){
        self ->isProcessingPayment = TRUE;
        UIViewController *topView = [Utils topViewController];
        
        PaymentView *customView =(PaymentView*) [Utils loadViewFromNibFile:[PaymentView class] universalWithNib:@"PaymentView"];
        customView.mode = RESTORE;
        customView.delegate = self.delegate;
        customView.callback = ^(NSString* identifier) {
            NSLog(@"Hide showPaymentUI %@",identifier );
            self ->isProcessingPayment = FALSE;
        };
        customView.translatesAutoresizingMaskIntoConstraints = NO;
        topView.view.tag = 200;
        [topView.view addSubview:customView];
        [Utils addConstraintForChild:topView.view andChild:customView withLeft:0 withTop:0 andRight:0 withBottom:0];
    }
}
-(NSDictionary *)getDictProducts{
    return self.dictProducts;
}
-(ProductInfo *)findProductInfobyId:(NSString *)productId{
    NSMutableArray* products = [self.dictProducts objectForKey:PRODUCT_TYPE_INAPP];
    ProductInfo* findProductItem = nil;
    if(products != NULL){
        for (int j=0; j< products.count; j++) {
            ProductInfo* productItem = [products objectAtIndex:j];
            if([productItem.productId isEqual:productId]){
                findProductItem = productItem;
                break;
            }
        }
    }
    if(findProductItem == nil){
        products = [self.dictProducts objectForKey:PRODUCT_TYPE_SUBS];
        if(products != NULL){
            for (int j=0; j< products.count; j++) {
                ProductInfo* productItem = [products objectAtIndex:j];
                if([productItem.productId isEqual:productId]){
                    findProductItem = productItem;
                    break;
                }
            }
        }
    }
    return findProductItem;
}
-(NSString *)getOneSignalUserId{
    NSString* userId = @"";
    if([OneSignal getDeviceState] != NULL ){
        userId =[OneSignal getDeviceState].userId;
    }
    return userId;
}
@end
