$buildPath = "C:\Users\cloud_user\Desktop\edgeside\app\build"

if (Test-Path $buildPath) {
    Write-Host "Build directory contents:"
    Get-ChildItem $buildPath -Recurse | Select-Object FullName
} else {
    Write-Host "Build directory not found"
}
