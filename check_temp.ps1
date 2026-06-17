Write-Host "TEMP: $env:TEMP"
Write-Host "TMP: $env:TMP"
Write-Host "USERPROFILE: $env:USERPROFILE"
Write-Host "USERNAME: $env:USERNAME"

# Check if TEMP path has non-ASCII characters
$tempPath = $env:TEMP
Write-Host "`nTEMP path analysis:"
Write-Host "Length: $($tempPath.Length)"
Write-Host "Has spaces: $($tempPath -match ' ')"

# Convert to bytes to check for non-ASCII
$bytes = [System.Text.Encoding]::UTF8.GetBytes($tempPath)
$hasNonASCII = $false
foreach ($b in $bytes) {
    if ($b -gt 127) {
        $hasNonASCII = $true
        break
    }
}
Write-Host "Has non-ASCII: $hasNonASCII"
