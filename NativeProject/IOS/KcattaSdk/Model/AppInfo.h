//
//  AppInfo.h
//  Unity-iPhone
//
//  Created by TranCong on 02/04/2023.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AppInfo : NSObject
@property (nonatomic,strong) NSString *oneSignalId;
@property (nonatomic,strong) NSArray<NSString *> *testDeviceIdentifiers;
@end

NS_ASSUME_NONNULL_END
