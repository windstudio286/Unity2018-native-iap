//
//  ProductInfo.h
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ProductInfo : NSObject
//consumable; non-consumable; subs
@property (nonatomic,strong) NSString *productType;
@property (nonatomic,strong) NSString *productId;
@property (nonatomic,strong) NSString *productDescription;
@property (nonatomic,strong) NSDecimalNumber *price;
@property (nonatomic,strong) NSString *localizedPrice;
@property (nonatomic,strong) NSString *currencyCode;
@property (nonatomic, strong) NSLocale *priceLocale;
@end

NS_ASSUME_NONNULL_END
