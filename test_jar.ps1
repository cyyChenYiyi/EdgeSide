$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$jarPath = "C:\And\platforms\android-34\core-for-system-modules.jar"

Write-Host "Testing core-for-system-modules.jar accessibility..."

# List contents using jar command
& jar tf $jarPath 2>&1 | Select-Object -First 10

Write-Host "`nChecking file size:"
$file = Get-Item $jarPath
Write-Host "Size: $($file.Length) bytes"

Write-Host "`nTrying to read as zip:"
Add-Type -AssemblyName System.IO.Compression.FileSystem
try {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
    Write-Host "ZIP entries: $($zip.Entries.Count)"
    $zip.Dispose()
    Write-Host "JAR is readable!"
} catch {
    Write-Host "ZIP error: $($_.Exception.Message)"
}
