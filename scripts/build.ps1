# Local Windows entry point. Same Gradle tasks as scripts/build.sh.
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleTasks
)
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..\dw2003-dual-screen")
if (-not $GradleTasks -or $GradleTasks.Count -eq 0) {
    $GradleTasks = @(":app:testDebugUnitTest", ":app:lintDebug", ":app:assembleDebug")
}
& .\gradlew.bat @GradleTasks
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
