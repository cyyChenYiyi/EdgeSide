$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Cleaning build cache..."
Remove-Item "$projectDir\.gradle" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item "$projectDir\app\build" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Building APK with info..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon","--info" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru -RedirectStandardError "$env:TEMP\gradle_err.log"

if ($proc.ExitCode -eq 0) {
    Write-Host "Build SUCCESS!"
} else {
    Write-Host "Build FAILED"
    Write-Host "Error log:"
    Get-Content "$env:TEMP\gradle_err.log" | Select-Object -Last 50
}
