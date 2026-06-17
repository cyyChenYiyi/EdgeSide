$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Cleaning build cache..."
Remove-Item "$projectDir\.gradle" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "$projectDir\app\build" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "ANDROID_HOME: $env:ANDROID_HOME"
Write-Host "Building APK..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

Write-Host "Exit code: $($proc.ExitCode)"

# Check for APK
$apkPath = "$projectDir\app\build\outputs\apk\debug"
if (Test-Path $apkPath) {
    Write-Host "APK found:"
    Get-ChildItem $apkPath
} else {
    Write-Host "APK not found"
}
