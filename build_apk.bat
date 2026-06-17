@echo off
set JAVA_HOME=C:\jdk17\jdk-17.0.11+9
set ANDROID_HOME=C:\Users\cloud_user\AppData\Local\Temp\android-sdk
set ANDROID_SDK_ROOT=C:\Users\cloud_user\AppData\Local\Temp\android-sdk
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d C:\Users\cloud_user\Desktop\edgeside
echo Building APK...
call gradlew.bat assembleDebug --no-daemon
