#Requires -Version 5.1

$ErrorActionPreference = 'Stop'

if ($env:OS -ne 'Windows_NT') {
    throw 'Build the Windows ZIP on Windows.'
}

$appName = 'Shooting2DGame'
[xml]$pom = Get-Content -LiteralPath (Join-Path $PSScriptRoot '..\pom.xml') -Raw
$jarName = "$appName-$($pom.project.version).jar"
$target = Join-Path $PSScriptRoot '..\target'
$jar = Join-Path $target $jarName
if (-not (Test-Path -LiteralPath $jar -PathType Leaf)) {
    throw "Run mvn package before packaging $jarName."
}

if ($env:JAVA_HOME) {
    $jpackage = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
} else {
    $jpackage = (Get-Command jpackage.exe -CommandType Application -ErrorAction SilentlyContinue).Source
}
if (-not $jpackage -or -not (Test-Path -LiteralPath $jpackage -PathType Leaf)) {
    throw 'Set JAVA_HOME to JDK 25 or later, or add its bin directory to PATH.'
}
$jpackageVersion = & $jpackage --version
if ($LASTEXITCODE -ne 0 -or $jpackageVersion -notmatch '^\d+' -or [int]$Matches[0] -lt 25) {
    throw "jpackage must be from JDK 25 or later. Found: $jpackageVersion"
}

$target = (Resolve-Path -LiteralPath $target).ProviderPath
$stage = New-Item -ItemType Directory -Path (Join-Path $target "windows-$([guid]::NewGuid())")
$zipName = "$appName-windows.zip"
$zipPath = Join-Path $target $zipName

try {
    $inputDir = New-Item -ItemType Directory -Path (Join-Path $stage.FullName 'input')
    Copy-Item -LiteralPath $jar -Destination $inputDir.FullName
    $imageDir = Join-Path $stage.FullName 'image'

    $packageArgs = @(
        '--type', 'app-image'
        '--name', $appName
        '--input', $inputDir.FullName
        '--main-jar', $jarName
        '--add-modules', 'java.desktop,java.logging'
        '--dest', $imageDir
    )
    & $jpackage @packageArgs
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage exited with code $LASTEXITCODE."
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $stagedZip = Join-Path $stage.FullName $zipName
    $appDir = Join-Path $imageDir $appName
    [System.IO.Compression.ZipFile]::CreateFromDirectory(
        $appDir, $stagedZip, [System.IO.Compression.CompressionLevel]::Optimal, $true
    )
    if ([System.IO.File]::Exists($zipPath)) {
        [System.IO.File]::Replace($stagedZip, $zipPath, [NullString]::Value)
    } else {
        [System.IO.File]::Move($stagedZip, $zipPath)
    }
    Write-Output $zipPath
} finally {
    $stagePath = (Resolve-Path -LiteralPath $stage.FullName).ProviderPath
    if ((Split-Path -Parent $stagePath) -eq $target) {
        Remove-Item -LiteralPath $stagePath -Recurse -Force
    }
}
