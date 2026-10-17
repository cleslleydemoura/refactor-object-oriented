param([Parameter(ValueFromRemainingArguments = $true)][string[]]$MavenArgs)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $mavenVersion = '3.9.9'
    $mavenDirectory = Join-Path $PSScriptRoot ".tools/apache-maven-$mavenVersion"
    $mavenExecutable = Join-Path $mavenDirectory 'bin/mvn.cmd'
    $existingMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($existingMaven) { $mavenExecutable = $existingMaven.Source }
    elseif (-not (Test-Path -LiteralPath $mavenExecutable)) {
        $toolsDirectory = Join-Path $PSScriptRoot '.tools'
        New-Item -ItemType Directory -Path $toolsDirectory -Force | Out-Null
        $mavenArchive = Join-Path $toolsDirectory "apache-maven-$mavenVersion-bin.zip"
        $downloadUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
        Write-Host "Baixando Maven $mavenVersion para a pasta local .tools..."
        Invoke-WebRequest -Uri $downloadUrl -OutFile $mavenArchive
        $checksum = ((Invoke-WebRequest -Uri "$downloadUrl.sha512").Content.Trim() -split '\s+')[0]
        if ((Get-FileHash -LiteralPath $mavenArchive -Algorithm SHA512).Hash.ToLowerInvariant() -ne $checksum.ToLowerInvariant()) {
            throw 'O SHA-512 do Maven não confere. O arquivo não será executado.'
        }
        Expand-Archive -LiteralPath $mavenArchive -DestinationPath $toolsDirectory -Force
    }
    & $mavenExecutable "-Dmaven.repo.local=$PSScriptRoot/.m2" @MavenArgs
    exit $LASTEXITCODE
} finally { Pop-Location }
