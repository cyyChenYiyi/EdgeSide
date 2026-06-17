$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\NewAndroidSDK"
$env:ANDROID_SDK_ROOT = "C:\NewAndroidSDK"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$sdkmanager = "C:\NewAndroidSDK\cmdline-tools\latest\bin\sdkmanager.bat"

# Create new SDK directory
New-Item -Path "C:\NewAndroidSDK" -ItemType Directory -Force | Out-Null
New-Item -Path "C:\NewAndroidSDK\cmdline-tools" -ItemType Directory -Force | Out-Null

Write-Host "Downloading cmdline-tools to new location..."

# Download cmdline-tools
$cmdlineUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$zipPath = "$env:TEMP\cmdline-tools-new.zip"

Start-BitsTransfer -Source $cmdlineUrl -Destination $zipPath

Write-Host "Extracting..."
$tempExtract = "$env:TEMP\cmdline-extract-new"
if (Test-Path $tempExtract) { Remove-Item $tempExtract -Recurse -Force }
Expand-Archive -Path $zipPath -DestinationPath $tempExtract -Force

# Move to correct location
Get-ChildItem $tempExtract -Directory | Select-Object -First 1 | ForEach-Object {
    Move-Item $_.FullName "C:\NewAndroidSDK\cmdline-tools\latest" -Force
}

Write-Host "Installing SDK components..."
& $sdkmanager --licenses <<< "y`ny`ny`ny`ny`ny`ny`ny`n"
& $sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

Write-Host "Done! Verifying:"
& $sdkmanager --list_installed
