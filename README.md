


# <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQPNpoTfN7s-CudM4rAFGbjNSbwARRjiOdu0otHMK9tiYL8__ZhreOhPyO5QHSuRIrSvDo&usqp=CAU" height="50" style="margin-right:20px"/> genesys-cloud-messenger-mobile-sdk-rn-wrapper 

> Genesys Cloud Messaging SDK for React Native

The SDK provides a simple react native wrapper for the Genesys Cloud Messenger SDK.

**Author:** Genesys

**Platform Support:** Android, iOS


- [Getting Started](#getting-started)
  - [Pre-Requisites](#pre-requisites)
  - [Install](#install)
  - [Update](#update)
- [Platform specific additional steps](#platform-specific-additional-steps)
  - [Android](#android)
  - [iOS](#ios)
- [Usage](#usage)
    - [Import](#import)
    - [Start Chat](#start-chat)
- [Sample Application](https://github.com/genesys/MobileDxRNSample)
- [License](#license)

## Getting Started

### Pre-requisites

In order to use this SDK you need a Genesys account with the Messaging feature enabled.

### Install

Run the following on the application root directory.

- **Option 1 - `npm install`**

   ```sh
   npm install genesys-cloud-messenger-mobile-sdk-rn-wrapper --save
   ```

- **Option 2 - `yarn add`**

   ```sh
   yarn add genesys-cloud-messenger-mobile-sdk-rn-wrapper
   ```

- **Install Genesys chat module native dependency**
   
   ```sh
   react-native link genesys-cloud-messenger-mobile-sdk-rn-wrapper
   ```

### Update

To update your project to the latest version of `genesys-messenger-mobile-sdk-rn-wrapper`

   ```sh
   npm update genesys-cloud-messenger-mobile-sdk-rn-wrapper
   ```

## Platform specific additional steps

### android

In order to be able to use the chat module on android please follow the next steps.

1. Go to `build.gradle` file, on the android project of your react native app.
    ```cmd
    YourAppFolder
    ├── android
    │   ├── app
    │   │   ├── build.gradle  
    │   │   ├── proguard-rules.pro
    │   │   └── src
    │   ├── build.gradle   <---
    │   ├── gradle
    │   │   └── wrapper
    │   ├── gradle.properties
    │   ├── gradlew
    │   ├── gradlew.bat
    │   └── settings.gradle
    |
    ```

-  Add the following repositories:
    ```gradle
    maven {url "https://bold360ai-mobile-artifacts.s3.amazonaws.com/dx/android/dev/"}
    maven {url "https://bold360ai-mobile-artifacts.s3.amazonaws.com/dx/common/dev/"}
    ```

- Add the following dependency:
    ```gradle
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30"
    ```

2. Go to `build.gradle` file, on the Android project's internal module.
    ```cmd
    YourAppFolder
    ├── android
    │   ├── app
    │   │   ├── build.gradle  <---
    │   │   ├── proguard-rules.pro
    │   │   └── src
    │   ├── build.gradle
    │   ├── gradle
    │   │   └── wrapper
    │   ├── gradle.properties
    │   ├── gradlew
    │   ├── gradlew.bat
    │   └── settings.gradle
    |
    ```

- Add kotlin plugin as follows:
`apply plugin: "kotlin-android"`

### ios

In order to be able to use the chat module on iOS please follow the next steps.

1. Go to `Podfile` file, on the ios project of your react native app.
    ```cmd
    YourAppFolder
    ├── ios
    │   ├── Podfile   <---
    ```
    -  validate your platform is set to `iOS11` or above.
    ```ruby
    platform :ios, '11.0'
    ```
    -  Add Genesys Messeging SDK sources.
    ```ruby
    source 'https://github.com/genesys/dx-sdk-specs-dev'
    source 'https://github.com/CocoaPods/Specs'
    ```
    -  Add `use_frameworks!` inside `target` scope.
    -  Add below `post_install` inside `target` scope.
    ```ruby
        target 'YourAppTargetName' do
        config = use_native_modules!
        use_frameworks!

        use_react_native!(
            :path => config[:reactNativePath],
            # to enable hermes on iOS, change `false` to `true` and then install pods
            :hermes_enabled => false
        )

        post_install do |installer|
            react_native_post_install(installer)

            installer.pods_project.targets.each do |target|
            target.build_configurations.each do |config|
                config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
            end

            if (target.name&.eql?('FBReactNativeSpec'))
                target.build_phases.each do |build_phase|
                if (build_phase.respond_to?(:name) && build_phase.name.eql?('[CP-User] Generate Specs'))
                    target.build_phases.move(build_phase, 0)
                end
                end
            end
            end
        end
        end
    ```
    >[Podfile Full Example](https://github.com/genesys/MobileDxRNSample/blob/master/ios/Podfile)

    - **Disable `Flipper` if activated.**
    ```ruby
         # use_flipper!()
    ```

## Usage

### import

Import `GenesysCloud` module.

```javascript
import { NativeModules } from 'react-native';
const { GenesysCloud } = NativeModules;
```

### start-chat

Call `startChat` to get the messenging view and start conversation with an agent.

```javascript
// Now the chat can be triggered using the following method activation:
GenesysCloud.startChat(deploymentId, domain, tokenStoreKey, logging);
```

## License

MIT
