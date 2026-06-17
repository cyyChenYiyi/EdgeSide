$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\AndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\AndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME = "C:\GradleHome"
$env:TEMP = "C:\Users\cloud_user\AppData\Local\Temp"
$env:TMP = "C:\Users\cloud_user\AppData\Local\Temp"
$env:GRADLE_OPTS = "-Dfile.encoding=UTF-8 -Xmx4g"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Building APK with verbose output..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon","--info" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru -RedirectStandardError "$env:TEMP\gradle_verbose.log"

Write-Host "Exit code: $($proc.ExitCode)"

# Show last 200 lines of error log
Get-Content "$env:TEMP\gradle_verbose.log" | Select-Object -Last 200
