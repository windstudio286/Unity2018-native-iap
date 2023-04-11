//
//  VnptTransaction.h
//  VnptSdk
//
//  Created by Tran Trong Cong on 8/5/21.
//

#import <Foundation/Foundation.h>

@interface TransactionInfo : NSObject
@property (nonatomic,strong) NSString *purchasedToken;
@property (nonatomic,strong) NSString *productId;
@property (nonatomic,strong) NSString *productType;
@property (nonatomic,strong) NSDate *transactionDate;
@property (nonatomic,strong) NSString* transactionIdentifier;
@end
