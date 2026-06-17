$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$jarPath = "C:\AndroidSDK\platforms\android-34\core-for-system-modules.jar"

Write-Host "Analyzing core-for-system-modules.jar..."

# Check if it's a valid JAR
& jar tf $jarPath 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "JAR is valid"
    $count = (& jar tf $jarPath 2>&1 | Measure-Object).Count
    Write-Host "Total entries: $count"
} else {
    Write-Host "JAR is INVALID"
}

# Check file properties
$file = Get-Item $jarPath
Write-Host "File size: $($file.Length) bytes"
Write-Host "Created: $($file.CreationTime)"
Write-Host "Modified: $($file.LastWriteTime)"

# Try to see if there are any issues with the JAR format
Write-Host "`nTrying to read manifest..."
& jar xf $jarPath META-INF\MANIFEST.MF 2>&1
if (Test-Path "META-INF\MANIFEST.MF") {
    Get-Content "META-INF\MANIFEST.MF"
    Remove-Item "META-INF" -Recurse -Force
} else {
    Write-Host "Could not extract manifest"
}

# Check if android.jar exists and is readable
$androidJar = "C:\AndroidSDK\platforms\android-34\android.jar"
Write-Host "`nChecking android.jar..."
$file = Get-Item $androidJar
Write-Host "android.jar size: $($file.Length) bytes"
