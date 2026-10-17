param([switch]$SemCompilar)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Instale o JDK 17 ou superior e adicione o Java ao PATH.' }
    if (-not $SemCompilar) {
        & powershell -NoProfile -ExecutionPolicy Bypass -File "$PSScriptRoot/maven.ps1" package '-DskipTests'
        if ($LASTEXITCODE -ne 0) { throw 'A compilação falhou. Consulte as mensagens acima.' }
    }
    if (-not (Test-Path -LiteralPath 'target/catalogo/WEB-INF/web.xml')) { throw 'Compile o projeto antes de usar -SemCompilar.' }
    $runtimeClasspath = 'target/classes' + [IO.Path]::PathSeparator + 'target/dependency/*'
    & java '-Dfile.encoding=UTF-8' -cp $runtimeClasspath br.edu.catalogo.launcher.ServidorLocal
} finally { Pop-Location }
