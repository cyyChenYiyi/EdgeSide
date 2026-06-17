# Check for symlinks/junction points
$paths = @(
    "C:\AndroidSDK",
    "C:\android-sdk",
    "C:\GradleHome",
    "C:\Users\cloud_user\Desktop\edgeside"
)

foreach ($p in $paths) {
    Write-Host "Checking: $p"
    $item = Get-Item $p -ErrorAction SilentlyContinue
    if ($item) {
        Write-Host "  Mode: $($item.Mode)"
        Write-Host "  Target: $($item.Target)"
        Write-Host "  LinkType: $($item.LinkType)"
    } else {
        Write-Host "  Not found or no access"
    }
    Write-Host ""
}
