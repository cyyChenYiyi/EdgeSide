$jarPath = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk\platforms\android-34\core-for-system-modules.jar"
if (Test-Path $jarPath) {
    $size = (Get-Item $jarPath).Length
    Write-Host "Found: $jarPath ($size bytes)"
} else {
    Write-Host "NOT FOUND: $jarPath"
}

# Also check platforms directory structure
$platformDir = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk\platforms\android-34"
Write-Host "Platforms directory contents:"
Get-ChildItem $platformDir -Name
