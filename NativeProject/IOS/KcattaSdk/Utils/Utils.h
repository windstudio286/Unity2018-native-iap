//
//  Utils.h
//  VnptSdk
//
//  Created by Tran Trong Cong on 8/5/21.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@interface Utils : NSObject
+ (UIViewController*) topViewController;
+ (NSString*) getDeviceId;
+ (BOOL) screenInPortrait;
+ (UIView*) loadViewFromNibFile:(Class) aClass withNib:(NSString*) name;
+ (UIView*) loadViewFromNibFile:(Class) aClass universalWithNib:(NSString*) name;
+ (float) ratioScreen;
+ (float) heightScreen;
+ (float) widthScreen;
+ (UIColor *)colorFromHexString:(NSString *)hexString;
+ (void) addConstraintForChild:(UIView*)parent andChild:(UIView*) child withLeft:(float) left withTop:(float) left andRight:(float) right withBottom:(float) bottom;
+ (UIImage *)imageWithImage:(UIImage *)image scaledToSize:(CGSize)newSize;
+ (void) logMessage:(NSString*) msg;
+(void) delayAction:(void (^)(void))action withTime:(float) time;
+(Class) getClassofProperty:(Class) aClass withProperty:(NSString*) nameProperty;
+(NSString *)typeForProperty:(NSString *)property andClass:(Class) aClass;
+(NSString*) displayDate:(NSDate*) date;
+(BOOL) date1IsGreaterOrEqualThan:(NSDate*) date1 date2:(NSDate*) date2;
+ (NSString *) md5str: ( NSString *) str;
@end
