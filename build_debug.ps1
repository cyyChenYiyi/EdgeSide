$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_OPTS = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

# Remove configuration cache
Remove-Item "$projectDir\.gradle/configuration-cache" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Building APK..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "assembleDebug","--no-daemon","--configuration-cache","off","--refresh-dependencies" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

Write-Host "Exit code: $($proc.ExitCode)"
