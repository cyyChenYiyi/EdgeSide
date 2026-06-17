$licenseDir = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk\licenses"

# Android SDK License
Set-Content -Path "$licenseDir\android-sdk-license" -Value @"
8933bad161af4178b1185d1a37fbf41ea5269c55
d56f5187479451eabf01fb78af6dfcb131a6481e
24333f8a63b6825ea9c5514f83c2829b004d1fee
"@

# Google APIs License
Set-Content -Path "$licenseDir\android-sdk-preview-license" -Value @"
84831b9409646a918e30573bab4c9c91346d8abd
"@

Write-Host "Licenses written"

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
