# Create fresh build directory
$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\AndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\AndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME = "C:\GradleHome2"
$env:TEMP = "C:\Users\cloud_user\AppData\Local\Temp"
$env:TMP = "C:\Users\cloud_user\AppData\Local\Temp"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

# Create new gradle home
New-Item -Path $env:GRADLE_USER_HOME -ItemType Directory -Force | Out-Null

Write-Host "Building with fresh Gradle home: $env:GRADLE_USER_HOME"

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
