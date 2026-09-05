[CmdletBinding()]
param(
    [ValidateSet('edit','test','build-diagnostic','build-release','install','commit','push','external')]
    [string]$Operation = 'edit',
    [string]$BackupBranch
)

$ErrorActionPreference = 'Stop'
$repo = (git rev-parse --show-toplevel 2>$null)
if (-not $repo) { throw 'Git deposu kökünde çalıştırılmalı.' }
Set-Location $repo

$agent = Join-Path $repo 'AGENTS.md'
if (-not (Test-Path -LiteralPath $agent)) { throw 'AGENTS.md bulunamadı; işlem durduruldu.' }

$required = @(
    'Zorunlu çalışma sırası',
    'Kesin yasaklar',
    'Değişiklik öncesi geri dönüş güvencesi',
    'Tamamlanma kanıtı'
)
$agentText = Get-Content -Raw -LiteralPath $agent
foreach ($section in $required) {
    if ($agentText -notmatch [regex]::Escape($section)) {
        throw "AGENTS.md beklenen bölümü içermiyor: $section"
    }
}

$sensitive = git ls-files | Select-String -Pattern '(^|/)(\.env|local\.properties|.*\.(jks|keystore))$'
if ($sensitive) { throw "Hassas dosya Git tarafından izleniyor: $($sensitive -join ', ')" }

if ($Operation -eq 'commit') {
    $cached = git diff --cached --name-only
    if (-not $cached) { throw 'Commit öncesi staged değişiklik bulunamadı.' }
    git diff --cached --check
    $badCached = $cached | Select-String -Pattern '(^|/)(\.env|local\.properties|.*\.(jks|keystore|apk|aab))$'
    if ($badCached) { throw "Staged listede yasaklı dosya var: $($badCached -join ', ')" }
} elseif ($Operation -eq 'push') {
    $unpushed = git diff --name-only origin/main..HEAD
    if (-not $unpushed) { throw 'Push edilecek yeni commit bulunamadı.' }
    $badUnpushed = $unpushed | Select-String -Pattern '(^|/)(\.env|local\.properties|.*\.(jks|keystore|apk|aab))$'
    if ($badUnpushed) { throw "Push edilecek listede yasaklı dosya var: $($badUnpushed -join ', ')" }
}

if ($Operation -in @('build-diagnostic','build-release','install','commit','push','external') -and $BackupBranch) {
    $exists = git show-ref --verify --quiet "refs/heads/$BackupBranch"
    if ($LASTEXITCODE -ne 0) { throw "Geri dönüş dalı bulunamadı: $BackupBranch" }
}

Write-Output "Preflight OK: $Operation"
