import argparse
import csv
from pathlib import Path
from typing import Dict, Iterator


def read_rows(csv_path: Path) -> Iterator[Dict[str, str]]:
    with csv_path.open("r", newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            yield row


def main() -> None:
    parser = argparse.ArgumentParser(description="sap_runn*: consume msnauto CSV and run SAP tasks")
    parser.add_argument("--input", required=True, help="Path to msnauto CSV (from MnsAutomation.py --csv-out)")
    args = parser.parse_args()

    csv_path = Path(args.input)
    if not csv_path.exists():
        raise FileNotFoundError(f"Input CSV not found: {csv_path}")

    # Example processing: iterate orders and print
    # Replace this block with SAP upload / RFC / API integration logic
    for row in read_rows(csv_path):
        order_id = row.get("order_id", "")
        product_title = row.get("product_title", "")
        product_url = row.get("product_url", "")
        generated_at_utc = row.get("generated_at_utc", "")
        print(f"[sap_runn] Processing order_id={order_id} title={product_title} url={product_url} ts={generated_at_utc}")


if __name__ == "__main__":
    main()


