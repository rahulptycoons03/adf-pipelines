# Azure Data Factory Deployment Script for Windows PowerShell
# Resource Group: rg-dataeng-dev
# Storage Account: adlsdataplatformdev123

param(
    [string]$ResourceGroup = "rg-dataeng-dev",
    [string]$StorageAccount = "adlsdataplatformdev123",
    [string]$FactoryName = "adf-free-tier-demo",
    [string]$Location = "eastus"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Azure Data Factory Deployment Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  Resource Group: $ResourceGroup"
Write-Host "  Storage Account: $StorageAccount"
Write-Host "  Factory Name: $FactoryName"
Write-Host "  Location: $Location"
Write-Host ""

# Check if Azure CLI is installed
try {
    $azVersion = az --version 2>&1 | Select-Object -First 1
    Write-Host "✓ Azure CLI found: $azVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Azure CLI not found. Please install it first." -ForegroundColor Red
    Write-Host "  Download from: https://aka.ms/installazurecliwindows" -ForegroundColor Yellow
    Write-Host "  Or see: INSTALL-AZURE-CLI.md" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Check if logged in
try {
    $account = az account show 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Please login to Azure..." -ForegroundColor Yellow
        az login
    } else {
        Write-Host "✓ Already logged in to Azure" -ForegroundColor Green
    }
} catch {
    Write-Host "Please login to Azure..." -ForegroundColor Yellow
    az login
}
Write-Host ""

# Get storage account key
Write-Host "Step 1: Getting storage account key..." -ForegroundColor Cyan
try {
    $env:AZURE_STORAGE_KEY = az storage account keys list `
        -g $ResourceGroup `
        -n $StorageAccount `
        --query '[0].value' -o tsv
    
    if ([string]::IsNullOrEmpty($env:AZURE_STORAGE_KEY)) {
        throw "Failed to get storage key"
    }
    Write-Host "✓ Storage key retrieved" -ForegroundColor Green
} catch {
    Write-Host "✗ ERROR: Failed to get storage account key" -ForegroundColor Red
    Write-Host "  Make sure resource group and storage account exist" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Verify containers
Write-Host "Step 2: Verifying containers..." -ForegroundColor Cyan
try {
    $containers = az storage container list `
        --account-name $StorageAccount `
        --account-key $env:AZURE_STORAGE_KEY `
        --query '[].name' -o tsv
    
    Write-Host "Containers found:" -ForegroundColor Yellow
    $containers | ForEach-Object { Write-Host "  ✓ $_" -ForegroundColor Green }
    
    $requiredContainers = @("bronze", "silver", "gold", "scripts")
    foreach ($required in $requiredContainers) {
        if ($containers -notcontains $required) {
            Write-Host "  ⚠ WARNING: Container '$required' not found!" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "⚠ WARNING: Could not verify containers" -ForegroundColor Yellow
}
Write-Host ""

# Check parameters file
Write-Host "Step 3: Checking parameters file..." -ForegroundColor Cyan
$paramsFile = "arm-templates\parameters-free-tier.json"
if (-not (Test-Path $paramsFile)) {
    Write-Host "✗ ERROR: Parameters file not found: $paramsFile" -ForegroundColor Red
    exit 1
}

$paramsContent = Get-Content $paramsFile -Raw | ConvertFrom-Json
if ([string]::IsNullOrEmpty($paramsContent.parameters.storageAccountKey.value)) {
    Write-Host "⚠ WARNING: storageAccountKey is empty in parameters file" -ForegroundColor Yellow
    Write-Host "  Current value from CLI will be used" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Would you like to update parameters-free-tier.json with the storage key? (Y/N)" -ForegroundColor Cyan
    $response = Read-Host
    if ($response -eq "Y" -or $response -eq "y") {
        $paramsContent.parameters.storageAccountKey.value = $env:AZURE_STORAGE_KEY
        $paramsContent.parameters.dataLakeStorageKey.value = $env:AZURE_STORAGE_KEY
        $paramsContent | ConvertTo-Json -Depth 10 | Set-Content $paramsFile
        Write-Host "✓ Parameters file updated" -ForegroundColor Green
    }
} else {
    Write-Host "✓ Parameters file has storage key configured" -ForegroundColor Green
}
Write-Host ""

# Deploy main template
Write-Host "Step 4: Deploying main template (Data Factory + Linked Services)..." -ForegroundColor Cyan
try {
    az deployment group create `
        --resource-group $ResourceGroup `
        --template-file "arm-templates\adf-main-template-free-tier.json" `
        --parameters "@$paramsFile" `
        --output table
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Main template deployed successfully" -ForegroundColor Green
    } else {
        throw "Deployment failed"
    }
} catch {
    Write-Host "✗ Main template deployment failed" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Deploy datasets
Write-Host "Step 5: Deploying ADLS Gen2 datasets..." -ForegroundColor Cyan
try {
    az deployment group create `
        --resource-group $ResourceGroup `
        --template-file "arm-templates\datasets\datasets-adls-gen2.json" `
        --parameters factoryName=$FactoryName `
        --output table
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Datasets deployed successfully" -ForegroundColor Green
    } else {
        throw "Deployment failed"
    }
} catch {
    Write-Host "✗ Datasets deployment failed" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Deploy medallion pipelines
Write-Host "Step 6: Deploying Medallion Architecture pipelines..." -ForegroundColor Cyan
try {
    az deployment group create `
        --resource-group $ResourceGroup `
        --template-file "arm-templates\pipelines\pipeline-medallion-architecture.json" `
        --parameters factoryName=$FactoryName `
        --output table
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Medallion pipelines deployed successfully" -ForegroundColor Green
    } else {
        throw "Deployment failed"
    }
} catch {
    Write-Host "✗ Medallion pipelines deployment failed" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Deploy employee ETL pipelines
Write-Host "Step 7: Deploying Employee ETL pipelines..." -ForegroundColor Cyan
try {
    az deployment group create `
        --resource-group $ResourceGroup `
        --template-file "arm-templates\pipelines\pipeline-employee-etl.json" `
        --parameters factoryName=$FactoryName `
        --output table
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Employee ETL pipelines deployed successfully" -ForegroundColor Green
    } else {
        throw "Deployment failed"
    }
} catch {
    Write-Host "✗ Employee ETL pipelines deployment failed" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Optional: Deploy free tier pipelines
Write-Host "Deploy free-tier pipelines? (Y/N)" -ForegroundColor Cyan
$response = Read-Host
if ($response -eq "Y" -or $response -eq "y") {
    Write-Host "Step 8: Deploying free-tier pipelines..." -ForegroundColor Cyan
    try {
        az deployment group create `
            --resource-group $ResourceGroup `
            --template-file "arm-templates\pipelines\pipeline-free-tier-scenarios.json" `
            --parameters factoryName=$FactoryName `
            --output table
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Free-tier pipelines deployed successfully" -ForegroundColor Green
        } else {
            Write-Host "⚠ Free-tier pipelines deployment failed (non-critical)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "⚠ Free-tier pipelines deployment failed (non-critical)" -ForegroundColor Yellow
    }
    Write-Host ""
}

# Upload sample data
Write-Host "Upload sample employee CSV files to bronze container? (Y/N)" -ForegroundColor Cyan
$response = Read-Host
if ($response -eq "Y" -or $response -eq "y") {
    Write-Host "Step 9: Uploading sample data..." -ForegroundColor Cyan
    
    $employeesFile = "arm-templates\sample-data\employees.csv"
    if (Test-Path $employeesFile) {
        az storage blob upload `
            --account-name $StorageAccount `
            --container-name bronze `
            --name "employees/employees.csv" `
            --file $employeesFile `
            --account-key $env:AZURE_STORAGE_KEY `
            --overwrite
        
        Write-Host "✓ employees.csv uploaded" -ForegroundColor Green
    } else {
        Write-Host "⚠ employees.csv not found" -ForegroundColor Yellow
    }
    
    $employeesDirtyFile = "arm-templates\sample-data\employees_dirty.csv"
    if (Test-Path $employeesDirtyFile) {
        az storage blob upload `
            --account-name $StorageAccount `
            --container-name bronze `
            --name "employees/employees_dirty.csv" `
            --file $employeesDirtyFile `
            --account-key $env:AZURE_STORAGE_KEY `
            --overwrite
        
        Write-Host "✓ employees_dirty.csv uploaded" -ForegroundColor Green
    } else {
        Write-Host "⚠ employees_dirty.csv not found" -ForegroundColor Yellow
    }
    Write-Host ""
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Deployment Complete!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Go to Azure Portal > Data Factory > $FactoryName"
Write-Host "2. Test pipelines:"
Write-Host "   - EmployeeETL_CompletePipeline"
Write-Host "   - Medallion_CompleteETL"
Write-Host "3. Monitor costs in Azure Portal"
Write-Host ""
Write-Host "Resource Group: $ResourceGroup" -ForegroundColor Cyan
Write-Host "Factory Name: $FactoryName" -ForegroundColor Cyan
Write-Host "Storage Account: $StorageAccount" -ForegroundColor Cyan
Write-Host ""


