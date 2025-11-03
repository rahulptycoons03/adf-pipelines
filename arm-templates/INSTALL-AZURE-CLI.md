# Installing Azure CLI on Windows

This guide will help you install Azure CLI on your Windows laptop so you can deploy ARM templates locally.

## Method 1: MSI Installer (Recommended)

1. **Download Azure CLI:**
   - Visit: https://aka.ms/installazurecliwindows
   - Or download directly: https://aka.ms/installazurecliwindows.msi

2. **Run the Installer:**
   - Double-click the downloaded `.msi` file
   - Follow the installation wizard
   - Select "Add to PATH" during installation (recommended)

3. **Verify Installation:**
   Open PowerShell or Command Prompt and run:
   ```powershell
   az --version
   ```

## Method 2: PowerShell Install (via winget)

If you have Windows 10 (1809+) or Windows 11:

```powershell
winget install -e --id Microsoft.AzureCLI
```

## Method 3: Chocolatey

If you have Chocolatey installed:

```powershell
choco install azure-cli
```

## After Installation

1. **Open a NEW PowerShell or Command Prompt window** (important - to refresh PATH)

2. **Verify Installation:**
   ```powershell
   az --version
   ```

3. **Login to Azure:**
   ```powershell
   az login
   ```
   This will open your browser for authentication.

4. **Set your subscription (if you have multiple):**
   ```powershell
   # List subscriptions
   az account list --output table
   
   # Set active subscription
   az account set --subscription "Your Subscription Name or ID"
   ```

5. **Verify you're logged in:**
   ```powershell
   az account show
   ```

## Quick Setup for Your Project

Once Azure CLI is installed and you're logged in:

```powershell
# Navigate to your project
cd C:\Users\rahul\projects\MnsAutomation

# Get storage account key
$env:AZURE_STORAGE_KEY = (az storage account keys list `
  -g rg-dataeng-dev `
  -n adlsdataplatformdev123 `
  --query '[0].value' -o tsv)

# Verify containers
az storage container list `
  --account-name adlsdataplatformdev123 `
  --account-key $env:AZURE_STORAGE_KEY `
  -o table
```

## Troubleshooting

### "az is not recognized"
- Close and reopen your PowerShell/Command Prompt window
- Or add Azure CLI to PATH manually:
  - Go to: `C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin`
  - Add this to your System PATH environment variable

### Login Issues
```powershell
# Clear cached credentials
az account clear

# Login again
az login
```

### PowerShell Execution Policy
If you get execution policy errors:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## Next Steps

After installing Azure CLI:
1. Login: `az login`
2. Set subscription: `az account set --subscription "Your Subscription"`
3. Update `parameters-free-tier.json` with storage key
4. Deploy templates using the commands in `README-FREE-TIER.md`

## References

- Official Installation Guide: https://docs.microsoft.com/cli/azure/install-azure-cli-windows
- Azure CLI Documentation: https://docs.microsoft.com/cli/azure/


