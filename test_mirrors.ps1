# Test Android SDK Mirror URLs
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$mirrors = @(
    'https://mirrors.tuna.tsinghua.edu.cn/android/repository/platform-34_r03.zip',
    'https://mirrors.aliyun.com/android/repository/platform-34_r03.zip',
    'https://mirrors.opencas.cn/android/repository/platform-34_r03.zip',
    'https://mirrors.neusoft.edu.cn/android/repository/platform-34_r03.zip'
)

foreach ($m in $mirrors) {
    try {
        $resp = Invoke-WebRequest -Uri $m -Method Head -TimeoutSec 10 -UseBasicParsing
        if ($resp.StatusCode -eq 200) {
            Write-Host "SUCCESS: $m"
            $len = $resp.Headers['Content-Length']
            if ($len) { Write-Host "Content-Length: $len" }
        } else {
            Write-Host "FAILED ($($resp.StatusCode)): $m"
        }
    } catch {
        Write-Host "ERROR: $m - $($_.Exception.Message)"
    }
}
