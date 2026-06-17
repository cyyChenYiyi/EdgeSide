$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "ANDROID_HOME: $env:ANDROID_HOME"
Write-Host "Building APK..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

if ($proc.ExitCode -eq 0) {
    Write-Host "Build SUCCESS!"
} else {
    Write-Host "Build FAILED with exit code: $($proc.ExitCode)"
}
