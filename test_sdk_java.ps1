$env:JAVA_HOME = "C:\jdk17\jdk-17.0.11+9"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

$testCode = @"
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SdkTest {
    public static void main(String[] args) {
        String sdkPath = "C:\\AndroidSDK";
        System.out.println("Testing SDK path: " + sdkPath);

        File sdk = new File(sdkPath);
        System.out.println("SDK exists: " + sdk.exists());
        System.out.println("SDK is directory: " + sdk.isDirectory());
        System.out.println("SDK can read: " + sdk.canRead());

        File platforms = new File(sdk, "platforms");
        System.out.println("Platforms exists: " + platforms.exists());
        System.out.println("Platforms is directory: " + platforms.isDirectory());

        File android34 = new File(platforms, "android-34");
        System.out.println("android-34 exists: " + android34.exists());

        File androidJar = new File(android34, "android.jar");
        System.out.println("android.jar exists: " + androidJar.exists());
        System.out.println("android.jar can read: " + androidJar.canRead());
        System.out.println("android.jar length: " + androidJar.length());

        File coreModules = new File(android34, "core-for-system-modules.jar");
        System.out.println("core-for-system-modules.jar exists: " + coreModules.exists());
        System.out.println("core-for-system-modules.jar can read: " + coreModules.canRead());

        // Test Path conversion
        try {
            Path p = Paths.get(sdkPath);
            System.out.println("Path to URI: " + p.toUri());
            System.out.println("Path to absolute: " + p.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Path error: " + e.getMessage());
        }

        System.out.println("All tests passed!");
    }
}
"@

$testCode | Out-File -FilePath "$env:TEMP\SdkTest.java" -Encoding UTF8
Set-Location $env:TEMP

Write-Host "Compiling SdkTest.java..."
& javac "$env:TEMP\SdkTest.java"

Write-Host "Running SdkTest..."
& java -cp $env:TEMP SdkTest
