$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$cmdlineToolsUrl = 'https://mirrors.cloud.tencent.com/android/repository/commandlinetools-win-11076708_latest.zip'
$dest = "$env:TEMP\cmdline-tools.zip"
$sdkDir = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"

Write-Host "Downloading cmdline-tools from Tencent mirror..."
Invoke-WebRequest -Uri $cmdlineToolsUrl -OutFile $dest -UseBasicParsing -TimeoutSec 120

$size = (Get-Item $dest).Length / 1MB
Write-Host "Downloaded: $([math]::Round($size, 2)) MB"

Write-Host "Extracting..."
$tempExtract = "$env:TEMP\cmdline-extract"
Expand-Archive -Path $dest -DestinationPath $tempExtract -Force

# The zip extracts to cmdline-tools, need to put it in the right structure
$extractedPath = Get-ChildItem $tempExtract -Directory | Select-Object -First 1
if ($extractedPath) {
    $targetPath = "$sdkDir\cmdline-tools\latest"
    New-Item -Path $targetPath -ItemType Directory -Force | Out-Null
    Get-ChildItem $extractedPath.FullName | Copy-Item -Destination $targetPath -Recurse -Force
    Write-Host "Installed to: $targetPath"
}

Remove-Item $dest -Force
Remove-Item $tempExtract -Recurse -Force

Write-Host "Done!"
