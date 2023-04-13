//
//  PaymentView.m
//  iTapSdk
//
//  Created by TranCong on 09/03/2022.
//

#import "PaymentView.h"

@interface PaymentView ()
@property (nonatomic) BOOL hasRestorablePurchases;
/// Keeps track of all restored purchases including non-consumable and auto-renew subscription
@property (nonatomic,strong) NSMutableArray *productsRestored;
@property (nonatomic,strong) NSMutableDictionary *productsCompleted;
@end
@implementation PaymentView
{
    int lastTransCount;
    TransactionInfo* lastSuccessTransaction;
}
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
    self->lastTransCount = [[[SKPaymentQueue defaultQueue] transactions] count];
    if(self.productsRestored == nil){
        self.productsRestored = [[NSMutableArray alloc] init];
    }
    if (self.productsRestored.count > 0) {
        [self.productsRestored removeAllObjects];
    }
    if(self.productsCompleted == NULL){
        self.productsCompleted = [[NSMutableDictionary alloc] init];
    }
    else{
        [self.productsCompleted removeAllObjects];
    }
    NSLog(@"[Begin Transaction] .count : %ld",[[[SKPaymentQueue defaultQueue] transactions] count]);
    if([mode isEqual:PAYMENT]){
        SKMutablePayment *payment = [[SKMutablePayment alloc] init] ;
        payment.productIdentifier = productInfo.productId;
        payment.applicationUsername = @"794b43e0-bcdf-42fa-b378-c259dede3675";
        NSLog(@"Purchase starting...");
        [[SKPaymentQueue defaultQueue] addPayment:payment];
    }
    else if([mode isEqual:RESTORE]){
        
        NSLog(@"Restore starting...");
        [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];
    }
}
-(void)configUI:(UIView *)parentView{
    NSLog(@"Add Observer...");
    [[SKPaymentQueue defaultQueue] addTransactionObserver:self];
    [self showLoading:[TSLanguageManager localizedString:@"Đang khởi tạo giao dịch"]];
    [self initTransaction];
}
- (void)removeFromSuperview{
    NSLog(@"Remove Observer...");
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
    
    NSLog(@"transactions...: %ld",[[SKPaymentQueue defaultQueue] transactions].count);
    if([[SKPaymentQueue defaultQueue] transactions].count <= self -> lastTransCount){
        [self btnClose:NULL];
    }
}

-(void)complete:(SKPaymentTransaction*) transaction{
    NSLog(@"complete...");
    [self showLoading:[TSLanguageManager localizedString:@"Đang xác thực giao dịch"]];
    
    NSURL *receiptURL = [[NSBundle mainBundle] appStoreReceiptURL];
    NSData *receipt = [NSData dataWithContentsOfURL:receiptURL];
    TransactionInfo *trans = [self.productsCompleted objectForKey:transaction.payment.productIdentifier];
    if(trans == NULL){
        trans = [[TransactionInfo alloc] init];
        [self.productsCompleted setValue:trans forKey:transaction.payment.productIdentifier];
        trans.productId = transaction.payment.productIdentifier;
        
        if (!receipt) {
            NSLog(@"no receipt");
            [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
            [self showAlert:NULL withContent:[TSLanguageManager localizedString:@"Không tìm thấy hóa đơn"] withAction:^(UIAlertAction * _Nonnull) {
                //[self btnClose:NULL];
            }];
        } else {
            NSString *encodedReceipt = [receipt base64EncodedStringWithOptions:0];
            //NSLog(@"receipt: %@",encodedReceipt);
            /*NSString *encodedReceipt2 = [transaction.transactionReceipt base64EncodedStringWithOptions:0];
             NSLog(@"receipt2: %@",encodedReceipt2);*/
            
            trans.purchasedToken = encodedReceipt;
            trans.transactionDate = transaction.transactionDate;
            trans.transactionIdentifier = transaction.transactionIdentifier;
            if(productInfo == NULL){
                productInfo = [[Sdk sharedInstance] findProductInfobyId:trans.productId];
            }
            trans.productType = productInfo.productType;
            NSLog(@"transaction Identifier: %@",transaction.transactionIdentifier);
            NSLog(@"transaction Date: %@",[Utils displayDate:trans.transactionDate]);
            NSLog(@"transaction Original Identifier: %@",transaction.originalTransaction.transactionIdentifier);
            NSLog(@"product id: %@",trans.productId);
            if(self.delegate != NULL){
                [self.delegate didPurchaseSuccess:trans withProduct:productInfo];
            }
        }
    }
    NSLog(@"complete finishTransaction...");
    [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
    //[self btnClose:NULL];
}

-(void)restore:(SKPaymentTransaction*) transaction{
    NSLog(@"restore...");
    if(!self.hasRestorablePurchases){
        self.hasRestorablePurchases = TRUE;
    }
    NSLog(@"productId...%@",transaction.payment.productIdentifier);
    /*NSLog(@"transactionDate: %@",[Utils displayDate:transaction.transactionDate]);
     NSLog(@"transactionID: %@",transaction.transactionIdentifier);
     NSLog(@"originalTransaction.transactionID: %@",transaction.originalTransaction.transactionIdentifier);
     NSLog(@"====");*/
    NSURL *receiptURL = [[NSBundle mainBundle] appStoreReceiptURL];
    NSData *receipt = [NSData dataWithContentsOfURL:receiptURL];
    NSString *encodedReceipt = NULL;
    if(receipt != NULL){
        encodedReceipt = [receipt base64EncodedStringWithOptions:0];
        //NSLog(@"receipt: %@",encodedReceipt);
    }
    TransactionInfo* trans = [self productList:self.productsRestored contain:transaction.payment.productIdentifier];
    if(trans != NULL){
        //update last transaction
        //NSLog(@"transactionDate1: %@",[Utils displayDate:trans.transactionDate]);
        //NSLog(@"transactionIdentifier1: %@",trans.transactionIdentifier);
        
        //NSLog(@"transactionDate2: %@",[Utils displayDate:transaction.transactionDate]);
        //NSLog(@"transactionIdentifier2: %@",transaction.transactionIdentifier);
        if([Utils date1IsGreaterOrEqualThan:transaction.transactionDate date2:trans.transactionDate]){
            trans.transactionDate = transaction.transactionDate;
            trans.transactionIdentifier = transaction.transactionIdentifier;
        }
        //NSLog(@"====");
        //NSLog(@"transactionDate: %@",[Utils displayDate:trans.transactionDate]);
        //NSLog(@"transactionIdentifier: %@",trans.transactionIdentifier);
    }
    else{
        trans = [[TransactionInfo alloc] init];
        trans.productId = transaction.payment.productIdentifier;
        ProductInfo* productInfo = [[Sdk sharedInstance] findProductInfobyId:trans.productId];
        trans.purchasedToken = encodedReceipt;
        trans.transactionIdentifier = transaction.transactionIdentifier;
        trans.productType = productInfo.productType;
        trans.transactionDate = transaction.transactionDate;
        //NSLog(@"transactionDate: %@",[Utils displayDate:trans.transactionDate]);
        [self.productsRestored addObject:trans];
    }
    [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
    
}

-(TransactionInfo*) productList:(NSMutableArray*) data contain:(NSString*) productId{
    TransactionInfo* returnItem = nil;
    for (int i=0; i<[data count]; i++) {
        TransactionInfo* item = [data objectAtIndex:i];
        if([item.productId isEqual:productId]){
            returnItem = item;
            break;
        }
    }
    return returnItem;
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
        [self btnClose:NULL];
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
    //[self btnClose:NULL];
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
