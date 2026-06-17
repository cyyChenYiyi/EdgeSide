# Download Gradle directly
$gradleUrl = "https://services.gradle.org/distributions/gradle-8.7-bin.zip"
$gradleZip = "$env:TEMP\gradle-8.7.zip"
$gradleHome = "C:\gradle-8.7"

if (-not (Test-Path $gradleHome)) {
    Write-Host "Downloading Gradle 8.7..."
    Start-BitsTransfer -Source $gradleUrl -Destination $gradleZip

    Write-Host "Extracting..."
    Expand-Archive -Path $gradleZip -DestinationPath "C:\" -Force
    Remove-Item $gradleZip -Force
}

Write-Host "Gradle installed at: $gradleHome"

# Now use this Gradle to build
$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;C:\gradle-8.7\bin;$env:PATH"

$projectDir = "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "ANDROID_HOME: $env:ANDROID_HOME"
Write-Host "Building APK with local Gradle..."

& gradle.bat assembleDebug --no-daemon -p $projectDir
