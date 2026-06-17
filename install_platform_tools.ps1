$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$sdkmanager = "C:\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "Checking installed packages..."
& $sdkmanager --list_installed 2>&1

Write-Host "Installing platform-tools..."
& $sdkmanager "platform-tools" 2>&1

Write-Host "Done!"
