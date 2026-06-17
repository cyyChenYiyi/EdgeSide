$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Building APK with stacktrace..."
$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon","--stacktrace" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

Write-Host "Exit code: $($proc.ExitCode)"
