Test Add to App

To build
```
pushd flutter_module>/dev/null && et build -c host_debug_unopt_arm64 && et build -c android_debug_unopt_arm64 && flutter build aar --debug --local-engine-src-path /Users/boetger/src/flutter/engine/src --local-engine=android_debug_unopt_arm64 --local-engine-host=host_debug_unopt_arm64 --no-profile --no-release && popd>/dev/null && ./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.example.testaddtoapp/.MainActivity
```


