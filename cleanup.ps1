# Clean up temporary SDK directories
Write-Host "Cleaning up temporary files to free disk space..."

$pathsToRemove = @(
    "C:\And",
    "C:\AndroidSDK",
    "C:\GradleHome",
    "C:\GradleHome2",
    "C:\GradleHome3",
    "C:\NewAndroidSDK"
)

foreach ($p in $pathsToRemove) {
    if (Test-Path $p) {
        Write-Host "Removing $p..."
        Remove-Item $p -Recurse -Force -ErrorAction SilentlyContinue
    }
}

# Clean temp files
$tempFiles = @(
    "$env:TEMP\cmdline-tools.zip",
    "$env:TEMP\platform-34.zip",
    "$env:TEMP\build-tools-34.zip",
    "$env:TEMP\cmdline-extract*"
)

foreach ($f in $tempFiles) {
    if (Test-Path $f) {
        Write-Host "Removing $f..."
        Remove-Item $f -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "`nChecking disk space after cleanup:"
$drive = Get-PSDrive C
Write-Host "Free: $([math]::Round($drive.Free/1GB, 2)) GB"
