## MnsAutomation (Python)

### Requirements
- Python 3.9+ recommended
- Google Chrome installed (Selenium Manager will fetch the correct ChromeDriver automatically)

### Install (Windows PowerShell)
```powershell
cd C:\Users\rahul\projects\MnsAutomation
py -m venv .venv
.\.venv\Scripts\Activate.ps1
py -m pip install --upgrade pip
pip install -r requirements.txt
```

### Run
```powershell
python .\MnsAutomation.py
```

### Produce CSV and run SAP step (chain)
The automation writes a CSV handoff for `sap_runn.py`.

- PowerShell
```powershell
$csv = "C:\Users\rahul\projects\MnsAutomation\test-results\msnauto_output.csv"
python .\MnsAutomation.py --csv-out "$csv"
python .\sap_runn.py --input "$csv"
```

- cmd
```bat
set CSV=C:\Users\rahul\projects\MnsAutomation\test-results\msnauto_output.csv
python MnsAutomation.py --csv-out "%CSV%"
python sap_runn.py --input "%CSV%"
```

- bash (WSL/Git Bash/macOS/Linux)
```bash
CSV="test-results/msnauto_output.csv"
python MnsAutomation.py --csv-out "$CSV"
python sap_runn.py --input "$CSV"
```

### Install (Windows cmd)
```bat
cd C:\Users\rahul\projects\MnsAutomation
py -m venv .venv
.venv\Scripts\activate.bat
py -m pip install --upgrade pip
pip install -r requirements.txt
```

### Install (bash)
```bash
cd ~/projects/MnsAutomation
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

### Notes
- Reports are written to `test-results/reports` (also `latest-report.html`).
- Screenshots are saved under `test-results/screenshots`.

