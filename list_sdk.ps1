$env:ANDROID_HOME = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"

$sdkmanager = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat"

& $sdkmanager --list 2>&1 | Select-String -Pattern "platforms;android|build-tools;34"
