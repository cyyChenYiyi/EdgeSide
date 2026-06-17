# Copy SDK to shorter path
$srcSdk = "C:\android-sdk"
$destSdk = "C:\And"

if (Test-Path $destSdk) {
    Remove-Item $destSdk -Recurse -Force
}

Write-Host "Copying SDK to $destSdk..."
Copy-Item $srcSdk -Destination $destSdk -Recurse -Force

Write-Host "Done!"
