$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$sdkmanager = "C:\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "Installing additional SDK components..."

# Install cmake (sometimes needed for native builds)
& $sdkmanager "cmake;3.22.1" 2>&1

# Install ndk
& $sdkmanager "ndk;26.1.10909125" 2>&1

Write-Host "Done!"
