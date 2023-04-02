//
//  SKProduct+LocalizedPrice.h
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>
NS_ASSUME_NONNULL_BEGIN

@interface SKProduct(LocalizedPrice)
- (NSString *)localizedPrice;
- (NSString*)currencyCode;
@end

NS_ASSUME_NONNULL_END
