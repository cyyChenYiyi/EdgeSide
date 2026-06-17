$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Set-Location "C:\Users\cloud_user\Desktop\edgeside"

Write-Host "Compiling SdkTest.java..."
& javac SdkTest.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Running SdkTest..."
    & java -cp . SdkTest
} else {
    Write-Host "Compilation failed"
}
