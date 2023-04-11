//
//  PaymentView.m
//  iTapSdk
//
//  Created by TranCong on 09/03/2022.
//

#import "PaymentView.h"

@interface PaymentView ()
@property (nonatomic) BOOL hasRestorablePurchases;
/// Keeps track of all restored purchases.
@property (strong) NSMutableArray *productsRestored;

@end
@implementation PaymentView

@synthesize productInfo;
@synthesize mode;
/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect {
 // Drawing code
 }
 
 -(void)confirmTransaction:(TransactionInfo*) trans andSKTransaction:(SKPaymentTransaction*) transaction{
 
 }*/
-(void)initTransaction{
    self.hasRestorablePurchases = NO;
    
    if([mode isEqual:PAYMENT]){
        SKMutablePayment *payment = [[SKMutablePayment alloc] init] ;
        payment.productIdentifier = productInfo.productId;
        payment.applicationUsername = @"794b43e0-bcdf-42fa-b378-c259dede3675";
        NSLog(@"Purchase starting...");
        [[SKPaymentQueue defaultQueue] addPayment:payment];
    }
    else if([mode isEqual:RESTORE]){
        if(self.productsRestored == nil){
            self.productsRestored = [[NSMutableArray alloc] init];
        }
        if (self.productsRestored.count > 0) {
            [self.productsRestored removeAllObjects];
        }
        NSLog(@"Restore starting...");
        [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
    }
}
-(void)configUI:(UIView *)parentView{
    [[SKPaymentQueue defaultQueue] addTransactionObserver:self];
    [self showLoading:[TSLanguageManager localizedString:@"Đang khởi tạo giao dịch"]];
    [self initTransaction];
}
- (void)removeFromSuperview{
    [[SKPaymentQueue defaultQueue] removeTransactionObserver:self];
    [super removeFromSuperview];
}
-(void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray<SKPaymentTransaction *> *)transactions{
    NSLog(@"paymentQueue...: %ld",transactions.count);
    [self showLoading:[TSLanguageManager localizedString:@"Đang xác thực giao dịch"]];
    for (SKPaymentTransaction *transaction in transactions) {
        switch (transaction.transactionState) {
                // Call the appropriate custom method for the transaction state.
            case SKPaymentTransactionStatePurchasing:
                
                break;
            case SKPaymentTransactionStateDeferred:
                
                break;
            case SKPaymentTransactionStateFailed:
                [self fail:transaction];
                break;
            case SKPaymentTransactionStatePurchased:
                [self complete:transaction];
                break;
            case SKPaymentTransactionStateRestored:
                [self restore:transaction];
                break;
            default:
                // For debugging
                NSLog(@"Unexpected transaction state %@", @(transaction.transactionState));
                break;
        }
    }
}
- (void)paymentQueue:(SKPaymentQueue *)queue removedTransactions:(NSArray *)transactions {
    NSLog(@"removedTransactions...: %ld",transactions.count);
    for(SKPaymentTransaction *transaction in transactions) {
        
    }
    [self btnClose:NULL];
}

-(void)complete:(SKPaymentTransaction*) transaction{
    NSLog(@"complete...");
    [self showLoading:[TSLanguageManager localizedString:@"Đang xác thực giao dịch"]];
    
    
    NSURL *receiptURL = [[NSBundle mainBundle] appStoreReceiptURL];
    NSData *receipt = [NSData dataWithContentsOfURL:receiptURL];
    TransactionInfo *trans = [[TransactionInfo alloc] init];
    trans.productId = transaction.payment.productIdentifier;
    
    if (!receipt) {
        NSLog(@"no receipt");
        [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
        [self showAlert:NULL withContent:[TSLanguageManager localizedString:@"Không tìm thấy hóa đơn"] withAction:^(UIAlertAction * _Nonnull) {
            //[self btnClose:NULL];
        }];
    } else {
        NSString *encodedReceipt = [receipt base64EncodedStringWithOptions:0];
        NSLog(@"receipt: %@",encodedReceipt);
        
        trans.purchasedToken = encodedReceipt;
        trans.transactionDate = transaction.transactionDate;
        NSLog(@"transaction Date: %@",[Utils displayDate:trans.transactionDate]);
        trans.transactionIdentifier = transaction.transactionIdentifier;
        NSLog(@"transaction Identifier: %@",trans.transactionIdentifier);
        NSLog(@"product id: %@",trans.productId);
        if(self.delegate != NULL){
            [self.delegate didPurchaseSuccess:trans withProduct:productInfo];
        }
        [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
        //[self btnClose:NULL];
    }
    
}

-(void)restore:(SKPaymentTransaction*) transaction{
    if(!self.hasRestorablePurchases){
        self.hasRestorablePurchases = TRUE;
    }
    NSLog(@"restore...%@",transaction.payment.productIdentifier);
    NSString *encodedReceipt = [transaction.transactionReceipt base64EncodedStringWithOptions:0];
    NSLog(@"restore receipt: %@",encodedReceipt);
    TransactionInfo* trans = [[TransactionInfo alloc] init];
    trans.productId = transaction.payment.productIdentifier;
    ProductInfo* productInfo = [[Sdk sharedInstance] findProductInfobyId:trans.productId];
    trans.purchasedToken = encodedReceipt;
    trans.productType = productInfo.productType;
    trans.transactionDate = transaction.transactionDate;
    NSLog(@"restore transactionDate: %@",[Utils displayDate:trans.transactionDate]);
    [self.productsRestored addObject:trans];
    [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
    
}

/// Called when an error occur while restoring purchases. Notifies the user about the error.
- (void)paymentQueue:(SKPaymentQueue *)queue restoreCompletedTransactionsFailedWithError:(NSError *)error {
    NSLog(@"restoreCompletedTransactionsFailedWithError...");
    if (error.code != SKErrorPaymentCancelled) {
        NSString* message = error.localizedDescription;
        [self.delegate didQueryError:error.code withError:message];
    }
    [self btnClose:NULL];
}

/// Called when all restorable transactions have been processed by the payment queue.
- (void)paymentQueueRestoreCompletedTransactionsFinished:(SKPaymentQueue *)queue {
    NSLog(@"paymentQueueRestoreCompletedTransactionsFinished...");
    
    if (!self.hasRestorablePurchases) {
        //no previous purchases
        [self.delegate didQueryError:QUERY_PRODUCT_NOT_AVAILABLE withError:@"Product is not available"];
    }
    else{
        NSMutableArray* transInApp = [[NSMutableArray alloc] init];
        NSMutableArray* transSubs = [[NSMutableArray alloc] init];
        if(self.productsRestored.count > 0 ){
            for (int i=0; i< self.productsRestored.count; i++) {
                TransactionInfo* transItem = [self.productsRestored objectAtIndex:i];
                if([transItem.productType isEqual:PRODUCT_TYPE_INAPP]
                   ||
                   [transItem.productType isEqual:PRODUCT_TYPE_NON_CONSUMABLE]
                   ||
                   [transItem.productType isEqual:PRODUCT_TYPE_CONSUMABLE]
                   ){
                    [transInApp addObject:transItem];
                }
                else{
                    [transSubs addObject:transItem];
                }
            }
        }
        if(transInApp.count > 0){
            if(self.delegate != NULL) {
                [self.delegate didQueryProductInApp:[transInApp copy]];
            }
        }
        if(transSubs.count > 0){
            if(self.delegate != NULL) {
                [self.delegate didQueryProductSubs:[transSubs copy]];
            }
        }
    }
    [self btnClose:NULL];
}

-(void)fail:(SKPaymentTransaction*) transaction{
    NSLog(@"fail...");
    [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
    //[self btnClose:NULL];
}
-(void)btnClose:(id)sender{
    [super btnClose:sender];
    NSLog(@"btnClose");
    [self removeFromSuperview];
}
@end
