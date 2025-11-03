# Azure Data Factory ARM Templates - Free Tier Edition

This version is optimized for **Azure Free Trial ($200 credit)** and focuses on services that are either free or have very low costs.

## ✅ Free Tier Services Included

### Completely Free (Within Limits)
- **Azure Data Factory**: First 500 pipeline runs/month free
- **Azure Blob Storage**: 5GB free for 12 months
- **Azure Key Vault**: Free tier (10,000 operations/month free)
- **Azure Functions**: 1M free requests/month (consumption plan)
- **Integration Runtime (Managed)**: Free (compute cost only when running pipelines)
- **Web Activities**: Free
- **Lookup Activities**: Free
- **Control Flow Activities**: Free (If, ForEach, Switch, Wait, etc.)

### Low Cost Services
- **Azure SQL Database**: 
  - Serverless tier: Auto-pauses when inactive (very low cost)
  - Basic tier: ~$5/month (lowest paid tier)
- **Azure Table Storage**: Pay per use (very cheap for small datasets)
- **Azure Queue Storage**: Pay per use (very cheap)

## ❌ Services NOT Included (Too Expensive for Free Tier)

These services are excluded from the free-tier template:
- Azure Databricks (expensive compute)
- Azure HDInsight (expensive cluster)
- Azure Synapse Analytics (expensive)
- Azure Data Lake Storage Gen2 (costs for storage and operations)
- Azure ML (can be expensive)
- Self-hosted Integration Runtime (requires VM)
- Large Data Flow transformations (expensive compute)
- SAP connectors (require expensive self-hosted IR)

## What's Included

### Linked Services (Free Tier Template)
1. **Azure Blob Storage** - Free tier (5GB for 12 months)
2. **Azure SQL Database** - Use Serverless/Basic tier
3. **Azure Key Vault** - Free tier
4. **Azure Functions** - Free tier (1M requests/month)
5. **Azure Table Storage** - Pay per use (very cheap)
6. **Azure File Storage** - Pay per use (very cheap)
7. **Azure Queue Storage** - Pay per use (very cheap)
8. **HTTP/REST Services** - Free
9. **OData Services** - Free

### Pipeline Scenarios (11 Pipelines)
1. **Copy Blob to SQL** - Basic CSV copy
2. **Web Activity (REST API)** - Call REST APIs for free
3. **Lookup Activity** - Query SQL databases
4. **If Condition** - Conditional branching
5. **ForEach Activity** - Loop through items
6. **Stored Procedure** - Execute SQL stored procedures
7. **Validation Activity** - Validate datasets
8. **Get Metadata** - Retrieve file metadata
9. **Delete Activity** - Delete files from storage
10. **Set Variable** - Work with pipeline variables
11. **Wait Activity** - Add delays
12. **Execute Pipeline** - Chain pipelines together

## Cost Estimation for Free Tier

### Monthly Costs (Estimated)
- **Azure Data Factory**: FREE (first 500 runs/month)
- **Azure Blob Storage**: FREE (5GB for 12 months)
- **Azure SQL Database**: 
  - Serverless: ~$0-5/month (auto-pauses when inactive)
  - Basic: ~$5/month
- **Azure Key Vault**: FREE (10K operations/month)
- **Azure Functions**: FREE (1M requests/month)
- **Other Storage Services**: < $1/month (for small usage)

**Total Estimated Cost: $0-10/month** (well within $200 trial budget)

## Prerequisites

### 0. Install Azure CLI (if not using Cloud Shell)

If you're using Azure CLI locally on your laptop, you need to install it first.

**Windows Installation:**
See [INSTALL-AZURE-CLI.md](./INSTALL-AZURE-CLI.md) for detailed instructions.

**Quick Install (Windows):**
- Download: https://aka.ms/installazurecliwindows
- Or use winget: `winget install -e --id Microsoft.AzureCLI`

After installation:
```powershell
# Login to Azure
az login

# Set subscription
az account set --subscription "Your Subscription Name"

# Verify
az account show
```

**Using Azure Cloud Shell (Online):**
- Go to: https://shell.azure.com/
- Select Bash or PowerShell
- No installation needed - Azure CLI is pre-installed

### 1. Create Free Resources First

#### Azure Data Lake Storage Gen2
Your storage account `adlsdataplatformdev123` is already created in resource group `rg-dataeng-dev`.

**Verified Containers:**
- ✅ bronze
- ✅ silver  
- ✅ gold
- ✅ scripts

