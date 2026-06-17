$srcSdk = "C:\Users\cloud_user\AppData\Local\Temp\android-sdk"
$destSdk = "C:\android-sdk"

# Create new SDK directory
New-Item -Path $destSdk -ItemType Directory -Force | Out-Null

Write-Host "Copying SDK to $destSdk..."

# Copy only essential parts (platforms, build-tools, licenses, cmdline-tools)
Copy-Item "$srcSdk\platforms" -Destination "$destSdk\platforms" -Recurse -Force
Copy-Item "$srcSdk\build-tools" -Destination "$destSdk\build-tools" -Recurse -Force
Copy-Item "$srcSdk\licenses" -Destination "$destSdk\licenses" -Recurse -Force
Copy-Item "$srcSdk\cmdline-tools" -Destination "$destSdk\cmdline-tools" -Recurse -Force

Write-Host "SDK copied!"
Get-ChildItem $destSdk -Name
