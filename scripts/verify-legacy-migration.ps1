param([switch]$AcceptMinecraftEula)
$ErrorActionPreference='Stop'
if(!$AcceptMinecraftEula){throw 'Read https://aka.ms/MinecraftEULA and explicitly accept before running server fixtures.'}
$repo=Split-Path $PSScriptRoot -Parent
$base=Join-Path $repo ('build/legacy-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $base | Out-Null
foreach($entry in @(@('05','79639da'),@('07','993d9f8'))){
 $name=$entry[0];$sha=$entry[1];$project=Join-Path $base ('source-'+$name);$zip=Join-Path $base ($name+'.zip');$fixture=Join-Path $base ('world-'+$name)
 & git -C $repo archive --format=zip -o $zip $sha
 if($LASTEXITCODE){throw 'Historical source archive failed; fetch full repository history'}
 Expand-Archive -LiteralPath $zip -DestinationPath $project
 New-Item -ItemType Directory -Path $fixture | Out-Null
 Set-Content (Join-Path $fixture 'eula.txt') 'eula=true'
 Set-Content (Join-Path $fixture 'server.properties') @('server-ip=127.0.0.1','server-port=25569','online-mode=false','level-name=world','level-type=minecraft:flat','view-distance=2','simulation-distance=2','difficulty=peaceful')
 $oldWrapper=if($IsWindows -or $env:OS -eq 'Windows_NT'){Join-Path $project 'gradlew.bat'}else{Join-Path $project 'gradlew'}
 $newWrapper=if($IsWindows -or $env:OS -eq 'Windows_NT'){Join-Path $repo 'gradlew.bat'}else{Join-Path $repo 'gradlew'}
 if(!($IsWindows -or $env:OS -eq 'Windows_NT')){& chmod +x $oldWrapper}
 $oldRelative=[IO.Path]::GetRelativePath($project,$fixture).Replace('\','/')
 $newRelative=[IO.Path]::GetRelativePath($repo,$fixture).Replace('\','/')
 & $oldWrapper -p $project runRestartSmoke -PsmokeMode=seed "-PsmokeRunDir=$oldRelative" -PacceptMinecraftEula=true --console=plain *> (Join-Path $fixture 'old-seed.log')
 if($LASTEXITCODE){throw "Historical $name seed failed; see $fixture"}
 foreach($pass in 1..2){
  & $newWrapper -p $repo runRestartSmoke "-PsmokeMode=legacy$name" "-PsmokeRunDir=$newRelative" -PacceptMinecraftEula=true --console=plain *> (Join-Path $fixture ('upgrade-'+$pass+'.log'))
  if($LASTEXITCODE -or (Get-Content (Join-Path $fixture ('restart-legacy'+$name+'.result')) -Raw) -notmatch '^PASS:'){throw "Historical $name migration failed; see $fixture"}
 }
}
Write-Output "PASS: actual 0.5/0.7 source-generated saves upgraded and reloaded twice. Evidence: $base"
