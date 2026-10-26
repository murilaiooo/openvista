<#
.SYNOPSIS
    Fire TV D-pad smoke test for OpenVista only. Fail closed on focus changes.
.DESCRIPTION
    Requires a single pre-installed com.openvista.prototype package.
    Never sends navigation keys to the launcher, screensaver, DocumentsUI,
    or any other foreground application. Does not install or remove apps.
.EXAMPLE
    .\scripts\safe_player_adb_smoke.ps1 -Action Inspect
.EXAMPLE
    .\scripts\safe_player_adb_smoke.ps1 -Action ImportPicker
#>
[CmdletBinding()]
param(
    [ValidateSet('Inspect','ImportPicker','FolderPicker')]
    [string]$Action='Inspect'
)
$ErrorActionPreference='Stop'
$package='com.openvista.prototype'
$activity='com.openvista.prototype/com.openvista.prototype.PlayerPrototypeActivity'

function Run-Adb([string[]]$arguments) {
    $result = & adb @arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "ADB command failed; no input sent"
    }
    return $result
}
function Focus {
    $status = Run-Adb @('shell','dumpsys','window','windows')
    $focusLine = $status | Where-Object { $_ -match 'mCurrentFocus=' } | Select-Object -First 1
    return [string]$focusLine
}
function Require-Focus([string]$phase) {
    $now=Focus
    Write-Output "$phase : $now"
    if (-not $now.Contains($activity)) {
        throw "SAFE_ABORT: foreground is not OpenVista ($phase). No navigation input."
    }
}
function Send-SafeKey([int]$code, [string]$label) {
    Require-Focus "before_$label"
    Run-Adb @('shell','input','keyevent',"$code") | Out-Null
    Start-Sleep -Milliseconds 250
    Require-Focus "after_$label"
}
$connected=Run-Adb @('devices','-l')
$devices=@($connected | Where-Object { $_ -match '\sdevice\s' })
if ($devices.Count -ne 1 -or $devices[0] -notmatch 'model:AFTHA001') {
    throw 'Expected exactly one connected Hisense/AFTHA001 ADB device'
}
$model=(Run-Adb @('shell','getprop','ro.product.model') | Select-Object -First 1).Trim()
if ($model -ne 'AFTHA001') {throw 'ADB device model mismatch'}
$packages=@(Run-Adb @('shell','pm','list','packages') | Where-Object { $_ -match '^package:com\.openvista\.' })
if ($packages.Count -ne 1 -or $packages[0].Trim() -ne 'package:com.openvista.prototype') {
    throw 'One-test-app policy not satisfied. No navigation input.'
}
Write-Output 'Single OpenVista player package and device identity verified.'
if ($Action -eq 'Inspect') {
    Write-Output (Focus)
    exit 0
}

# Relaunch ONLY our own Activity. If an OEM dream remains on top, abort.
Run-Adb @('shell','am','force-stop',$package) | Out-Null
Run-Adb @('shell','am','start','-W','-n',"$package/.PlayerPrototypeActivity") | Out-Null
Start-Sleep -Milliseconds 700
Require-Focus 'fresh_player_launch'
Send-SafeKey 22 'first_tab_right'
if ($Action -eq 'FolderPicker') {
    Send-SafeKey 22 'second_tab_right'
}
Require-Focus 'before_picker_action'
Run-Adb @('shell','input','keyevent','23') | Out-Null
Start-Sleep -Milliseconds 950
$now=Focus
Write-Output "after_picker_action : $now"
if ($now -match 'com\.android\.documentsui/.+PickActivity') {
    Write-Output "PASS: $Action opened system DocumentsUI. No keys sent while it had focus."
    exit 0
}
if ($now.Contains($activity)) {
    throw 'Picker did not launch; no further input'
}
throw 'SAFE_ABORT: unexpected foreground window; no further input'
