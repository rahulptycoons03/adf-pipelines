# Azure Data Factory ARM Templates

This repository contains comprehensive ARM templates for Azure Data Factory (ADF) pipelines covering all possible scenarios and ADF resources.

## ⚠️ Important: Free Tier Version Available

**For Azure Free Trial ($200 credit) users**, see **[README-FREE-TIER.md](./README-FREE-TIER.md)** for a cost-optimized version that includes only free/low-cost services and activities suitable for testing within a $200 trial budget.

## Full Version

The templates in this repository include ALL ADF resources and activities, including expensive services like Databricks, HDInsight, Synapse, etc. Use these if you have an active Azure subscription with sufficient budget.

## Structure

```
arm-templates/
├── adf-main-template.json          # Main template with factory and linked services
├── datasets/
│   └── datasets-template.json       # Common dataset definitions
├── pipelines/
│   ├── pipeline-01-copy-data-scenarios.json        # Copy activity scenarios
│   ├── pipeline-02-transformation-scenarios.json   # Transformation scenarios
│   ├── pipeline-03-control-flow-scenarios.json     # Control flow activities
│   ├── pipeline-04-integration-scenarios.json      # Integration activities
│   └── pipeline-05-orchestration-scenarios.json    # Orchestration scenarios
├── parameters.json                  # Parameter file template
└── README.md                       # This file
```

## Scenarios Covered

### 1. Copy Data Scenarios (pipeline-01-copy-data-scenarios.json)

- **Blob to SQL Database**: Basic CSV copy from Blob Storage to Azure SQL
- **Data Lake to Data Warehouse**: Copy with PolyBase and staging
- **SQL to Cosmos DB**: Copy with upsert and mapping
- **REST API to Blob**: Extract data from REST API and store as JSON
- **SFTP to Blob**: Copy files from SFTP with date filtering
- **Data Quality Validation**: Copy with pre-validation
- **Parquet to Parquet**: Copy Parquet files between locations
- **Excel to SQL**: Import Excel files to SQL Database
- **Avro to JSON**: Convert Avro format to JSON
- **ORC to ORC**: Copy ORC files
- **Binary Copy**: Copy binary files with compression

### 2. Transformation Scenarios (pipeline-02-transformation-scenarios.json)

- **Mapping Data Flow**: Transform data using mapping data flows
- **Wrangling Data Flow**: Data wrangling with Power Query
- **Stored Procedure**: Execute stored procedures for transformations
- **Databricks Notebook**: Run Databricks notebooks
- **Databricks JAR**: Execute Spark JAR jobs
- **Databricks Python**: Run Python scripts in Databricks
- **HDInsight Spark**: Execute Spark jobs on HDInsight
- **HDInsight Pig**: Run Pig scripts for transformations
- **HDInsight Hive**: Execute Hive queries
- **HDInsight MapReduce**: Run MapReduce jobs
- **HDInsight Streaming**: Process streaming data
- **Azure ML Batch Prediction**: Execute ML batch predictions
- **Azure ML Update Resource**: Update ML models
- **Script Activity**: Execute SQL/Python scripts

### 3. Control Flow Scenarios (pipeline-03-control-flow-scenarios.json)

- **If Condition**: Conditional branching based on expressions
- **ForEach**: Loop through items (sequential and parallel)
- **Switch**: Multi-way branching based on values
- **Until Loop**: Execute until condition is met
- **Execute Pipeline**: Call child pipelines
- **Wait**: Wait for specified duration
- **Filter**: Filter items based on conditions
- **Set Variable**: Set pipeline variables
- **Append Variable**: Append to array variables
- **Fail**: Explicitly fail pipeline with error message

### 4. Integration Scenarios (pipeline-04-integration-scenarios.json)

- **Web Activity**: Call REST APIs with authentication
- **Custom Activity**: Execute custom .NET/PowerShell scripts
- **Execute SSIS Package**: Run SSIS packages
- **Azure Function**: Invoke Azure Functions
- **Lookup**: Query data sources for values
- **Delete Activity**: Delete files from storage
- **Script Activity**: Execute SQL scripts
- **Get Metadata**: Retrieve metadata about datasets
- **Validation**: Validate datasets before processing
- **Webhook**: Trigger external webhooks
- **Synapse Notebook**: Execute Synapse notebooks
- **Synapse Spark Job**: Run Spark jobs in Synapse
- **Synapse SQL Script**: Execute SQL scripts in Synapse

### 5. Orchestration Scenarios (pipeline-05-orchestration-scenarios.json)

- **Complete ETL**: End-to-end ETL pipeline
- **Parallel Processing**: Process multiple files in parallel
- **Error Handling**: Error handling with retries and notifications
- **Chained Pipelines**: Execute pipelines in sequence
- **Conditional Branching**: Route processing based on conditions

