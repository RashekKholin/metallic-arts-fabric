param([switch]$AcceptMinecraftEula)
$ErrorActionPreference='Stop'
if(!$AcceptMinecraftEula){throw 'Read https://aka.ms/MinecraftEULA and use -AcceptMinecraftEula only if you agree.'}
$repo=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $repo
if(Get-NetTCPConnection -LocalPort 25570 -State Listen -ErrorAction SilentlyContinue){throw 'Port25570 is occupied. Stop that server yourself or choose another test environment.'}
# Compile once before three normal JVMs use the same immutable classes/resources.
& "$repo/gradlew.bat" gametestClasses --console=plain
if($LASTEXITCODE){throw 'Test compilation failed'}
$runRoot='build/run/multiplayer-'+(Get-Date -Format 'yyyyMMdd-HHmmss')
$base=Join-Path $repo $runRoot
foreach($name in @('multiplayerServer','multiplayerClientA','multiplayerClientB')){New-Item -ItemType Directory -Path (Join-Path $base $name) -Force | Out-Null}
Set-Content (Join-Path $base 'multiplayerServer/eula.txt') 'eula=true'
Set-Content (Join-Path $base 'multiplayerServer/server.properties') @('server-ip=127.0.0.1','server-port=25570','online-mode=false','level-name=world','view-distance=3','simulation-distance=3','spawn-protection=0','pvp=true','difficulty=peaceful','max-tick-time=120000')
foreach($name in @('multiplayerClientA','multiplayerClientB')){Set-Content (Join-Path $base ($name+'/options.txt')) @('renderDistance:3','simulationDistance:5','maxFps:30','soundCategory_master:0.0','pauseOnLostFocus:false','guiScale:2')}
$processes=@()
try {
 foreach($role in @('Server','ClientA','ClientB')){
  $arguments=@('-NoProfile','-ExecutionPolicy','Bypass','-File',('"'+(Join-Path $PSScriptRoot 'run-multiplayer-role.ps1')+'"'),'-Role',$role,'-RunRoot',$runRoot)
  $processes+=Start-Process powershell.exe -ArgumentList $arguments -WorkingDirectory $repo -WindowStyle Hidden -RedirectStandardOutput (Join-Path $base ($role+'.log')) -RedirectStandardError (Join-Path $base ($role+'.stderr.log')) -PassThru
 }
 $result=Join-Path $base 'multiplayerServer/multiplayer-result.json'
 $deadline=(Get-Date).AddMinutes(6)
 while(!(Test-Path -LiteralPath $result)){
  if((Get-Date) -gt $deadline){throw "Timed out. See $base"}
  if($processes | Where-Object {$_.HasExited -and $_.ExitCode -ne 0}){throw "A test process failed. See $base"}
  Start-Sleep -Seconds 1
 }
 $data=Get-Content -LiteralPath $result -Raw | ConvertFrom-Json
 $data | ConvertTo-Json -Depth 8
 if($data.status -ne 'PASS'){throw "Multiplayer failed. See $result"}
 foreach($process in $processes){if(!$process.WaitForExit(30000)){throw "Test shutdown timed out. See $base"}}
 Write-Output "Loopback verification passed; two ordinary clients, not authenticated accounts. Evidence: $base"
} finally {
 # These PIDs are only the helpers started above; never stop unrelated Minecraft sessions.
 foreach($process in $processes){if(!$process.HasExited){& taskkill.exe /PID $process.Id /T /F | Out-Null}}
}
