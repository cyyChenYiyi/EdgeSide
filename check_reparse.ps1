# Check for reparse points or junctions
$paths = @(
    "C:\AndroidSDK",
    "C:\AndroidSDK\platforms",
    "C:\AndroidSDK\platforms\android-34",
    "C:\GradleHome"
)

foreach ($p in $paths) {
    Write-Host "Checking: $p"
    if (Test-Path $p) {
        $item = Get-Item $p
        $attrs = $item.Attributes
        Write-Host "  Attributes: $attrs"
        if ($attrs -match "ReparsePoint") {
            Write-Host "  IS a reparse point!"
        }
        if ($attrs -match "Directory") {
            Write-Host "  Is directory"
        }
    } else {
        Write-Host "  Not found"
    }
    Write-Host ""
}

# Check actual disk space and filesystem
Write-Host "Checking filesystem:"
Get-PSDrive C | Format-List

# Check if path has any special characters
$testPath = "C:\AndroidSDK"
Write-Host "`nPath analysis:"
Write-Host "Path: $testPath"
Write-Host "Length: $($testPath.Length)"
Write-Host "Chars: $($testPath.ToCharArray() -join ', ')"
