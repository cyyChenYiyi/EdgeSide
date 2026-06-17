$env:ANDROID_HOME = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_SDK_ROOT = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"

$sdkmanager = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "Installing platforms;android-34..."
& $sdkmanager "platforms;android-34" 2>&1

Write-Host "Installing build-tools;34.0.0..."
& $sdkmanager "build-tools;34.0.0" 2>&1

Write-Host "Done! Verifying installed components:"
& $sdkmanager --list_installed 2>&1
