$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\AndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\AndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$sdkmanager = "C:\AndroidSDK\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "Verifying SDK installation..."
Write-Host "Installed packages:"
& $sdkmanager --list_installed 2>&1

Write-Host "`nChecking platforms/android-34 directory:"
Get-ChildItem "C:\AndroidSDK\platforms\android-34" -Name

Write-Host "`nChecking build-tools/34.0.0 directory:"
Get-ChildItem "C:\AndroidSDK\build-tools\34.0.0" -Name

Write-Host "`nTesting if we can read android.jar:"
$jarPath = "C:\AndroidSDK\platforms\android-34\android.jar"
$file = Get-Item $jarPath
Write-Host "android.jar exists: $($file.Exists)"
Write-Host "android.jar size: $($file.Length)"
