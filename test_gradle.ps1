$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\AndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\AndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME = "C:\GradleHome"
$env:TEMP = "C:\Users\cloud_user\AppData\Local\Temp"
$env:TMP = "C:\Users\cloud_user\AppData\Local\Temp"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Testing simple Gradle task..."

$proc = Start-Process -FilePath "$projectDir\gradlew.bat" -ArgumentList "tasks","--no-daemon" -WorkingDirectory $projectDir -NoNewWindow -Wait -PassThru

Write-Host "Exit code: $($proc.ExitCode)"
