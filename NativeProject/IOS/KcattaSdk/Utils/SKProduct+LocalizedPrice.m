//
//  SKProduct+LocalizedPrice.m
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import "SKProduct+LocalizedPrice.h"

@implementation SKProduct(LocalizedPrice)

-(NSString *)localizedPrice{
    
    NSNumberFormatter *formater = [[NSNumberFormatter alloc] init];
    formater.numberStyle = NSNumberFormatterCurrencyStyle;
    formater.locale = self.priceLocale;
    return [formater stringFromNumber:self.price];
}
-(NSString*)currencyCode{
    NSString *code = [self.priceLocale objectForKey:NSLocaleCurrencyCode];
    return code;
}
@end
