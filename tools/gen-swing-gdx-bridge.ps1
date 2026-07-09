param(
    [Parameter(Mandatory = $true)][string]$CoreRoot,
    [Parameter(Mandatory = $true)][string]$OutFile
)

$bridgeDir = Join-Path $CoreRoot 'main\java\com\witcher\gdx\bridge'
$graphicsDir = Join-Path $CoreRoot 'main\java\com\witcher\gdx\graphics'

$paths = @()
if (Test-Path -LiteralPath $bridgeDir) {
    $paths += Get-ChildItem -LiteralPath $bridgeDir -Filter '*.java' -File |
        Sort-Object Name |
        ForEach-Object { $_.FullName }
}

$graphicsFiles = @(
    'PixelTextures.java',
    'RenderQuality.java',
    'GdxTextureBridge.java'
)
foreach ($name in $graphicsFiles) {
    $file = Join-Path $graphicsDir $name
    if (Test-Path -LiteralPath $file) {
        $paths += $file
    }
}

if ($paths.Count -eq 0) {
    Write-Error "No GDX bridge sources found under $CoreRoot"
    exit 1
}

$parent = Split-Path -Parent $OutFile
if ($parent -and -not (Test-Path -LiteralPath $parent)) {
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
}

[System.IO.File]::WriteAllLines($OutFile, $paths)
Write-Host "GDX bridge sources: $($paths.Count) -> $OutFile"
