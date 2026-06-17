# Copy SDK to C:\AndroidSDK
$srcSdk = "C:\android-sdk"
$destSdk = "C:\AndroidSDK"

if (Test-Path $destSdk) {
    Remove-Item $destSdk -Recurse -Force
}

Write-Host "Copying SDK to $destSdk..."
Copy-Item $srcSdk -Destination $destSdk -Recurse -Force

Write-Host "Done!"
