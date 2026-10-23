# HM Auto-trader local build script. Does not change machine-wide Java settings.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
Write-Host '=== HM Auto-trader :: Fabric 26.2 build ===' -ForegroundColor Cyan

function Find-JDK {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
    foreach ($parent in @('C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', 'C:\Program Files\Microsoft')) {
        if (Test-Path $parent) {
            $candidates += @(Get-ChildItem $parent -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName)
        }
    }
    # Prefer 25 over 26; both can compile --release 25.
    foreach ($version in @(25, 26)) {
        foreach ($dir in $candidates) {
            if (-not (Test-Path (Join-Path $dir 'bin\javac.exe'))) { continue }
            $output = (& (Join-Path $dir 'bin\javac.exe') -version 2>&1 | Out-String).Trim()
            if ($output -match "^javac $version(?:\.|$)") { return $dir }
        }
    }
    return $null
}

$jdk = Find-JDK
if (-not $jdk) {
    throw 'JDK 25 or 26 not found. Install JDK 25 (or set JAVA_HOME) and retry.'
}
$env:JAVA_HOME = $jdk
$env:Path = (Join-Path $jdk 'bin') + ';' + $env:Path
Write-Host "Using JDK: $jdk"

# Reuse existing Gradle 9.5.0 from the user's previous Minecraft builds when possible.
$gradleBat = $null
$cache = Join-Path $env:USERPROFILE '.gradle\wrapper\dists\gradle-9.5.0-bin'
if (Test-Path $cache) {
    $gradleBat = Get-ChildItem $cache -Recurse -Filter gradle.bat -File -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match 'gradle-9\.5\.0\\bin\\gradle\.bat$' } |
        Select-Object -First 1 -ExpandProperty FullName
}

if (-not $gradleBat) {
    $tools = Join-Path $PSScriptRoot '.build-tools'
    $gradleBat = Join-Path $tools 'gradle-9.5.0\bin\gradle.bat'
    if (-not (Test-Path $gradleBat)) {
        New-Item $tools -ItemType Directory -Force | Out-Null
        $zip = Join-Path $tools 'gradle-9.5.0-bin.zip'
        Write-Host 'Gradle 9.5.0 not cached; downloading the official distribution...' -ForegroundColor Yellow
        Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-9.5.0-bin.zip' -OutFile $zip
        Expand-Archive -Path $zip -DestinationPath $tools -Force
        Remove-Item $zip
    }
}

Write-Host "Using Gradle: $gradleBat"
& $gradleBat clean build --no-daemon
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit $LASTEXITCODE). See the error above." }
Write-Host ''
Write-Host 'Build succeeded! Mod JAR:' -ForegroundColor Green
Get-ChildItem (Join-Path $PSScriptRoot 'build\libs') -Filter 'hm-auto-trader-*.jar' |
    Where-Object { $_.Name -notlike '*sources*' } | Select-Object -ExpandProperty FullName
