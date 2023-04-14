//
//  Utils.m
//  VnptSdk
//
//  Created by Tran Trong Cong on 8/5/21.
//

#import "Utils.h"
#import <objc/runtime.h>
#import <CommonCrypto/CommonDigest.h>
#import <CommonCrypto/CommonCryptor.h>
#import <CommonCrypto/CommonHMAC.h>
#import "Sdk.h"
#import "AppInfo.h"
#import "Keychain/Keychain.h"
#define SERVICE_NAME @"SECRET_AUTH"
#define CC_MD5_DIGEST_LENGTH    16          /* digest length in bytes */
#define CC_MD5_BLOCK_BYTES      64          /* block size in bytes */
#define CC_MD5_BLOCK_LONG       (CC_MD5_BLOCK_BYTES / sizeof(CC_LONG))
@implementation Utils
+(UIViewController *)topViewController{
    return [self topViewControllerWithRootViewController:[UIApplication sharedApplication].keyWindow.rootViewController];
}

+(UIViewController*)topViewControllerWithRootViewController:(UIViewController*)rootViewController {
    if ([rootViewController isKindOfClass:[UITabBarController class]]) {
        UITabBarController* tabBarController = (UITabBarController*)rootViewController;
        return [self topViewControllerWithRootViewController:tabBarController.selectedViewController];
    } else if ([rootViewController isKindOfClass:[UINavigationController class]]) {
        UINavigationController* navigationController = (UINavigationController*)rootViewController;
        return [self topViewControllerWithRootViewController:navigationController.visibleViewController];
    } else if (rootViewController.presentedViewController) {
        UIViewController* presentedViewController = rootViewController.presentedViewController;
        return [self topViewControllerWithRootViewController:presentedViewController];
    } else {
        return rootViewController;
    }
}
+(NSString *)getDeviceId{
    Keychain *keychain = [[Keychain alloc] initWithService:SERVICE_NAME withGroup:nil];
    
    NSString *deviceId = nil;
    if(keychain != NULL){
        
        NSData * data =[keychain find:KEY_DEVICE_ID];
        if(data == nil)
        {
            [self logMessage:@"deviceID not found. Now, It's create new one"];
            deviceId =[[[[UIDevice currentDevice] identifierForVendor] UUIDString] lowercaseString];
            NSData *value = [deviceId dataUsingEncoding:NSUTF8StringEncoding];
             
            if([keychain insert:KEY_DEVICE_ID :value])
             {
                 [self logMessage:@"Successfully added data"];
             }
             else
                 [self logMessage:@"Failed to add data"];
        }
        else
        {
            deviceId = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
            deviceId = [self md5str:deviceId];
        }
    }
    [self logMessage:[NSString stringWithFormat:@"deviceID : %@",deviceId]];
    return deviceId;
}
+(NSString *)md5str:(NSString *)str{
    const char *cStr = [str UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5( cStr, strlen(cStr), result );
    NSString* temp = [NSString  stringWithFormat:
                      @"%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
                      result[0], result[1], result[2], result[3], result[4],
                      result[5], result[6], result[7],
                      result[8], result[9], result[10], result[11], result[12],
                      result[13], result[14], result[15]
                      ];
    //  temp = [temp substringToIndex:16];
    return [temp lowercaseString];
}
+ (BOOL)screenInPortrait{
    UIInterfaceOrientation interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    if (UIInterfaceOrientationIsPortrait(interfaceOrientation))
    {
        return TRUE;
    }
    return FALSE;
}
+ (UIView *)loadViewFromNibFile:(Class)aClass withNib:(NSString *)name{
    BOOL isPortrait = [self screenInPortrait];
    NSBundle *myBundle = [NSBundle bundleForClass:aClass];
    UIView *customView = nil;
    if(isPortrait){
        customView = [[myBundle loadNibNamed:name owner:self options:nil] objectAtIndex:0];
    }
    else{
        customView = [[myBundle loadNibNamed:[NSString stringWithFormat:@"%@-landscape",name] owner:self options:nil] objectAtIndex:0];
    }
    return customView;
}
+(UIView *)loadViewFromNibFile:(Class)aClass universalWithNib:(NSString *)name{
    UIView *customView = nil;
    NSBundle *myBundle = [NSBundle bundleForClass:aClass];
    customView = [[myBundle loadNibNamed:name owner:self options:nil] objectAtIndex:0];
    return customView;
}
//https://developer.apple.com/library/archive/documentation/DeviceInformation/Reference/iOSDeviceCompatibility/Displays/Displays.html
+(float)ratioScreen{
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    float maxWidth = MAX(screenRect.size.width* [[UIScreen mainScreen] scale],screenRect.size.height*[[UIScreen mainScreen] scale]);
    
    float maxHeight = MIN(screenRect.size.width*[[UIScreen mainScreen] scale],screenRect.size.height*[[UIScreen mainScreen] scale]);
    
    NSLog(@"Screen in width %f and height %f",maxWidth,maxHeight);
    return maxWidth*1.0/maxHeight;
}
+(float)heightScreen{
    return [[UIScreen mainScreen] bounds].size.height * [[UIScreen mainScreen] scale];
}
+(float)widthScreen{
    return [[UIScreen mainScreen] bounds].size.width * [[UIScreen mainScreen] scale];
}
+ (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1]; // bypass '#' character
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}
+(void)addConstraintForChild:(UIView *)parent andChild:(UIView *)child withLeft:(float)left withTop:(float)top andRight:(float)right withBottom:(float)bottom{
    [parent addConstraints:@[
        //algin with bottom
        [NSLayoutConstraint constraintWithItem:parent
                                     attribute:NSLayoutAttributeBottom
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:child //self.bottomLayoutGuide
                                     attribute:NSLayoutAttributeBottom //NSLayoutAttributeTop
                                    multiplier:1
                                      constant:bottom],
        //align leading
        [NSLayoutConstraint constraintWithItem:child
                                     attribute:NSLayoutAttributeLeading
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:parent
                                     attribute:NSLayoutAttributeLeading
                                    multiplier:1
                                      constant:left],
        //align trailing
        [NSLayoutConstraint constraintWithItem:child
                                     attribute:NSLayoutAttributeTrailing
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:parent
                                     attribute:NSLayoutAttributeTrailing
                                    multiplier:1
                                      constant:right],
        //align top
        [NSLayoutConstraint constraintWithItem:child
                                     attribute:NSLayoutAttributeTop
                                     relatedBy:NSLayoutRelationEqual
                                        toItem:parent
                                     attribute:NSLayoutAttributeTop
                                    multiplier:1
                                      constant:top]
    ]];
}
+ (UIImage *)imageWithImage:(UIImage *)image scaledToSize:(CGSize)newSize {
    //UIGraphicsBeginImageContext(newSize);
    // In next line, pass 0.0 to use the current device's pixel scaling factor (and thus account for Retina resolution).
    // Pass 1.0 to force exact pixel size.
    UIGraphicsBeginImageContextWithOptions(newSize, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}
+(void)logMessage:(NSString *)msg{
    //#ifdef DEBUG
    // Something to log your sensitive data here
    NSLog(@"%@",msg);
    //#else
    //
    //#endif
}
+(void)delayAction:(void (^)(void))action withTime:(float) time{
    NSTimeInterval delayInSeconds = time;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), action);
}
+(Class)getClassofProperty:(Class)aClass withProperty:(NSString *) nameProperty{
    //https://useyourloaf.com/blog/objective-c-class-properties/
    //https://developer.apple.com/forums/thread/653916
    //https://stackoverflow.com/questions/16861204/property-type-or-class-using-reflection
    const char *cName=[nameProperty UTF8String];
    objc_property_t property = class_getProperty(aClass, cName);
    const char * name = property_getName(property);
    NSString *propertyName = [NSString stringWithCString:name encoding:NSUTF8StringEncoding];
    const char * type = property_getAttributes(property);
    NSString *attr = [NSString stringWithCString:type encoding:NSUTF8StringEncoding];
    
    NSString * typeString = [NSString stringWithUTF8String:type];
    NSArray * attributes = [typeString componentsSeparatedByString:@","];
    NSString * typeAttribute = [attributes objectAtIndex:0];
    NSString * propertyType = [typeAttribute substringFromIndex:1];
    const char * rawPropertyType = [propertyType UTF8String];
    
    if (strcmp(rawPropertyType, @encode(float)) == 0) {
        //it's a float
    } else if (strcmp(rawPropertyType, @encode(int)) == 0) {
        //it's an int
    } else if (strcmp(rawPropertyType, @encode(id)) == 0) {
        //it's some sort of object
    } else {
        // According to Apples Documentation you can determine the corresponding encoding values
    }
    
    if ([typeAttribute hasPrefix:@"T@"]) {
        NSString * typeClassName = [typeAttribute substringWithRange:NSMakeRange(3, [typeAttribute length]-4)];  //turns @"NSDate" into NSDate
        Class typeClass = NSClassFromString(typeClassName);
        if (typeClass != nil) {
            // Here is the corresponding class even for nil values
            return typeClass;
        }
    }
    return nil;
}

