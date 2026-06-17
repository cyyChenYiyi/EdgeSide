$ErrorActionPreference = "Continue"

$SDK_DIR = "C:\AndroidSDK"
$CMDLINE_URL = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$CMDLINE_ZIP = "$env:TEMP\cmdline-tools.zip"
$CMDLINE_EXTRACT = "$env:TEMP\cmdline-extract"

Write-Host "Downloading cmdline-tools from official source (Google)..."
Write-Host "This may take several minutes, please wait..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

try {
    # Use a longer timeout and add User-Agent header
    $webClient = New-Object System.Net.WebClient
    $webClient.Headers.Add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    $webClient.DownloadFile($CMDLINE_URL, $CMDLINE_ZIP)
    $webClient.Dispose()
    Write-Host "Download complete!"
} catch {
    Write-Host "Download failed: $_"
    Write-Host "Trying alternative method with Invoke-WebRequest..."
    Invoke-WebRequest -Uri $CMDLINE_URL -OutFile $CMDLINE_ZIP -UseBasicParsing -TimeoutSec 600
}

if (-not (Test-Path $CMDLINE_ZIP)) {
    Write-Host "ERROR: Download failed, file not found"
    exit 1
}

$fileSize = (Get-Item $CMDLINE_ZIP).Length
Write-Host "Downloaded file size: $([math]::Round($fileSize/1MB, 2)) MB"

if ($fileSize -lt 1000000) {
    Write-Host "File too small, checking content..."
    Get-Content $CMDLINE_ZIP -Raw | Select-Object -First 500
    Write-Host "ERROR: Downloaded file is too small"
    exit 1
}

Write-Host "Extracting cmdline-tools..."
if (Test-Path $CMDLINE_EXTRACT) { Remove-Item $CMDLINE_EXTRACT -Recurse -Force }
New-Item -ItemType Directory -Path $CMDLINE_EXTRACT -Force | Out-Null
Expand-Archive -Path $CMDLINE_ZIP -DestinationPath $CMDLINE_EXTRACT -Force

# Fix nested directory structure
Write-Host "Fixing cmdline-tools structure..."
if (Test-Path "$CMDLINE_EXTRACT\cmdline-tools") {
    Move-Item -Path "$CMDLINE_EXTRACT\cmdline-tools\*" -Destination $CMDLINE_EXTRACT -Force
    Remove-Item "$CMDLINE_EXTRACT\cmdline-tools" -Recurse -Force
}

New-Item -ItemType Directory -Path "$SDK_DIR\cmdline-tools" -Force | Out-Null
New-Item -ItemType Directory -Path "$SDK_DIR\cmdline-tools\latest" -Force | Out-Null
Move-Item -Path "$CMDLINE_EXTRACT\*" -Destination "$SDK_DIR\cmdline-tools\latest" -Force

# Write license files
Write-Host "Accepting licenses..."
$licenseDir = "$env:USERPROFILE\AppData\Local\Temp\android-sdk\licenses"
New-Item -ItemType Directory -Path $licenseDir -Force | Out-Null
@"
24333f8a63b6825ea9c5514f83c2829b004d1fee
d56f5187479451eabf01fb78af6dfcb131a6481e
"@ | Set-Content "$licenseDir\android-sdk-license"

Write-Host "Installing SDK components..."
& "$SDK_DIR\cmdline-tools\latest\bin\sdkmanager.bat" --install "platforms;android-34" "build-tools;34.0.0" --sdk_root="$SDK_DIR"

Write-Host "`nDone! SDK installed at: $SDK_DIR"