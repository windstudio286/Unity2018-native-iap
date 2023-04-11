//
//  BaseUIView.h
//  Unity-iPhone
//
//  Created by TranCong on 03/04/2023.
//

#import <Foundation/Foundation.h>
#import "Sdk.h"
#import "LoadingView.h"
NS_ASSUME_NONNULL_BEGIN
typedef void(^ViewHideCallback)(NSString* identifier);

@interface BaseUIView : UIView
@property (nonatomic, readwrite, copy) ViewHideCallback callback;
@property (nonatomic, assign) id<SdkDelegate> delegate;

-(IBAction)btnClose:(id)sender;

-(void) initInternals;
-(void) configUI:(UIView*)parentView;
-(void)showLoading;
-(void)showLoading:(NSString*)note;
-(void)hideLoading;
-(void)showAlert:(NSString*)title withContent:(NSString*) content withAction:(void (^)(UIAlertAction*))action;
-(void)showAlertWith2Action:(NSString*)title withContent:(NSString*) content withOkAction:(void (^)(UIAlertAction*))okAction andCancelAction:(void (^)(UIAlertAction*))cancelAction;
-(void)showAlertWith2Action:(NSString *)title withContent:(NSString *)content withOkTitle:(NSString*) okTitle withOkAction:(void (^)(UIAlertAction * _Nonnull))okAction andCancelAction:(void (^)(UIAlertAction * _Nonnull))cancelAction;

@end

NS_ASSUME_NONNULL_END
