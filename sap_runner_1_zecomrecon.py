import time
import win32com.client
import subprocess
import sys
from PIL import ImageGrab
import os
import base64
from pathlib import Path
import csv

sys.stdout.reconfigure(encoding='utf-8')
# ==== SAP Logon paths ====
SAP_LOGON_PATH = r"C:\Program Files (x86)\SAP\FrontEnd\SAPgui\saplogon.exe"
subprocess.Popen(SAP_LOGON_PATH)
time.sleep(5)

# ==== SAP Login Credentials ====
SAP_SYSTEM = "03     CRT    S/4 HANA  HCM & IS - RETAIL"
SAP_CLIENT = "100"
SAP_USER = "CRT_TOSCA"
SAP_PASS = "Tosca_CRT@25"
SAP_LANG = "EN"
TRANSACTION_CODE = "zecomrecon"


# Path to your CSV file
csv_file = "MnSOrdersListFromKibo.csv"

# ==== Report Data ====
steps = []
report_dir = "report_assets"
os.makedirs(report_dir, exist_ok=True)

def capture_step(description, input_data=""):
    screenshot_path = os.path.join(report_dir, f"{len(steps)+1}_step.png")
    
    # Optional: wait for the window to be fully ready
    time.sleep(2)
    
    img = ImageGrab.grab()
    img.save(screenshot_path)
    steps.append({
        "description": description,
        "input": input_data,
        "screenshot": screenshot_path
    })

# --- Helper: convert image to base64 (embed inside HTML) ---
def image_to_base64(img_path):
    try:
        with open(img_path, "rb") as f:
            encoded = base64.b64encode(f.read()).decode("utf-8")
            ext = Path(img_path).suffix.lower().lstrip(".")
            return f"data:image/{ext};base64,{encoded}"
    except FileNotFoundError:
        return None

## Open SAPGUI and start automation
print("Starting SAP automation...")

try:
    SapGuiAuto = win32com.client.GetObject("SAPGUI")
    application = SapGuiAuto.GetScriptingEngine
except Exception:
    print("SAP GUI not running. Please open SAP Logon manually.")
    sys.exit(1)

# Open SAP system
print(f"Opening system: {SAP_SYSTEM}")
connection = application.OpenConnection(SAP_SYSTEM, True)
time.sleep(3)
session = connection.Children(0)

# Login if needed
if "SAP Easy Access" not in session.Info.Program:
    session.findById("wnd[0]/usr/txtRSYST-MANDT").text = SAP_CLIENT
    session.findById("wnd[0]/usr/txtRSYST-BNAME").text = SAP_USER
    session.findById("wnd[0]/usr/pwdRSYST-BCODE").text = SAP_PASS
    session.findById("wnd[0]/usr/txtRSYST-LANGU").text = SAP_LANG
    session.findById("wnd[0]").sendVKey(0)
    capture_step("Login to SAP", f"Client={SAP_CLIENT}, User={SAP_USER}")


# Read the CSV file
with open(csv_file, newline='', encoding='utf-8') as f:
    reader = csv.reader(f)
    rows = list(reader)

# Count rows and columns
num_rows = len(rows)
num_cols = len(rows[0]) if num_rows > 0 else 0

print(f"Number of rows: {num_rows}")
print(f"Number of columns: {num_cols}")