+(NSString *)typeForProperty:(NSString *)property andClass:(Class)class
{
    const char *type = property_getAttributes(class_getProperty(class, [property UTF8String]));
    NSString *typeString = [NSString stringWithUTF8String:type];
    NSArray *attributes = [typeString componentsSeparatedByString:@","];
    NSString *typeAttribute = [attributes objectAtIndex:0];
    NSString *className = [[[typeAttribute substringFromIndex:1]
                            stringByReplacingOccurrencesOfString:@"@" withString:@""]
                           stringByReplacingOccurrencesOfString:@"\"" withString:@""];
    
    
    return className;
}
+(NSString *)displayDate:(NSDate *)date{
    NSString *dateString = [NSDateFormatter localizedStringFromDate:date
                                                          dateStyle:kCFDateFormatterFullStyle
                                                          timeStyle:NSDateFormatterFullStyle];
    return dateString;
}
+(BOOL)date1IsGreaterOrEqualThan:(NSDate *)date1 date2:(NSDate *)date2{
    BOOL isGreaterOrEqual = FALSE;
    NSComparisonResult result = [date1 compare:date2];
    
    if(result==NSOrderedAscending){
        isGreaterOrEqual = FALSE;
        //NSLog(@"Date1 is less than Date2");
    }
    
    else if(result==NSOrderedDescending){
        isGreaterOrEqual = TRUE;
        //NSLog(@"Date1 is greater than Date2");
    }
    
    else{
        isGreaterOrEqual = TRUE;
        //NSLog(@"Both dates are the same");
    }
    return isGreaterOrEqual;
}

@end
