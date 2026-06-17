$drive = Get-PSDrive C
Write-Host "Drive C:"
Write-Host "Used: $([math]::Round($drive.Used/1GB, 2)) GB"
Write-Host "Free: $([math]::Round($drive.Free/1GB, 2)) GB"