## Linked Services Included

The main template includes linked services for:

### Azure Services
- Azure SQL Database
- Azure SQL Data Warehouse
- Azure Blob Storage
- Azure Data Lake Storage Gen1 & Gen2
- Azure Table Storage
- Azure Queue Storage
- Azure File Storage
- Azure Cosmos DB
- Azure Databricks
- Azure Key Vault
- Azure Batch
- Azure Data Explorer
- Azure Functions
- Azure ML & ML Service
- Azure Search
- Azure SQL Managed Instance
- Azure PostgreSQL
- Azure MySQL

### On-Premises & File Systems
- SQL Server
- Oracle
- MySQL
- PostgreSQL
- File Server
- FTP Server
- SFTP Server

### Cloud Services
- Amazon S3
- Amazon Redshift
- Google Cloud Storage
- Google BigQuery

### SaaS Applications
- Salesforce
- Dynamics 365
- Dynamics CRM
- OData Services

### Big Data & Analytics
- SAP ECC, OpenHub, Table, BW
- Teradata
- DB2
- Sybase
- HDFS
- Hive
- Impala
- Presto
- Spark
- MongoDB
- Cassandra
- Couchbase
- Elasticsearch
- HBase
- Phoenix

## Activities Covered

### Data Movement
- Copy (all source/sink combinations)
- Delete

### Data Transformation
- Execute Data Flow
- Execute Wrangling Data Flow
- Stored Procedure
- Databricks Notebook/JAR/Python
- HDInsight (Spark/Pig/Hive/MapReduce/Streaming)
- Azure ML (Batch/Update Resource)
- Script
- Synapse (Notebook/Spark/SQL)

### Control Flow
- If Condition
- Switch
- ForEach
- Until
- Wait
- Filter
- Set Variable
- Append Variable
- Execute Pipeline
- Fail

### Data Integration
- Lookup
- Get Metadata
- Validation
- Web Activity
- Webhook
- Custom Activity
- Azure Function
- Execute SSIS Package

## Deployment Instructions

### Prerequisites
1. Azure Subscription
2. Azure Data Factory instance (or create one)
3. Resource Group
4. All required linked services configured

### Deploy Main Template

```bash
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/adf-main-template.json \
  --parameters @arm-templates/parameters.json
```

### Deploy Datasets Template

```bash
# Deploy common datasets first (required for pipelines)
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/datasets/datasets-template.json \
  --parameters factoryName=your-adf-name
```

### Deploy Individual Pipeline Templates

```bash
# Deploy copy data pipelines
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/pipelines/pipeline-01-copy-data-scenarios.json \
  --parameters factoryName=your-adf-name

# Deploy transformation pipelines
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/pipelines/pipeline-02-transformation-scenarios.json \
  --parameters factoryName=your-adf-name

# Deploy control flow pipelines
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/pipelines/pipeline-03-control-flow-scenarios.json \
  --parameters factoryName=your-adf-name

# Deploy integration pipelines
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/pipelines/pipeline-04-integration-scenarios.json \
  --parameters factoryName=your-adf-name

# Deploy orchestration pipelines
az deployment group create \
  --resource-group your-resource-group \
  --template-file arm-templates/pipelines/pipeline-05-orchestration-scenarios.json \
  --parameters factoryName=your-adf-name
```

## Parameter Configuration

Before deploying, update `parameters.json` with your actual values:

1. **Factory Name**: Your ADF instance name
2. **Storage Accounts**: Names of your storage accounts
3. **SQL Servers**: Connection details for SQL databases
4. **Service Principals**: Authentication credentials
5. **Key Vault**: Name of your Key Vault (secrets stored here)
6. **Integration Runtimes**: Names of your IR instances

## Security Best Practices

1. **Use Key Vault**: All sensitive information (passwords, keys) should be stored in Azure Key Vault
2. **Managed Identity**: Use Managed Identity authentication where possible
3. **Service Principals**: Use Service Principals for service-to-service authentication
4. **Network Security**: Configure network rules and private endpoints
5. **Access Control**: Implement RBAC for ADF resources

## Notes

- Some linked services in the main template reference datasets that need to be created separately
- Some activities reference data flows that need to be created in ADF
- Parameters in pipeline templates may need adjustment based on your environment
- Integration Runtime names should match your actual IR configuration
- Some scenarios require specific Azure services to be provisioned first

## Support

For issues or questions:
- Azure Data Factory Documentation: https://docs.microsoft.com/azure/data-factory/
- ARM Template Reference: https://docs.microsoft.com/azure/templates/

