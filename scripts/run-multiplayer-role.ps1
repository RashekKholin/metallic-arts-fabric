param([ValidateSet('Server','ClientA','ClientB')][string]$Role,[string]$RunRoot)
$ErrorActionPreference='Stop'
$repo=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $repo
& "$repo/gradlew.bat" "runMultiplayer$Role" "-PmultiplayerRunRoot=$RunRoot" --console=plain
exit $LASTEXITCODE
