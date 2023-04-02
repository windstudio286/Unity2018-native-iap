//
//  SKProductsRequest+Tag.h
//  Unity-iPhone
//
//  Created by TranCong on 03/04/2023.
//

#import <Foundation/Foundation.h>
#import <StoreKit/StoreKit.h>
NS_ASSUME_NONNULL_BEGIN

@interface SKProductsRequest(Tag)
@property (nonatomic,strong) NSString* tagValue;
@end

NS_ASSUME_NONNULL_END
