#!/bin/bash

# Azure Data Factory Deployment Script
# Resource Group: rg-dataeng-dev
# Storage Account: adlsdataplatformdev123

set -e

echo "=========================================="
echo "Azure Data Factory Deployment Script"
echo "=========================================="
echo ""

# Configuration
RESOURCE_GROUP="rg-dataeng-dev"
STORAGE_ACCOUNT="adlsdataplatformdev123"
FACTORY_NAME="adf-free-tier-demo"
LOCATION="eastus"

echo "Configuration:"
echo "  Resource Group: $RESOURCE_GROUP"
echo "  Storage Account: $STORAGE_ACCOUNT"
echo "  Factory Name: $FACTORY_NAME"
echo "  Location: $LOCATION"
echo ""

# Get storage account key
echo "Step 1: Getting storage account key..."
export AZURE_STORAGE_KEY=$(az storage account keys list \
  -g "$RESOURCE_GROUP" \
  -n "$STORAGE_ACCOUNT" \
  --query '[0].value' -o tsv)

if [ -z "$AZURE_STORAGE_KEY" ]; then
    echo "ERROR: Failed to get storage account key"
    exit 1
fi

echo "✓ Storage key retrieved"
echo ""

# Verify containers
echo "Step 2: Verifying containers..."
CONTAINERS=$(az storage container list \
  --account-name "$STORAGE_ACCOUNT" \
  --account-key "$AZURE_STORAGE_KEY" \
  --query '[].name' -o tsv)

echo "Containers found:"
echo "$CONTAINERS" | while read container; do
    echo "  ✓ $container"
done

REQUIRED_CONTAINERS=("bronze" "silver" "gold" "scripts")
for required in "${REQUIRED_CONTAINERS[@]}"; do
    if ! echo "$CONTAINERS" | grep -q "^${required}$"; then
        echo "WARNING: Container '$required' not found!"
    fi
done
echo ""

# Update parameters file
echo "Step 3: Updating parameters file..."
# Note: You'll need to manually update parameters-free-tier.json with the storage key
echo "⚠️  IMPORTANT: Update arm-templates/parameters-free-tier.json with:"
echo "   - storageAccountKey: (use the value from \$AZURE_STORAGE_KEY)"
echo "   - dataLakeStorageKey: (same as storageAccountKey)"
echo ""

read -p "Have you updated parameters-free-tier.json with the storage key? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Please update the parameters file and run this script again."
    exit 1
fi
echo ""

# Deploy main template
echo "Step 4: Deploying main template (Data Factory + Linked Services)..."
az deployment group create \
  --resource-group "$RESOURCE_GROUP" \
  --template-file arm-templates/adf-main-template-free-tier.json \
  --parameters @arm-templates/parameters-free-tier.json \
  --output table

if [ $? -eq 0 ]; then
    echo "✓ Main template deployed successfully"
else
    echo "✗ Main template deployment failed"
    exit 1
fi
echo ""

# Deploy datasets
echo "Step 5: Deploying ADLS Gen2 datasets..."
az deployment group create \
  --resource-group "$RESOURCE_GROUP" \
  --template-file arm-templates/datasets/datasets-adls-gen2.json \
  --parameters factoryName="$FACTORY_NAME" \
  --output table

if [ $? -eq 0 ]; then
    echo "✓ Datasets deployed successfully"
else
    echo "✗ Datasets deployment failed"
    exit 1
fi
echo ""

# Deploy medallion pipelines
echo "Step 6: Deploying Medallion Architecture pipelines..."
az deployment group create \
  --resource-group "$RESOURCE_GROUP" \
  --template-file arm-templates/pipelines/pipeline-medallion-architecture.json \
  --parameters factoryName="$FACTORY_NAME" \
  --output table

if [ $? -eq 0 ]; then
    echo "✓ Medallion pipelines deployed successfully"
else
    echo "✗ Medallion pipelines deployment failed"
    exit 1
fi
echo ""

# Deploy employee ETL pipelines
echo "Step 7: Deploying Employee ETL pipelines..."
az deployment group create \
  --resource-group "$RESOURCE_GROUP" \
  --template-file arm-templates/pipelines/pipeline-employee-etl.json \
  --parameters factoryName="$FACTORY_NAME" \
  --output table

if [ $? -eq 0 ]; then
    echo "✓ Employee ETL pipelines deployed successfully"
else
    echo "✗ Employee ETL pipelines deployment failed"
    exit 1
fi
echo ""

# Optional: Deploy free tier pipelines
read -p "Deploy free-tier pipelines? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Step 8: Deploying free-tier pipelines..."
    az deployment group create \
      --resource-group "$RESOURCE_GROUP" \
      --template-file arm-templates/pipelines/pipeline-free-tier-scenarios.json \
      --parameters factoryName="$FACTORY_NAME" \
      --output table

    if [ $? -eq 0 ]; then
        echo "✓ Free-tier pipelines deployed successfully"
    else
        echo "✗ Free-tier pipelines deployment failed"
    fi
    echo ""
fi

# Upload sample data
read -p "Upload sample employee CSV files to bronze container? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Step 9: Uploading sample data..."
    
    if [ -f "arm-templates/sample-data/employees.csv" ]; then
        az storage blob upload \
          --account-name "$STORAGE_ACCOUNT" \
          --container-name bronze \
          --name employees/employees.csv \
          --file arm-templates/sample-data/employees.csv \
          --account-key "$AZURE_STORAGE_KEY" \
          --overwrite
        
        echo "✓ employees.csv uploaded"
    else
        echo "⚠️  employees.csv not found"
    fi
    
    if [ -f "arm-templates/sample-data/employees_dirty.csv" ]; then
        az storage blob upload \
          --account-name "$STORAGE_ACCOUNT" \
          --container-name bronze \
          --name employees/employees_dirty.csv \
          --file arm-templates/sample-data/employees_dirty.csv \
          --account-key "$AZURE_STORAGE_KEY" \
          --overwrite
        
        echo "✓ employees_dirty.csv uploaded"
    else
        echo "⚠️  employees_dirty.csv not found"
    fi
    echo ""
fi

echo "=========================================="
echo "Deployment Complete!"
echo "=========================================="
echo ""
echo "Next Steps:"
echo "1. Go to Azure Portal > Data Factory > $FACTORY_NAME"
echo "2. Test pipelines:"
echo "   - EmployeeETL_CompletePipeline"
echo "   - Medallion_CompleteETL"
echo "3. Monitor costs in Azure Portal"
echo ""
echo "Resource Group: $RESOURCE_GROUP"
echo "Factory Name: $FACTORY_NAME"
echo "Storage Account: $STORAGE_ACCOUNT"
echo ""



