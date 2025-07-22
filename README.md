Test Add to App

To build
```
pushd flutter_module>/dev/null && etbd && fbrd && popd>/dev/null && ./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.example.testaddtoapp/.MainActivity && flutter logs
