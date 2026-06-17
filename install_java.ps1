$ErrorActionPreference = "Continue"

# Download OpenJDK from Tencent Cloud (Adoptium mirror)
$JDK_URL = "https://mirrors.cloud.tencent.com/Adoptium/21/jre/x64/windows/Temurin21JRE-jre_x64_windows_hotspot_21.0.2_13.zip"
$JDK_ZIP = "$env:TEMP\OpenJDK21.zip"
$JDK_EXTRACT = "$env:TEMP\OpenJDK21"

Write-Host "Downloading OpenJDK 21 from Tencent Cloud..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
try {
    Invoke-WebRequest -Uri $JDK_URL -OutFile $JDK_ZIP -UseBasicParsing -TimeoutSec 300
    Write-Host "Download complete!"
} catch {
    Write-Host "Download failed: $_"
    exit 1
}

$fileSize = (Get-Item $JDK_ZIP).Length
Write-Host "Downloaded file size: $([math]::Round($fileSize/1MB, 2)) MB"

if ($fileSize -lt 1000000) {
    Write-Host "File too small"
    exit 1
}

Write-Host "Extracting OpenJDK..."
if (Test-Path $JDK_EXTRACT) { Remove-Item $JDK_EXTRACT -Recurse -Force }
New-Item -ItemType Directory -Path $JDK_EXTRACT -Force | Out-Null
Expand-Archive -Path $JDK_ZIP -DestinationPath $JDK_EXTRACT -Force

# Find the extracted folder
$jreFolder = Get-ChildItem $JDK_EXTRACT -Directory | Select-Object -First 1
if ($jreFolder) {
    $JAVA_HOME = "$env:TEMP\OpenJDK21\$($jreFolder.Name)"
    Write-Host "JAVA_HOME set to: $JAVA_HOME"
    
    # Verify java.exe exists
    $javaExe = "$JAVA_HOME\bin\java.exe"
    if (Test-Path $javaExe) {
        Write-Host "Java found at: $javaExe"
        & $javaExe -version
    }
    
    # Now install SDK components
    $SDK_DIR = "C:\AndroidSDK"
    Write-Host "Installing SDK components..."
    $env:JAVA_HOME = $JAVA_HOME
    & "$SDK_DIR\cmdline-tools\latest\bin\sdkmanager.bat" --install "platforms;android-34" "build-tools;34.0.0" --sdk_root="$SDK_DIR"
} else {
    Write-Host "Failed to find extracted JDK folder"
    exit 1
}

Write-Host "`nDone!"