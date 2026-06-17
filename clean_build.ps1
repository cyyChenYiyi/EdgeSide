$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\AndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\AndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME = "C:\GradleHome"
$env:TEMP = "C:\Users\cloud_user\AppData\Local\Temp"
$env:TMP = "C:\Users\cloud_user\AppData\Local\Temp"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Cleaning ALL Gradle caches..."

# Clean Gradle home
Remove-Item "$env:GRADLE_USER_HOME" -Recurse -Force -ErrorAction SilentlyContinue

# Clean project .gradle
Remove-Item "$projectDir\.gradle" -Recurse -Force -ErrorAction SilentlyContinue

# Clean app build
Remove-Item "$projectDir\app\build" -Recurse -Force -ErrorAction SilentlyContinue

# Clean user .gradle (in case of permission issues)
$userGradle = "C:\Users\cloud_user\.gradle"
Remove-Item $userGradle -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Building APK with fresh Gradle..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon","--refresh-dependencies" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

Write-Host "Exit code: $($proc.ExitCode)"

# Check for APK
$apkPath = "$projectDir\app\build\outputs\apk\debug"
if (Test-Path $apkPath) {
    Write-Host "APK found:"
    Get-ChildItem $apkPath
} else {
    Write-Host "APK not found"
}
