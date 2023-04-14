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
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <GoogleMobileAds/GoogleMobileAds.h>

@interface Sdk()<SKProductsRequestDelegate,GADBannerViewDelegate,GADFullScreenContentDelegate>

@property (nonatomic, weak) id<SdkDelegate> delegate;
@property (nonatomic, weak) UIApplication* uiApplicaion;
@property (nonatomic, strong) NSMutableDictionary* dictProducts;
@property (nonatomic, strong) AppInfo* appInfo;
//declare ads
@property (nonatomic, strong) GADBannerView *bannerView;
@property (nonatomic, strong) GADRewardedAd *rewardedAd;
@property (nonatomic, strong) GADInterstitialAd *interstitialAd;

@end

@implementation Sdk
{
    BOOL isInited;
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
        isInited = FALSE;
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
    
    if(appInfo != nil){
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(sendLaunch:)
                                                     name:UIApplicationDidBecomeActiveNotification
                                                   object:nil];
    }
    [self initAds];
}
-(void) initAds{
    [GADMobileAds.sharedInstance startWithCompletionHandler:^(GADInitializationStatus * _Nonnull status) {
        // Optional: Log each adapter's initialization latency.
        NSDictionary *adapterStatuses = [status adapterStatusesByClassName];
        for (NSString *adapter in adapterStatuses) {
            GADAdapterStatus *adapterStatus = adapterStatuses[adapter];
            NSLog(@"Adapter Name: %@, State: %ld, Description: %@, Latency: %f", adapter,adapterStatus.state,
                  adapterStatus.description, adapterStatus.latency);
        }
    }];
    if(self.appInfo.testDeviceIdentifiers != NULL){
        GADMobileAds.sharedInstance.requestConfiguration.testDeviceIdentifiers = self.appInfo.testDeviceIdentifiers;
    }
    if(self.appInfo != NULL){
        [GADMobileAds.sharedInstance.requestConfiguration tagForChildDirectedTreatment:self.appInfo.tagForChildDirectedTreatment];
    }
}
- (void)sendLaunch:(NSString *)applicationState {
    if(!isInited){
        [self askAllowATT];
        //show ask notification
        //[self askAllowNotification:[UIApplication sharedApplication]];
        
        // promptForPushNotifications will show the native iOS notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 8)
        [OneSignal promptForPushNotificationsWithUserResponse:^(BOOL accepted) {
            NSLog(@"User accepted notifications: %d", accepted);
        }];
        isInited = true;
    }
    NSLog(@"Application active");
}
-(void) askAllowATT{
    if (@available(iOS 14, *)) {
        [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
            // Tracking authorization completed. Start loading ads here.
            if(status == ATTrackingManagerAuthorizationStatusAuthorized){
                //[FBSDKSettings setAdvertiserTrackingEnabled:YES];
                //[FBSDKSettings setAdvertiserIDCollectionEnabled:YES];
            }
            else{
                //[FBSDKSettings setAdvertiserTrackingEnabled:NO];
            }
        }];
    }
}

