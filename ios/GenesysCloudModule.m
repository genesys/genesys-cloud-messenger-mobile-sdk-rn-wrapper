#import "GenesysCloudModule.h"
#import <GenesysCloud/GenesysCloud.h>

/************************************************************/
// MARK: - GenesysCloudModule
/************************************************************/

@interface GenesysCloudModule()<ChatControllerDelegate>

/************************************************************/
// MARK: - Properties
/************************************************************/
@property (nonatomic, strong) ChatController *chatController;
@end

@implementation GenesysCloudModule

RCT_EXPORT_MODULE(GenesysCloud)

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

/************************************************************/
// MARK: - Exported Methods
/************************************************************/

RCT_EXPORT_METHOD(startChat: (NSString *)deploymentId: (NSString *)domain: (NSString *)tokenStoreKey: (BOOL)logging) {
    MessengerAccount *account = [self setupAccount:deploymentId domain:domain tokenStoreKey:tokenStoreKey logging:logging];
    [self startChatWithAccount:account];
}

/************************************************************/
// MARK: - Private Methods
/************************************************************/

- (MessengerAccount *)setupAccount:(NSString *)deploymentId
                            domain:(NSString *)domin
                     tokenStoreKey:(NSString *)tokenStoreKey
                           logging:(BOOL)logging {
    return [[MessengerAccount alloc] initWithDeploymentId:deploymentId domain:domin tokenStoreKey:tokenStoreKey logging:logging];
}

- (void)startChatWithAccount:(MessengerAccount *)account {
    self.chatController = [[ChatController alloc] initWithAccount:account];
    [self setupInitialConfigurations];
    self.chatController.delegate = self;
}

- (void)setupInitialConfigurations {
    self.chatController.viewConfiguration.incomingBotConfig.avatar = nil;
    self.chatController.viewConfiguration.incomingLiveConfig.avatar = nil;
    self.chatController.viewConfiguration.outgoingConfig.avatar = nil;
}

- (void)doneButtonPressed {
    UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
    [rootViewController dismissViewControllerAnimated:YES completion:nil];
}

/************************************************************/
// MARK: - ChatControllerDelegate
/************************************************************/

- (void)shouldPresentChatViewController:(UINavigationController *)viewController {
    viewController.modalPresentationStyle = UIModalPresentationOverFullScreen;
    UIBarButtonItem *doneBarButtonItem = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemDone target:self action:@selector(doneButtonPressed)];
    viewController.navigationBar.topItem.rightBarButtonItem = doneBarButtonItem;
    UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
    [rootViewController presentViewController:viewController animated:YES completion:nil];
}

@end
