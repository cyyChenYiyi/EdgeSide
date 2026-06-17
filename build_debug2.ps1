$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\And"
$env:ANDROID_SDK_ROOT = "C:\And"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Building APK with debug output..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon","--debug" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru -RedirectStandardError "$env:TEMP\gradle_debug.log"

Write-Host "Exit code: $($proc.ExitCode)"

# Show last 100 lines of error log
Get-Content "$env:TEMP\gradle_debug.log" | Select-Object -Last 100
