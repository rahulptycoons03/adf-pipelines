# Sample Data Files

This directory contains sample CSV files for testing your ADF pipelines.

## Files

### employees.csv
Clean employee data with 27 records including:
- Employee IDs (1001-1020, 2001-2007)
- Employee details (name, email, department, position, salary)
- Manager relationships
- Join dates
- Status (Active/Inactive)

**Columns:**
- EmployeeID
- FirstName
- LastName
- Email
- Department
- Position
- Salary
- JoinDate
- ManagerID
- Status

**Use Case:** Use this for testing clean data ingestion and transformations.

### employees_dirty.csv
Employee data with data quality issues:
- Missing EmployeeID (row 1011)
- Missing Salary (row 1011)
- Missing JoinDate (row 1012)
- Additional Notes column with error descriptions

**Use Case:** Use this for testing data validation, cleansing, and error handling pipelines.

## How to Use

### Option 1: Upload to Azure Storage Account
1. Upload the CSV files to your storage account container (e.g., `raw-data` container)
2. Place in a folder like `employees/`
3. Use the file path in your pipeline parameters

### Option 2: Upload to Azure Data Lake Storage Gen2 (adlsdataplatformdev123)
```bash
# Get storage key from resource group rg-dataeng-dev
export AZURE_STORAGE_KEY=$(az storage account keys list \
  -g rg-dataeng-dev \
  -n adlsdataplatformdev123 \
  --query '[0].value' -o tsv)

# Upload to bronze container for testing
az storage blob upload \
  --account-name adlsdataplatformdev123 \
  --container-name bronze \
  --name employees/employees.csv \
  --file arm-templates/sample-data/employees.csv \
  --account-key "$AZURE_STORAGE_KEY"

# Upload dirty data for testing data quality pipelines
az storage blob upload \
  --account-name adlsdataplatformdev123 \
  --container-name bronze \
  --name employees/employees_dirty.csv \
  --file arm-templates/sample-data/employees_dirty.csv \
  --account-key "$AZURE_STORAGE_KEY"
```

### Option 3: Upload via Azure Portal
1. Go to Azure Portal
2. Navigate to Storage Account `adlsdataplatformdev123`
3. Select a container (e.g., `bronze` or create `raw-data`)
4. Click "Upload"
5. Select the CSV file
6. Set path as `employees/employees.csv`

## Pipeline Testing

### Test Employee ETL Pipeline

1. **Deploy Employee ETL Pipeline:**
```bash
az deployment group create \
  --resource-group rg-dataeng-dev \
  --template-file arm-templates/pipelines/pipeline-employee-etl.json \
  --parameters factoryName=adf-free-tier-demo
```

2. **Run EmployeeETL_CompletePipeline with parameters:**
   - `sourceContainer`: "bronze" (or where you uploaded the file)
   - `sourceFolder`: "employees"
   - `sourceFileName`: "employees.csv"

3. **Expected Results:**
   - **Bronze**: `bronze/employees/YYYY/MM/DD/employees_[runId].csv`
   - **Silver**: `silver/employees/YYYY/MM/DD/employees_cleaned_[runId].parquet`
   - **Gold**: `gold/employees/YYYY/MM/DD/employees_aggregated_[runId].parquet`

## Data Schema

### Employees Table Structure
```
- EmployeeID (INT): Unique employee identifier
- FirstName (STRING): Employee first name
- LastName (STRING): Employee last name
- Email (STRING): Employee email address
- Department (STRING): Department name (Engineering, Marketing, Sales, HR, Finance, Operations, IT)
- Position (STRING): Job title/position
- Salary (DECIMAL): Annual salary
- JoinDate (DATE): Date employee joined company (YYYY-MM-DD)
- ManagerID (INT): Manager's EmployeeID (NULL for directors/managers)
- Status (STRING): Employment status (Active/Inactive)
```

## Departments
- Engineering
- Marketing
- Sales
- HR
- Finance
- Operations
- IT

## Sample Queries for Testing

### After loading to SQL Database:
```sql
-- Count employees by department
SELECT Department, COUNT(*) as EmployeeCount
FROM Employees
GROUP BY Department;

-- Average salary by department
SELECT Department, AVG(Salary) as AvgSalary
FROM Employees
WHERE Status = 'Active'
GROUP BY Department;

-- Employee hierarchy (employees and their managers)
SELECT 
    e.EmployeeID,
    e.FirstName + ' ' + e.LastName as EmployeeName,
    e.Position,
    m.FirstName + ' ' + m.LastName as ManagerName
FROM Employees e
LEFT JOIN Employees m ON e.ManagerID = m.EmployeeID;
```

## Notes
- All dates are in YYYY-MM-DD format
- Salaries are annual amounts
- ManagerID is NULL for top-level executives
- Use employees.csv for clean data testing
- Use employees_dirty.csv for data quality testing