**Get Storage Account Key:**
```bash
export AZURE_STORAGE_KEY=$(az storage account keys list \
  -g rg-dataeng-dev \
  -n adlsdataplatformdev123 \
  --query '[0].value' -o tsv)

# Verify containers
az storage container list \
  --account-name adlsdataplatformdev123 \
  --account-key "$AZURE_STORAGE_KEY" \
  -o table
```

#### Azure SQL Database (Serverless - Lowest Cost)
```bash
# Optional: Create SQL Database if you need it
az sql server create \
  --name adf-sql-server-free \
  --resource-group rg-dataeng-dev \
  --location eastus \
  --admin-user sqladmin \
  --admin-password YourSecurePassword123!

az sql db create \
  --resource-group rg-dataeng-dev \
  --server adf-sql-server-free \
  --name adfdatabase \
  --tier Serverless \
  --compute-model Serverless \
  --family Gen5 \
  --capacity 1
```

#### Azure Key Vault (Free Tier)
```bash
# Optional: Create Key Vault if you need it
az keyvault create \
  --name adf-keyvault-free \
  --resource-group rg-dataeng-dev \
  --location eastus \
  --sku standard
```

#### Azure Data Factory (Free)
```bash
# Create ADF if it doesn't exist
az datafactory create \
  --name adf-free-tier-demo \
  --resource-group rg-dataeng-dev \
  --location eastus
```

### 2. Get Storage Account Key
```bash
az storage account keys list \
  --account-name adfstoragefree001 \
  --resource-group your-rg \
  --query "[0].value" -o tsv
```

## Deployment Instructions

### Step 1: Update Parameters File
Edit `parameters-free-tier.json` and fill in:
- `storageAccountKey` - Get from Azure Portal or CLI
- `sqlServerAdminPassword` - Your secure password

### Step 2: Get Storage Account Key

**For PowerShell (Windows):**
```powershell
# Set storage key as environment variable
$env:AZURE_STORAGE_KEY = (az storage account keys list `
  -g rg-dataeng-dev `
  -n adlsdataplatformdev123 `
  --query '[0].value' -o tsv)

# Verify containers exist
az storage container list `
  --account-name adlsdataplatformdev123 `
  --account-key $env:AZURE_STORAGE_KEY `
  -o table
```

**For Bash (Linux/Mac/Cloud Shell):**
```bash
# Set storage key as environment variable
export AZURE_STORAGE_KEY=$(az storage account keys list \
  -g rg-dataeng-dev \
  -n adlsdataplatformdev123 \
  --query '[0].value' -o tsv)

# Verify containers exist
az storage container list \
  --account-name adlsdataplatformdev123 \
  --account-key "$AZURE_STORAGE_KEY" \
  -o table
```

### Step 3: Update Parameters File
Edit `parameters-free-tier.json` and set:
- `storageAccountKey`: Copy the value from `$AZURE_STORAGE_KEY` or Azure Portal
- `dataLakeStorageKey`: Same value as `storageAccountKey`
- Verify `resourceGroupName`: `rg-dataeng-dev`
- Verify `location`: `eastus`

### Step 4: Deploy Main Template
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/adf-main-template-free-tier.json \
  --parameters @arm-templates/parameters-free-tier.json
```

### Step 5: Deploy ADLS Gen2 Datasets
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/datasets/datasets-adls-gen2.json \
  --parameters factoryName=adf-free-tier-demo
```

### Step 6: Deploy Medallion Architecture Pipelines
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/pipelines/pipeline-medallion-architecture.json \
  --parameters factoryName=adf-free-tier-demo
```

### Step 7: Deploy Employee ETL Pipelines (Optional)
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/pipelines/pipeline-employee-etl.json \
  --parameters factoryName=adf-free-tier-demo
```

### Step 8: Deploy Free Tier Pipelines (Optional)
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/pipelines/pipeline-free-tier-scenarios.json \
  --parameters factoryName=adf-free-tier-demo
