# Download Android SDK components using background job
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$sdkDir = "C:\Users\CLOUD_~1\AppData\Local\Temp\android-sdk"
$tempDir = $env:TEMP

# Try direct Google download with resume support
$platformsUrl = "https://dl.google.com/android/repository/platform-34_r03.zip"
$buildToolsUrl = "https://dl.google.com/android/repository/build-tools_r34.0.0-win.zip"

Write-Host "Downloading platforms..."
$platformZip = "$tempDir\platform-34.zip"
try {
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $platformsUrl -OutFile $platformZip -UseBasicParsing -TimeoutSec 300
    if (Test-Path $platformZip) {
        $size = (Get-Item $platformZip).Length / 1MB
        Write-Host "platforms downloaded: $([math]::Round($size, 2)) MB"
        
        # Extract
        Write-Host "Extracting platforms..."
        Expand-Archive -Path $platformZip -DestinationPath "$sdkDir\temp_platforms" -Force
        Get-ChildItem "$sdkDir\temp_platforms" | Move-Item -Destination "$sdkDir\platforms" -Force
        Remove-Item "$sdkDir\temp_platforms" -Recurse -Force
        Write-Host "platforms installed"
        Remove-Item $platformZip -Force
    }
} catch {
    Write-Host "platforms download failed: $($_.Exception.Message)"
}

Write-Host "Downloading build-tools..."
$buildZip = "$tempDir\build-tools-34.zip"
try {
    Invoke-WebRequest -Uri $buildToolsUrl -OutFile $buildZip -UseBasicParsing -TimeoutSec 300
    if (Test-Path $buildZip) {
        $size = (Get-Item $buildZip).Length / 1MB
        Write-Host "build-tools downloaded: $([math]::Round($size, 2)) MB"
        
        # Extract
        Write-Host "Extracting build-tools..."
        Expand-Archive -Path $buildZip -DestinationPath "$sdkDir\temp_buildtools" -Force
        Get-ChildItem "$sdkDir\temp_buildtools" | Move-Item -Destination "$sdkDir\build-tools" -Force
        Remove-Item "$sdkDir\temp_buildtools" -Recurse -Force
        Write-Host "build-tools installed"
        Remove-Item $buildZip -Force
    }
} catch {
    Write-Host "build-tools download failed: $($_.Exception.Message)"
}

Write-Host "Done!"
