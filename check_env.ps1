$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Java version:"
java -version 2>&1

Write-Host "`nJava home: $env:JAVA_HOME"
Write-Host "Checking java.exe:"
Test-Path "$env:JAVA_HOME\bin\java.exe"

Write-Host "`nChecking android.jar:"
Test-Path "$env:ANDROID_HOME\platforms\android-34\android.jar"

Write-Host "`nEnvironment:"
Get-ChildItem Env: | Where-Object { $_.Name -match "ANDROID|JAVA" }
