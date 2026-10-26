<#
.SYNOPSIS
  Read-only OpenVista endurance sampling for the owner's physical Fire TV.
.DESCRIPTION
  Collects only numeric/process state metrics. No raw logcat, media URLs,
  playlist names, locations or app-private data are written to the report.
  Does not wake the TV, change settings, start playback or install any APK.
  Leave OpenVista playing and the Windows PC awake throughout the test.
.EXAMPLE
  .\scripts\hisense-soak-monitor.ps1 -Hours 24
.EXAMPLE
  .\scripts\hisense-soak-monitor.ps1 -Hours 1 -IntervalSeconds 5 -MaxSamples 2
#>
[CmdletBinding()]
param(
    [ValidateRange(1,72)][int]$Hours = 24,
    [ValidateRange(5,300)][int]$IntervalSeconds = 60,
    [ValidateRange(0,30000)][int]$MaxSamples = 0,
    [string]$AdbPath = 'adb',
    [string]$OutputDirectory = ''
)
$ErrorActionPreference = 'Stop'
$package = 'com.openvista.prototype'
$adb = (Get-Command $AdbPath -ErrorAction Stop).Source
$connected = @(& $adb devices -l | Where-Object { $_ -match '\sdevice\s' })
if ($connected.Count -ne 1 -or $connected[0] -notmatch 'model:AFTHA001') {
    throw 'Expected exactly one connected Hisense AFTHA001; no sampling started.'
}
$model = (& $adb shell getprop ro.product.model | Select-Object -First 1).Trim()
$api = (& $adb shell getprop ro.build.version.sdk | Select-Object -First 1).Trim()
if ($model -ne 'AFTHA001' -or $api -ne '28') { throw 'Unexpected device or API level.' }
$packages = @(& $adb shell pm list packages com.openvista)
if ($packages.Count -ne 1 -or $packages[0].Trim() -ne "package:$package") {
    throw 'One OpenVista app package was not confirmed.'
}
if (-not $OutputDirectory) {
    $OutputDirectory = Join-Path $env:LOCALAPPDATA ('OpenVista\QA\soak-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
}
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$csv = Join-Path $OutputDirectory 'samples.csv'
$started = Get-Date
$until = $started.AddHours($Hours)
$observed = 0
$hadMissingApp = $false
$hadEnabledScreensaver = $false
$firstPid = ''
do {
    $now = Get-Date
    $pidText = (& $adb shell pidof $package 2>$null | Out-String).Trim()
    $pidText = ($pidText -split '\s+')[0]
    if ($pidText -notmatch '^\d+$') { $pidText = '' }
    if ($pidText -eq '') { $hadMissingApp = $true }
    if (-not $firstPid -and $pidText) { $firstPid = $pidText }
    $powerText = & $adb shell dumpsys power
    $powerMatch = [regex]::Match(($powerText -join ' '), 'mWakefulness=(\w+)')
    $awake = if ($powerMatch.Success) { $powerMatch.Groups[1].Value } else { 'NOT_MEASURABLE' }
    $windowText = & $adb shell dumpsys window windows
    $focused = (($windowText | Where-Object { $_ -match 'mCurrentFocus=' } | Select-Object -First 1) -join '')
    $inFront = $focused -like "*$package/*"
    $screensaver = ((& $adb shell settings get secure screensaver_enabled | Out-String).Trim())
    if ($screensaver -ne '0') { $hadEnabledScreensaver = $true }
    $pss = ''
    $cpuTicks = ''
    if ($pidText) {
        $mem = & $adb shell dumpsys meminfo $package
        $match = [regex]::Match(($mem -join "`n"), '(?m)^\s*TOTAL:\s+(\d+)')
        if ($match.Success) { $pss = $match.Groups[1].Value }
        $stat = (& $adb shell cat "/proc/$pidText/stat" 2>$null | Out-String).Trim()
        $m = [regex]::Match($stat, '^\d+\s+\(.+\)\s+(.*)$')
        if ($m.Success) {
            $parts = @($m.Groups[1].Value -split '\s+')
            if ($parts.Count -gt 12) {
                try { $cpuTicks = ([long]$parts[11] + [long]$parts[12]).ToString() } catch {}
            }
        }
    }
    [pscustomobject]@{
        utc = $now.ToUniversalTime().ToString('o')
        appAlive = [bool]$pidText
        pid = $pidText
        appInFront = $inFront
        wakefulness = $awake
        screensaverDisabled = ($screensaver -eq '0')
        pssKiB = $pss
        processCpuTicks = $cpuTicks
        decodedFrames = 'NOT_MEASURABLE'
        droppedFrames = 'NOT_MEASURABLE'
        thermalC = 'NOT_MEASURABLE'
    } | Export-Csv -NoTypeInformation -Path $csv -Append
    $observed++
    if ($MaxSamples -and $observed -ge $MaxSamples) { break }
    if ((Get-Date) -ge $until) { break }
    Start-Sleep -Seconds $IntervalSeconds
} while ((Get-Date) -lt $until)
$ended = Get-Date
[pscustomobject]@{
    device = 'Hisense AFTHA001'
    api = 28
    startedUtc = $started.ToUniversalTime().ToString('o')
    endedUtc = $ended.ToUniversalTime().ToString('o')
    elapsedSeconds = [math]::Round(($ended-$started).TotalSeconds)
    samples = $observed
    anyAppAbsent = $hadMissingApp
    anyScreensaverEnabled = $hadEnabledScreensaver
    dropFrameMetric = 'NOT_MEASURABLE'
    thermalMetric = 'NOT_MEASURABLE'
    verdict = 'REVIEW_REQUIRED'
} | ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $OutputDirectory 'summary.json')
Write-Output ("Sampling finished: $observed records. Review $OutputDirectory")
Write-Output 'This is evidence collection, not automatic 24-hour acceptance.'
