//
//  SKProductsRequest+Tag.m
//  Unity-iPhone
//
//  Created by TranCong on 03/04/2023.
//

#import "SKProductsRequest+Tag.h"
#import <objc/runtime.h>
@implementation SKProductsRequest(Tag)
- (NSString *)tagValue{
    return objc_getAssociatedObject(self, @selector(tagValue));
}

- (void)setTagValue:(NSString *)value{
    objc_setAssociatedObject(self, @selector(tagValue), value, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}
@end
