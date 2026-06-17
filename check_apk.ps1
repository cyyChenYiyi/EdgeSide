$apkPath = "C:\Users\cloud_user\Desktop\edgeside\app\build\outputs\apk\debug"
if (Test-Path $apkPath) {
    Get-ChildItem $apkPath
} else {
    Write-Host "APK directory not found: $apkPath"
    # Check parent directories
    $outputsPath = "C:\Users\cloud_user\Desktop\edgeside\app\build\outputs"
    if (Test-Path $outputsPath) {
        Write-Host "Contents of outputs:"
        Get-ChildItem $outputsPath -Recurse
    } else {
        Write-Host "build\outputs not found"
    }
}
