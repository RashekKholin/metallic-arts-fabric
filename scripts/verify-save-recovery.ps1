param([switch]$AcceptMinecraftEula)
$ErrorActionPreference='Stop'
if(!$AcceptMinecraftEula){throw 'Read https://aka.ms/MinecraftEULA and explicitly accept before running server fixtures.'}
$repo=Split-Path $PSScriptRoot -Parent
$base=Join-Path $repo ('build/run/recovery-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
$wrapper=if($IsWindows -or $env:OS -eq 'Windows_NT'){Join-Path $repo 'gradlew.bat'}else{Join-Path $repo 'gradlew'}
foreach($scenario in @('orderly','crash')){
 $fixture=Join-Path $base $scenario
 New-Item -ItemType Directory -Path $fixture | Out-Null
 Set-Content (Join-Path $fixture 'eula.txt') 'eula=true'
 Set-Content (Join-Path $fixture 'server.properties') @('server-ip=127.0.0.1','server-port=25569','online-mode=false','level-name=world','level-type=minecraft:flat','view-distance=2','simulation-distance=2','difficulty=peaceful')
 $relativeFixture=[IO.Path]::GetRelativePath($repo,$fixture).Replace('\','/')
 $modes=if($scenario -eq 'orderly'){@('seed','verify')}else{@('seed','fault','recover')}
 foreach($mode in $modes){
  $log=Join-Path $fixture ($mode+'.log')
  & $wrapper -p $repo runRestartSmoke "-PsmokeMode=$mode" "-PsmokeRunDir=$relativeFixture" -PacceptMinecraftEula=true --console=plain *> $log
  if($mode -eq 'fault'){
   $marker=Join-Path $fixture 'restart-fault.result'
   if($LASTEXITCODE -eq 0 -or !(Test-Path -LiteralPath $marker) -or (Get-Content $marker -Raw) -notmatch 'INTENTIONAL_CRASH_AFTER_PLAYER_SAVE_BEFORE_WORLD_SAVE'){throw "Expected isolated fault point was not reached; see $log"}
  }elseif($LASTEXITCODE -ne 0 -or (Get-Content (Join-Path $fixture ('restart-'+$mode+'.result')) -Raw) -notmatch '^PASS:'){throw "Recovery fixture failed; see $log"}
 }
}
Write-Output "PASS: separate-process save/reload and intentional partial-save crash recovery. Evidence: $base"
