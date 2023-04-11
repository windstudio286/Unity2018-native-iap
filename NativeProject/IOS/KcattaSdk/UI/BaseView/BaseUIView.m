//
//  BaseUIView.m
//  Unity-iPhone
//
//  Created by TranCong on 03/04/2023.
//

#import "BaseUIView.h"

@implementation BaseUIView

{
    LoadingView *loadingView;
    BOOL isInited;
}
@synthesize callback;
- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        //init from frame
        [self initInternals];
    }
    return self;
}
-(instancetype)initWithCoder:(NSCoder *)coder
{
    self = [super initWithCoder:coder];
    if (self) {
        //init from xib or storyboard
        [self initInternals];
    }
    return self;
}
//init variable in here
-(void) initInternals{
    isInited = FALSE;
    if (@available(iOS 13.0, *)) {
        self.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
    } else {
        // Fallback on earlier versions
    }
}
-(void)willMoveToSuperview:(UIView *)newSuperview{
    if(!isInited){
        [self configUI:newSuperview];
        isInited = TRUE;
    }
}
-(void)btnClose:(id)sender{
    if(callback != nil){
        NSString* identifier = NSStringFromClass(self.class);
        self.callback(identifier);
    }
}
-(void)configUI:(UIView *)parentView{
    
}

-(void)showLoading{
    //display loading
    if(self->loadingView == NULL){
        self->loadingView = (LoadingView*)[Utils loadViewFromNibFile:[LoadingView class] universalWithNib:@"LoadingView"];


    [self addSubview:self->loadingView];
    [Utils addConstraintForChild:self andChild:self->loadingView withLeft:0 withTop:0 andRight:0 withBottom:0];
    }
    CABasicAnimation* animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    animation.fromValue = @0.0f;
    animation.toValue = @(2*M_PI);
    animation.duration = 1;             // this might be too fast
    animation.repeatCount = HUGE_VALF;     // HUGE_VALF is defined in math.h so import it
    [self->loadingView.iconLoading.layer addAnimation:animation forKey:@"rotation"];
    self->loadingView.noteLoading.hidden = YES;
    self->loadingView.translatesAutoresizingMaskIntoConstraints = NO;
}

-(void)showLoading:(NSString*)note{
    //display loading
    if(self->loadingView == NULL){
        self->loadingView = (LoadingView*)[Utils loadViewFromNibFile:[LoadingView class] universalWithNib:@"LoadingView"];

   
    self->loadingView.translatesAutoresizingMaskIntoConstraints = NO;
    [self addSubview:self->loadingView];
    [Utils addConstraintForChild:self andChild:self->loadingView withLeft:0 withTop:0 andRight:0 withBottom:0];
    }
    CABasicAnimation* animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    animation.fromValue = @0.0f;
    animation.toValue = @(2*M_PI);
    animation.duration = 1;             // this might be too fast
    animation.repeatCount = HUGE_VALF;     // HUGE_VALF is defined in math.h so import it
    [self->loadingView.iconLoading.layer addAnimation:animation forKey:@"rotation"];
    self->loadingView.noteLoading.hidden = NO;
    self->loadingView.noteLoading.text = note;
}

-(void)hideLoading{
    if(self->loadingView != NULL && self->loadingView.superview != NULL){
        [self->loadingView removeFromSuperview];
        self->loadingView = NULL;
    }
}
- (void)showAlert:(NSString *)title withContent:(NSString *)content withAction:(void (^)(UIAlertAction*))action{
    NSString *newTitle = title;
    if(newTitle == nil){
        newTitle = [TSLanguageManager localizedString:@"Thông báo"];
    }
    UIAlertController * alert = [UIAlertController
                                 alertControllerWithTitle:newTitle
                                 message:content
                                 preferredStyle:UIAlertControllerStyleAlert];
    
    //Add Buttons
    
    UIAlertAction* yesButton = [UIAlertAction
                                actionWithTitle:[TSLanguageManager localizedString:@"Đồng ý"]
                                style:UIAlertActionStyleDefault
                                handler:action];
    [alert addAction:yesButton];
    UIViewController * top = [Utils topViewController];
    [top presentViewController:alert animated:YES completion:nil];
}
-(void)showAlertWith2Action:(NSString *)title withContent:(NSString *)content withOkAction:(void (^)(UIAlertAction * _Nonnull))okAction andCancelAction:(void (^)(UIAlertAction * _Nonnull))cancelAction{
    
    UIAlertController * alert = [UIAlertController
                                 alertControllerWithTitle:title
                                 message:content
                                 preferredStyle:UIAlertControllerStyleAlert];
    
    //Add Ok Buttons
    
    UIAlertAction* okButton = [UIAlertAction
                               actionWithTitle:[TSLanguageManager localizedString:@"Đồng ý"]
                               style:UIAlertActionStyleDefault
                               handler:okAction];
    //Add Cancel Buttons
    
    UIAlertAction* cancelButton = [UIAlertAction
                                   actionWithTitle:[TSLanguageManager localizedString:@"Huỷ"]
                                   style:UIAlertActionStyleDefault
                                   handler:cancelAction];
    [alert addAction:okButton];
    [alert addAction:cancelButton];
    UIViewController * top = [Utils topViewController];
    [top presentViewController:alert animated:YES completion:nil];
}

-(void)showAlertWith2Action:(NSString *)title withContent:(NSString *)content withOkTitle:(NSString*) okTitle withOkAction:(void (^)(UIAlertAction * _Nonnull))okAction andCancelAction:(void (^)(UIAlertAction * _Nonnull))cancelAction{
    
    UIAlertController * alert = [UIAlertController
                                 alertControllerWithTitle:title
                                 message:content
                                 preferredStyle:UIAlertControllerStyleAlert];
    
    //Add Ok Buttons
    
    UIAlertAction* okButton = [UIAlertAction
                               actionWithTitle:okTitle
                               style:UIAlertActionStyleDefault
                               handler:okAction];
    //Add Cancel Buttons
    
    UIAlertAction* cancelButton = [UIAlertAction
                                   actionWithTitle:[TSLanguageManager localizedString:@"Huỷ"]
                                   style:UIAlertActionStyleDefault
                                   handler:cancelAction];
    [alert addAction:okButton];
    [alert addAction:cancelButton];
    UIViewController * top = [Utils topViewController];
    [top presentViewController:alert animated:YES completion:nil];
}
@end
