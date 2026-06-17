# Download cmdline-tools using BITS
$ErrorActionPreference = 'Continue'

$cmdlineToolsUrl = 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip'
$dest = "$env:TEMP\cmdline-tools.zip"
$sdkDir = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"

Write-Host "Starting BITS transfer for cmdline-tools..."
Write-Host "URL: $cmdlineToolsUrl"

try {
    Start-BitsTransfer -Source $cmdlineToolsUrl -Destination $dest -Description "Android cmdline-tools" -RetryInterval 60 -RetryTimeout 600
    
    $size = (Get-Item $dest).Length / 1MB
    Write-Host "Downloaded: $([math]::Round($size, 2)) MB"
    
    Write-Host "Extracting..."
    $tempExtract = "$env:TEMP\cmdline-extract"
    if (Test-Path $tempExtract) { Remove-Item $tempExtract -Recurse -Force }
    Expand-Archive -Path $dest -DestinationPath $tempExtract -Force
    
    # The zip extracts to cmdline-tools, need to put it in the right structure
    $extractedPath = Get-ChildItem $tempExtract -Directory | Select-Object -First 1
    if ($extractedPath) {
        $targetPath = "$sdkDir\cmdline-tools\latest"
        if (Test-Path "$sdkDir\cmdline-tools") { Remove-Item "$sdkDir\cmdline-tools" -Recurse -Force }
        New-Item -Path $targetPath -ItemType Directory -Force | Out-Null
        Get-ChildItem $extractedPath.FullName | Copy-Item -Destination $targetPath -Recurse -Force
        Write-Host "Installed to: $targetPath"
    }
    
    Remove-Item $dest -Force
    Remove-Item $tempExtract -Recurse -Force
    
    Write-Host "Done!"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
