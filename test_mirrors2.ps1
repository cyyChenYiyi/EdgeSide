# Test various mirror URLs for cmdline-tools
$mirrors = @(
    'https://mirrors.cloud.tencent.com/android/repository/commandlinetools-win-11076708_latest.zip',
    'https://mirrors.cloud.tencent.com/android/repository/commandlinetools-win_latest.zip',
    'https://mirrors.aliyun.com/android/repository/commandlinetools-win-11076708_latest.zip',
    'https://mirrors.tuna.tsinghua.edu.cn/android/repository/commandlinetools-win-11076708_latest.zip'
)

foreach ($m in $mirrors) {
    try {
        $resp = Invoke-WebRequest -Uri $m -Method Head -TimeoutSec 10 -UseBasicParsing
        if ($resp.StatusCode -eq 200) {
            Write-Host "FOUND: $m"
            Write-Host "Size: $($resp.Headers['Content-Length'])"
        } else {
            Write-Host "NOT FOUND ($($resp.StatusCode)): $m"
        }
    } catch {
        Write-Host "ERROR: $($_.Exception.Message)"
    }
}
