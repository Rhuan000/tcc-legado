$dir = ".\monolito-biblioteca"
$output = Join-Path $dir "projeto.md"

$ignoreDirs = @(
    "target",
    ".git",
    ".idea",
    ".settings",
    ".vscode",
    "node_modules",
    "bin",
    "build",
    "dist",
    "logs"
)

$includeExtensions = @(
    ".java",".jsp",".jspx",".xml",".properties",
    ".json",".yml",".yaml",".js",".ts",".css",
    ".html",".sql",".md",".txt",".gradle",".groovy"
)

"# Projeto" | Set-Content $output -Encoding UTF8
"" | Add-Content $output
"## Estrutura" | Add-Content $output
"" | Add-Content $output

cmd /c "tree `"$dir`" /F" | Add-Content $output

"" | Add-Content $output
"## Arquivos" | Add-Content $output

Get-ChildItem $dir -Recurse -File | Where-Object {

    foreach($folder in $ignoreDirs){
        if($_.FullName -like "*\$folder\*"){
            return $false
        }
    }

    $includeExtensions -contains $_.Extension.ToLower()

} | Sort-Object FullName | ForEach-Object {

    Add-Content $output ""
    Add-Content $output "===================================================="
    Add-Content $output "Arquivo: $($_.FullName)"
    Add-Content $output "===================================================="
    Add-Content $output ""

    try{
        Get-Content $_.FullName | Add-Content $output
    }
    catch{
        Add-Content $output "[ERRO AO LER O ARQUIVO]"
    }

    Add-Content $output ""
}

Write-Host ""
Write-Host "Gerado:"
Write-Host $output
