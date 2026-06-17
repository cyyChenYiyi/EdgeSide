$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:ANDROID_HOME = "C:\android-sdk"
$env:ANDROID_SDK_ROOT = "C:\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$sdkmanager = "C:\android-sdk\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "Verifying SDK..."
& $sdkmanager --list 2>&1 | Select-String -Pattern "Installed|platforms|build-tools"

Write-Host "`nChecking platforms folder:"
Get-ChildItem "C:\android-sdk\platforms"

Write-Host "`nChecking build-tools folder:"
Get-ChildItem "C:\android-sdk\build-tools"

Write-Host "`nTesting SDK path access from Java:"
$testScript = @"
import java.io.File;
public class Test {
    public static void main(String[] args) {
        File sdk = new File(System.getenv("ANDROID_HOME"));
        System.out.println("SDK exists: " + sdk.exists());
        System.out.println("SDK is directory: " + sdk.isDirectory());
        File platforms = new File(sdk, "platforms");
        System.out.println("Platforms exists: " + platforms.exists());
        File android34 = new File(platforms, "android-34");
        System.out.println("android-34 exists: " + android34.exists());
        File jar = new File(android34, "android.jar");
        System.out.println("android.jar exists: " + jar.exists());
    }
}
"@
$testScript | Out-File -FilePath "$env:TEMP\Test.java" -Encoding UTF8
& javac "$env:TEMP\Test.java"
& java -cp $env:TEMP Test