-(BOOL)application:(UIApplication *)application openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options{
    /*if(options == NULL){
     BOOL handledFB = [[FBSDKApplicationDelegate sharedInstance] application:application
     openURL:url
     sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]
     annotation:options[UIApplicationOpenURLOptionsAnnotationKey]
     ];
     BOOL handledGG = [GIDSignIn.sharedInstance handleURL:url];
     return handledFB || handledGG;
     }
     else{
     BOOL handledFB = [[FBSDKApplicationDelegate sharedInstance] application:application
     openURL:url
     sourceApplication:nil
     annotation:nil
     ];
     BOOL handledGG = [GIDSignIn.sharedInstance handleURL:url];
     return handledFB || handledGG;
     }
     */
    return TRUE;
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
-(void)postOneSignalNotification:(NSString *)jsonString onSuccess:(OSResultSuccessBlock)successBlock onFailure:(OSFailureBlock)failureBlock{
    [OneSignal postNotificationWithJsonString:jsonString onSuccess:successBlock onFailure:failureBlock];
}
-(void)loadBanner:(NSString *)adUnitId{
    UIViewController *rootViewController = [Utils topViewController];
    UIView* view = rootViewController.view;
    CGRect frame = view.frame;
    // Here safe area is taken into account, hence the view frame is used after the
    // view has been laid out.
    if (@available(iOS 11.0, *)) {
        frame = UIEdgeInsetsInsetRect(view.frame, view.safeAreaInsets);
    }
    CGFloat viewWidth = frame.size.width;
    NSLog(@"viewWidth:%f",viewWidth);
    NSLog(@"viewWidth:%f",view.frame.size.width);
    //use adaptive banner
    GADAdSize adSize = GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(viewWidth);
    NSLog(@"adSize:%f,%f",adSize.size.height,adSize.size.width);
    while(adSize.size.height > 65){
        int newAdWidth = 65 * adSize.size.width / adSize.size.height;
        adSize = GADCurrentOrientationAnchoredAdaptiveBannerAdSizeWithWidth(newAdWidth);
    }
    NSLog(@"after adSize:%f,%f",adSize.size.height,adSize.size.width);
    self.bannerView = [[GADBannerView alloc]
                       initWithAdSize:adSize];
    
    
    /*self.bannerView = [[GADBannerView alloc]
     initWithAdSize:kGADAdSizeSmartBannerLandscape];*/
    
    
    self.bannerView.delegate = self;
    
    self.bannerView.adUnitID = adUnitId;
    self.bannerView.rootViewController = rootViewController;
    //top or bottom
    [self addBannerViewToView:self.bannerView onPosition:@"bottom"];
    //[self addSmartBannerViewToView:self.bannerView];
    [self.bannerView loadRequest:[GADRequest request]];
}
- (void)addBannerViewToView:(UIView *)bannerView onPosition:(NSString*) position{
    UIViewController *rootViewController = [Utils topViewController];
    UIView* view = rootViewController.view;
    bannerView.translatesAutoresizingMaskIntoConstraints = NO;
    [view addSubview:bannerView];
    if([position isEqual:@"bottom"]){
        [view addConstraints:@[
            [NSLayoutConstraint constraintWithItem:bannerView
                                         attribute:NSLayoutAttributeBottom
                                         relatedBy:NSLayoutRelationEqual
                                            toItem:view //self.bottomLayoutGuide
                                         attribute:NSLayoutAttributeBottom //NSLayoutAttributeTop
                                        multiplier:1
                                          constant:0],
            [NSLayoutConstraint constraintWithItem:bannerView
                                         attribute:NSLayoutAttributeCenterX
                                         relatedBy:NSLayoutRelationEqual
                                            toItem:view
                                         attribute:NSLayoutAttributeCenterX
                                        multiplier:1
                                          constant:0]
        ]];
    }
    else{
        [view addConstraints:@[
            [NSLayoutConstraint constraintWithItem:bannerView
                                         attribute:NSLayoutAttributeTop
                                         relatedBy:NSLayoutRelationEqual
                                            toItem:view //self.bottomLayoutGuide
                                         attribute:NSLayoutAttributeTop //NSLayoutAttributeTop
                                        multiplier:1
                                          constant:0],
            [NSLayoutConstraint constraintWithItem:bannerView
                                         attribute:NSLayoutAttributeCenterX
                                         relatedBy:NSLayoutRelationEqual
                                            toItem:view
                                         attribute:NSLayoutAttributeCenterX
                                        multiplier:1
                                          constant:0]
        ]];
    }
}

-(void)showBanner{
    if(self.bannerView){
        self.bannerView.hidden = NO;
    }
}
-(void)hideBanner{
    if(self.bannerView){
        self.bannerView.hidden = YES;
    }
}
-(void)loadInterstitialAd:(NSString *)adUnitId{
    GADRequest *request = [GADRequest request];
    [GADInterstitialAd loadWithAdUnitID:adUnitId
                                request:request
                      completionHandler:^(GADInterstitialAd *ad, NSError *error) {
        if (error) {
            NSLog(@"Failed to load interstitial ad with error: %@", [error localizedDescription]);
            NSInteger errorCode = error.code;
            if(self.delegate != NULL){
                if ([self.delegate respondsToSelector: @selector(onAdFailedToLoad:withAdId:withError:)]){
                    [self.delegate onAdFailedToLoad:ADS_TYPE_INTERSTITIAL withAdId:adUnitId withError:error];
                }
            }
            return;
        }
        self.interstitialAd = ad;
        self.interstitialAd.fullScreenContentDelegate = self;
        if(self.delegate != NULL){
            if ([self.delegate respondsToSelector: @selector(onAdLoaded:withAdId:)]){
                [self.delegate onAdLoaded:ADS_TYPE_INTERSTITIAL withAdId:adUnitId];
            }
        }
        NSLog(@"Interstital ad loaded.");
    }];
}
- (void)loadRewardedAd:(NSString *)adUnitId{
    GADRequest *request = [GADRequest request];
    [GADRewardedAd
     loadWithAdUnitID:adUnitId
     request:request
     completionHandler:^(GADRewardedAd *ad, NSError *error) {
        if (error) {
            NSLog(@"Rewarded ad failed to load with error: %@", [error localizedDescription]);
            NSInteger errorCode = error.code;
            if(self.delegate != NULL){
                if ([self.delegate respondsToSelector: @selector(onAdFailedToLoad:withAdId:withError:)]){
                    [self.delegate onAdFailedToLoad:ADS_TYPE_REWARDED withAdId:adUnitId withError:error];
                }
            }
            return;
        }
        self.rewardedAd = ad;
        self.rewardedAd.fullScreenContentDelegate = self;
        if(self.delegate != NULL){
            if ([self.delegate respondsToSelector: @selector(onAdLoaded:withAdId:)]){
                [self.delegate onAdLoaded:ADS_TYPE_REWARDED withAdId:adUnitId];
            }
        }
        NSLog(@"Rewarded ad loaded.");
    }];
}
-(void) showInterstitial{
    if (self.interstitialAd) {
        UIViewController *rootViewController = [Utils topViewController];
        [self.interstitialAd presentFromRootViewController:rootViewController];
      } else {
        NSLog(@"Ad wasn't ready");
    }
}
-(void)showRewarded{
    if (self.rewardedAd) {
        UIViewController *rootViewController = [Utils topViewController];
        [self.rewardedAd presentFromRootViewController:rootViewController
                              userDidEarnRewardHandler:^{
            GADAdReward *reward = self.rewardedAd.adReward;
            if(self.delegate != NULL){
                if ([self.delegate respondsToSelector: @selector(onAdLoaded:withAdId:)]){
                    [self.delegate onAdEarnedReward:ADS_TYPE_REWARDED withAdId:self.rewardedAd.adUnitID withAmount:[reward.amount intValue]];
                }
            }
        }];
    } else {
        NSLog(@"Ad wasn't ready");
    }
}
///
/// implement GADBannerViewDelegate
///
- (void)bannerViewDidReceiveAd:(GADBannerView *)bannerView {
    NSLog(@"bannerViewDidReceiveAd");
    bannerView.hidden = YES;
    if(self.delegate != NULL){
        if ([self.delegate respondsToSelector: @selector(onAdLoaded:withAdId:)]){
            [self.delegate onAdLoaded:ADS_TYPE_BANNER withAdId:bannerView.adUnitID];
        }
    }
}

- (void)bannerView:(GADBannerView *)bannerView didFailToReceiveAdWithError:(NSError *)error {
    NSLog(@"bannerView:didFailToReceiveAdWithError: %@", [error localizedDescription]);
    NSInteger errorCode = error.code;
    GADResponseInfo *responseInfo = error.userInfo[GADErrorUserInfoKeyResponseInfo];
    NSLog(@"\n%@", responseInfo);
    if(self.delegate != NULL){
        if ([self.delegate respondsToSelector: @selector(onAdFailedToLoad:withAdId:withError:)]){
            [self.delegate onAdFailedToLoad:ADS_TYPE_BANNER withAdId:bannerView.adUnitID withError:error];
        }
    }
}

- (void)bannerViewDidRecordImpression:(GADBannerView *)bannerView {
    NSLog(@"bannerViewDidRecordImpression");
}

- (void)bannerViewWillPresentScreen:(GADBannerView *)bannerView {
    NSLog(@"bannerViewWillPresentScreen");
    if(self.delegate != NULL){
        if ([self.delegate respondsToSelector: @selector(onAdOpened:withAdId:)]){
            [self.delegate onAdOpened:ADS_TYPE_BANNER withAdId:bannerView.adUnitID];
        }
    }
}

- (void)bannerViewWillDismissScreen:(GADBannerView *)bannerView {
    NSLog(@"bannerViewWillDismissScreen");
}

- (void)bannerViewDidDismissScreen:(GADBannerView *)bannerView {
    NSLog(@"bannerViewDidDismissScreen");
    if(self.delegate != NULL){
        if ([self.delegate respondsToSelector: @selector(onAdClosed:withAdId:)]){
            [self.delegate onAdClosed:ADS_TYPE_BANNER withAdId:bannerView.adUnitID];
        }
    }
}
///
/// implement GADFullScreenContentDelegate
///
/// Tells the delegate that the ad failed to present full screen content.
- (void)ad:(nonnull id<GADFullScreenPresentingAd>)ad
didFailToPresentFullScreenContentWithError:(nonnull NSError *)error {
    NSLog(@"Ad did fail to present full screen content.");
}

/// Tells the delegate that the ad presented full screen content.
- (void)adDidPresentFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad {
    NSLog(@"Ad did present full screen content.");
    if([ad isKindOfClass:[GADRewardedAd class]]){
        GADRewardedAd *rewardedAd = (GADRewardedAd*)ad;
        if(self.delegate != NULL){
            if ([self.delegate respondsToSelector: @selector(onAdOpened:withAdId:)]){
                [self.delegate onAdOpened:ADS_TYPE_REWARDED withAdId:rewardedAd.adUnitID];
            }
        }
    }
    else if([ad isKindOfClass:[GADInterstitialAd class]]){
        GADInterstitialAd *interstitialAd = (GADInterstitialAd*)ad;
        if(self.delegate != NULL){
            if ([self.delegate respondsToSelector: @selector(onAdOpened:withAdId:)]){
                [self.delegate onAdOpened:ADS_TYPE_INTERSTITIAL withAdId:interstitialAd.adUnitID];
            }
        }
    }
    
}

/// Tells the delegate that the ad dismissed full screen content.
- (void)adDidDismissFullScreenContent:(nonnull id<GADFullScreenPresentingAd>)ad {
    NSLog(@"Ad did dismiss full screen content.");
    if([ad isKindOfClass:[GADRewardedAd class]]){
        GADRewardedAd *rewardedAd = (GADRewardedAd*)ad;
        if(self.delegate != NULL){
            if ([self.delegate respondsToSelector: @selector(onAdClosed:withAdId:)]){
                [self.delegate onAdClosed:ADS_TYPE_REWARDED withAdId:rewardedAd.adUnitID];
            }
        }
        self.rewardedAd = nil;
    }
    else if([ad isKindOfClass:[GADInterstitialAd class]]){
        GADInterstitialAd *interstitialAd = (GADInterstitialAd*)ad;
        if(self.delegate != NULL){
            if ([self.delegate respondsToSelector: @selector(onAdClosed:withAdId:)]){
                [self.delegate onAdClosed:ADS_TYPE_INTERSTITIAL withAdId:interstitialAd.adUnitID];
            }
        }
        self.interstitialAd = nil;
    }
    
}

@end
