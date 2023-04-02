//
//  NativeWrapper.h
//  Unity-iPhone
//
//  Created by TranCong on 01/04/2023.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NativeWrapper : NSObject

@end
extern "C" {
    void sendDataFromUnity (const char* key, const char* value);
}
NS_ASSUME_NONNULL_END
