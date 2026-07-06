param(
    [Parameter(Mandatory = $true)][string]$Src,
    [Parameter(Mandatory = $true)][string]$Res,
    [Parameter(Mandatory = $true)][string]$StampClass
)

if (-not (Test-Path -LiteralPath $StampClass)) {
    exit 1
}

$outTime = (Get-Item -LiteralPath $StampClass).LastWriteTime

$srcFiles = Get-ChildItem -LiteralPath $Src -Filter '*.java' -Recurse -File -ErrorAction SilentlyContinue
foreach ($file in $srcFiles) {
    if ($file.LastWriteTime -gt $outTime) {
        exit 1
    }
}

if (Test-Path -LiteralPath $Res) {
    $resFiles = Get-ChildItem -LiteralPath $Res -Recurse -File -ErrorAction SilentlyContinue
    foreach ($file in $resFiles) {
        if ($file.LastWriteTime -gt $outTime) {
            exit 2
        }
    }
}

exit 0
