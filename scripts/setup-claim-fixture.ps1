param([Parameter(Mandatory=$true)][string]$Destination)
$ErrorActionPreference='Stop'
$destinationPath=[IO.Path]::GetFullPath($Destination)
New-Item -ItemType Directory -Path $destinationPath -Force | Out-Null
# Official Modrinth version IDs, pinned to the actually tested 26.1 dependency set.
foreach($entry in @(@('HDDpeLJz','openpac.jar'),@('RXbL43Lt','configport.jar'))){
 $version=Invoke-RestMethod ('https://api.modrinth.com/v2/version/'+$entry[0])
 $file=$version.files | Where-Object primary | Select-Object -First 1
 if(!$file){throw 'No primary official artifact'}
 $target=Join-Path $destinationPath $entry[1]
 Invoke-WebRequest -Uri $file.url -OutFile $target
 if((Get-FileHash -LiteralPath $target -Algorithm SHA512).Hash.ToLower() -ne $file.hashes.sha512){throw 'Official artifact checksum mismatch'}
 $version | ConvertTo-Json -Depth 10 | Set-Content (Join-Path $destinationPath ($entry[1]+'.metadata.json'))
}
Write-Output "Verified OpenPAC and Forge Config API Port fixture: $destinationPath"
