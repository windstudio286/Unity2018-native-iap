//
//  PaymentView.h
//  iTapSdk
//
//  Created by TranCong on 09/03/2022.
//

#import "BaseUIView.h"
#import "TransactionInfo.h"
#import <StoreKit/StoreKit.h>
NS_ASSUME_NONNULL_BEGIN

@interface PaymentView : BaseUIView<SKPaymentTransactionObserver>

@property (nonatomic, strong) ProductInfo* productInfo;
@property (nonatomic, strong) NSString* mode;
@end

NS_ASSUME_NONNULL_END