```

## Cost Optimization Tips

1. **Use Serverless SQL Database**: Auto-pauses when inactive
2. **Limit Pipeline Runs**: Stay within 500 free runs/month
3. **Use Small Data Sets**: Keep under 5GB to use free storage
4. **Avoid Data Flows**: They require expensive compute
5. **Use Basic Activities**: Copy, Lookup, Web activities are cheaper
6. **Monitor Costs**: Set up cost alerts in Azure Portal
7. **Delete Test Resources**: Clean up when done testing

## Free Tier Limitations

- **Data Factory**: 500 pipeline runs/month free
- **Blob Storage**: 5GB free for 12 months
- **SQL Database Serverless**: Auto-pauses after 1 hour of inactivity
- **Key Vault**: 10,000 operations/month free
- **Functions**: 1M requests/month free

## Testing the Pipelines

### 1. Test Copy Pipeline
```bash
# Create a test CSV file in blob storage first
# Then trigger the pipeline: FreeTier_CopyBlobToSql
```

### 2. Test Web Activity
```bash
# Trigger: FreeTier_WebActivityRestApi
# Uses public API (GitHub API) - completely free
```

### 3. Test Lookup
```bash
# Trigger: FreeTier_LookupActivity
# Queries system tables - no cost
```

## Monitoring Costs

1. Go to Azure Portal > Cost Management
2. Set up Budget Alert for $10/month
3. Review Cost Analysis daily
4. Use Cost Management + Billing dashboard

## Sample Workflow (Cost: ~$0-2/month)

1. **Daily ETL** (within 500 free runs/month): FREE
2. **Store data in Blob** (under 5GB): FREE
3. **Query SQL Database Serverless**: ~$0-2/month (only when active)
4. **Use Key Vault for secrets**: FREE (within 10K operations)

## Medallion Architecture with Your Containers

Your storage account `adlsdataplatformdev123` follows the **Medallion Architecture** pattern:

- **Bronze Container**: Raw, unprocessed data (landing zone)
- **Silver Container**: Cleaned, validated, and transformed data
- **Gold Container**: Business-ready, aggregated, and enriched data
- **Scripts Container**: Configuration files, scripts, and metadata

### Deploy Medallion Architecture Pipelines

1. **Deploy ADLS Gen2 Datasets** (required first):
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/datasets/datasets-adls-gen2.json \
  --parameters factoryName=adf-free-tier-demo
```

2. **Deploy Medallion Pipelines**:
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/pipelines/pipeline-medallion-architecture.json \
  --parameters factoryName=adf-free-tier-demo
```

### Available Medallion Pipelines

- **Medallion_BronzeIngestion**: Ingest raw CSV files into bronze container
- **Medallion_SilverTransformation**: Transform bronze CSV → silver Parquet (cleaned)
- **Medallion_GoldAggregation**: Aggregate silver → gold (business-ready)
- **Medallion_CompleteETL**: End-to-end pipeline (Bronze → Silver → Gold)

### Example: Running Medallion Pipeline

```bash
# Trigger the complete ETL pipeline
# Parameters:
# - sourceContainer: "raw-data" (or wherever your source files are)
# - sourceFolder: "input"
# - sourceFile: "customers.csv"
# - tableName: "customers"
```

## Quick Deployment Options

### Option 1: PowerShell Script (Windows - Recommended)

If you installed Azure CLI locally on Windows:

```powershell
# Navigate to project directory
cd C:\Users\rahul\projects\MnsAutomation

# Run deployment script
.\arm-templates\deploy.ps1
```

The script will:
- Check Azure CLI installation
- Login if needed
- Get storage account key
- Verify containers
- Deploy all templates
- Optionally upload sample data

### Option 2: Manual Deployment (Any Platform)

Follow the step-by-step instructions above, using the appropriate commands for your platform:
- **PowerShell (Windows)**: Use backticks `` ` `` for line continuation
- **Bash (Linux/Mac/Cloud Shell)**: Use backslash `\` for line continuation

### Option 3: Azure Cloud Shell (Online)

If using Azure Cloud Shell at https://shell.azure.com/:

1. Upload your ARM template files to Cloud Shell
2. Use Bash commands from the README
3. No local installation needed

## Next Steps

1. **Install Azure CLI** (if using locally) - See [INSTALL-AZURE-CLI.md](./INSTALL-AZURE-CLI.md)
2. **Login to Azure**: `az login`
3. **Get storage account key** (see Step 2 above)
4. **Update `parameters-free-tier.json`** with storage key
5. **Deploy templates** (use PowerShell script or manual commands)
6. **Test pipelines** in Azure Portal
7. **Monitor costs** to stay within $200 trial

## Troubleshooting

### "Storage account not found"
- Make sure storage account is created first
- Check the storage account name matches exactly

### "SQL Database connection failed"
- Verify SQL Server firewall allows Azure services
- Check username/password are correct

### "Pipeline run exceeded timeout"
- Reduce data size for testing
- Use smaller datasets

## Support

- Azure Free Account: https://azure.microsoft.com/free/
- ADF Pricing: https://azure.microsoft.com/pricing/details/data-factory/
- Cost Calculator: https://azure.microsoft.com/pricing/calculator/

---

**Remember**: Always monitor your costs! Set up budget alerts to stay within your $200 trial credit.

