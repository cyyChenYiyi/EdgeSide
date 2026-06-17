$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Testing Java..."
& java -version 2>&1

Write-Host "Building APK..."
& "$projectDir\gradlew.bat" assembleDebug --no-daemon -p "$projectDir" 2>&1