# Iterate through values in the first column (skip header if needed)
for i, row in enumerate(rows):
    if i == 0:
        print(f"Header: {row[0]}")  # First column header
        continue
    first_col_value = row[0]

    # Start transaction
    session.StartTransaction(TRANSACTION_CODE)
    capture_step("Start Transaction", TRANSACTION_CODE)

    # Set order id field
    argument = sys.argv[1] if len(sys.argv) > 1 else "12345"
    session.findById("wnd[0]/usr/tabsTABSTRIP_MYTAB/tabpINVOICE/ssub%_SUBSCREEN_MYTAB:ZSD_ECOM_RECONCILIATION:0101/txtS_ORDER-LOW").text = row[0]
    session.findById("wnd[0]/usr/tabsTABSTRIP_MYTAB/tabpINVOICE/ssub%_SUBSCREEN_MYTAB:ZSD_ECOM_RECONCILIATION:0101/ctxtS_SORG-LOW").text = "*"
    session.findById("wnd[0]/usr/tabsTABSTRIP_MYTAB/tabpINVOICE/ssub%_SUBSCREEN_MYTAB:ZSD_ECOM_RECONCILIATION:0101/btn%_S_BILDT_%_APP_%-VALU_PUSH").press()
    time.sleep(3)
    session.findById("wnd[1]/usr/tabsTAB_STRIP/tabpINTL").select()
    time.sleep(3)
    session.findById("wnd[1]/usr/tabsTAB_STRIP/tabpINTL/ssubSCREEN_HEADER:SAPLALDB:3020/tblSAPLALDBINTERVAL/ctxtRSCSEL_255-ILOW_I[1,0]").text = "01.10.2025"
    session.findById("wnd[1]/usr/tabsTAB_STRIP/tabpINTL/ssubSCREEN_HEADER:SAPLALDB:3020/tblSAPLALDBINTERVAL/ctxtRSCSEL_255-IHIGH_I[2,0]").text = "31.10.2025"
    session.findById("wnd[1]/tbar[0]/btn[8]").press()

    # Select Header Report option
    session.findById("wnd[0]/usr/tabsTABSTRIP_MYTAB/tabpINVOICE/ssub%_SUBSCREEN_MYTAB:ZSD_ECOM_RECONCILIATION:0101/radP_R1").setFocus()
    capture_step("Enter Order Number", row[0])

    # Execute (button 8 on toolbar)
    session.findById("wnd[0]/tbar[1]/btn[8]").press()

    # Select cell in the results grid
    session.findById("wnd[0]/usr/cntlGRID1/shellcont/shell").selectedRows = "0"
    session.findById("wnd[0]/tbar[1]/btn[39]").press()
    grid = session.findById("wnd[1]/usr/cntlGRID/shellcont/shell")

    # Get selected rows (returns a string like "10" or "10,11")
    row_count = grid.RowCount
    col_count = grid.ColumnCount

    print(f"Grid has {row_count} rows and {col_count} columns")

    # Get column names
    columns = [grid.ColumnOrder(i) for i in range(col_count)]
    print("Columns:", columns)

    # Loop through all rows and print data
    grid_data = []
    for row in range(row_count):
        row_data = []
        grid_data.append(row_data)
        for col in columns:
            value = grid.GetCellValue(row, col)
            row_data.append(value)
        print(f"Row {row + 1}: {row_data}")

    capture_step("Invoice header details",f"{grid_data}")
    session.findById("wnd[0]/usr/cntlGRID1/shellcont/shell").currentCellColumn = ""

    session.findById("wnd[0]/tbar[0]/btn[15]").press
    session.findById("wnd[0]/tbar[0]/btn[15]").press


# ==== Generate HTML Report ====
html_content = """
<html>
<head>
<title>SAP Automation Report</title>
<style>
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
img { width: 300px; }
</style>
</head>
<body>
<h1>SAP Automation Report</h1>
<table>
<tr><th>Step</th><th>Description</th><th>Input</th><th>Screenshot</th></tr>
"""

for idx, step in enumerate(steps, start=1):
    img_data = image_to_base64(step["screenshot"])
    if img_data:
        img_tag = f'<img src="{img_data}" alt="Step {idx} Screenshot">'
    else:
        img_tag = f"<em>Screenshot not found</em>"

    html_content += (
        f"<tr>"
        f"<td>{idx}</td>"
        f"<td>{step['description']}</td>"
        f"<td>{step['input']}</td>"
        f"<td>{img_tag}</td>"
        f"</tr>"
    )

html_content += "</table></body></html>"

with open("sap_report.html", "w", encoding="utf-8") as f:
    f.write(html_content)

print("HTML report generated: sap_report.html")