param([ValidateSet('Server','ClientA','ClientB')][string]$Role,[string]$RunRoot,[int]$SoakTicks=1200)
$ErrorActionPreference='Stop'
$repo=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $repo
& "$repo/gradlew.bat" "runMultiplayer$Role" "-PmultiplayerRunRoot=$RunRoot" "-PsoakTicks=$SoakTicks" --console=plain
exit $LASTEXITCODE
