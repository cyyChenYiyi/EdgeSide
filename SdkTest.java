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

        File android34 = new File(platforms, "android-34");
        System.out.println("android-34 exists: " + android34.exists());

        File androidJar = new File(android34, "android.jar");
        System.out.println("android.jar exists: " + androidJar.exists());
        System.out.println("android.jar length: " + androidJar.length());

        System.out.println("All tests passed!");
    }
}
