# Check and enable Long Path support
Write-Host "Checking Windows Long Path support..."

# Check registry
$key = Get-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -ErrorAction SilentlyContinue
if ($key) {
    Write-Host "LongPathsEnabled value: $($key.LongPathsEnabled)"
} else {
    Write-Host "LongPathsEnabled registry key not found"
}

# Try to enable it
try {
    Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1 -Type DWord -Force
    Write-Host "Enabled LongPathsEnabled"
} catch {
    Write-Host "Could not enable LongPathsEnabled: $($_.Exception.Message)"
}

# Now try building again
$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\AndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\AndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME = "C:\GradleHome3"
$env:TEMP = "C:\Users\cloud_user\AppData\Local\Temp"
$env:TMP = "C:\Users\cloud_user\AppData\Local\Temp"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

New-Item -Path $env:GRADLE_USER_HOME -ItemType Directory -Force | Out-Null

Write-Host "Building APK with Long Path support enabled..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

Write-Host "Exit code: $($proc.ExitCode)"

$apkPath = "$projectDir\app\build\outputs\apk\debug"
if (Test-Path $apkPath) {
    Write-Host "APK found:"
    Get-ChildItem $apkPath
} else {
    Write-Host "APK not found"
}
