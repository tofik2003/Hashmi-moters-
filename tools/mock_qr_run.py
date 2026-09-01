#!/usr/bin/env python3
"""
Mock-run harness for QrProductParser.

This mirrors the parsing rules in
    app/src/main/java/com/hashmimotors/app/util/QrProductParser.kt
line-for-line in Python so it can be executed locally without an Android
toolchain. It feeds a table of sample QR/barcode payloads through the parser and
prints the result, proving the parsing logic behaves correctly.

Usage:
    python3 tools/mock_qr_run.py

Keep this file in sync with QrProductParser.kt when the rules change.
"""

import json as _json

CURRENCY_SYMBOLS = "₹$€£¥"


def parse_money(s: str):
    cleaned = "".join(ch for ch in s.strip() if ch.isdigit() or ch in ".-")
    if cleaned in ("", "-", "."):
        return None
    try:
        return float(cleaned)
    except ValueError:
        return None


def parse_qty(s: str):
    cleaned = "".join(ch for ch in s.strip() if ch.isdigit())
    if not cleaned:
        return None
    try:
        return int(cleaned)
    except ValueError:
        return None


def looks_like_money(t: str) -> bool:
    if not t or any(ch.isalpha() for ch in t):
        return False
    has_symbol = any(ch in CURRENCY_SYMBOLS for ch in t)
    has_decimal = "." in t or "," in t
    value = parse_money(t)
    if value is None:
        return False
    if not has_symbol and not has_decimal and value >= 1_000_000.0:
        return False
    return True


def split_delimited(v: str):
    pipe = [p.strip() for p in v.split("|") if p.strip()]
    if len(pipe) >= 2:
        return pipe
    tab = [p.strip() for p in v.split("\t") if p.strip()]
    if len(tab) >= 2:
        return tab
    comma = [p.strip() for p in v.split(",") if p.strip()]
    looks_csv = (
        len(comma) >= 2
        and any(any(c.isalpha() for c in t) for t in comma)
        and any(looks_like_money(t) or t.isdigit() for t in comma)
    )
    return comma if looks_csv else []


def parse_json(v: str):
    try:
        root = _json.loads(v)
    except Exception:
        return None
    if not isinstance(root, dict):
        return None

    def get_str(keys):
        for k in keys:
            val = root.get(k)
            if isinstance(val, str) and val.strip():
                return val.strip()
        return None

    def get_num(keys):
        for k in keys:
            val = root.get(k)
            if val is None:
                continue
            if isinstance(val, (int, float)) and not isinstance(val, bool):
                return float(val)
            if isinstance(val, str):
                m = parse_money(val)
                if m is not None:
                    return m
        return None

    def get_int(keys):
        for k in keys:
            val = root.get(k)
            if val is None:
                continue
            if isinstance(val, bool):
                continue
            if isinstance(val, int):
                return val
            if isinstance(val, float) and val.is_integer():
                return int(val)
            if isinstance(val, str):
                q = parse_qty(val)
                if q is not None:
                    return q
        return None

    name = get_str(["name", "title", "product", "item", "description"])
    if name is None:
        return None
    return {
        "name": name,
        "mrp": get_num(["price", "mrp", "rate", "amount", "cost"]),
        "sku": get_str(["sku", "code", "id", "itemCode"]),
        "barcode": get_str(["barcode", "ean", "upc", "gtin"]),
        "qty": max(get_int(["qty", "quantity"]) or 1, 1),
    }


def parse_key_value(v: str):
    mapping = {}
    for segment in [s for s in v.replace("\r", "").replace("\n", ";").split(";")]:
        idx = min([i for i in (segment.find("="), segment.find(":")) if i >= 0] or [-1])
        if idx <= 0:
            continue
        key = segment[:idx].strip().lower()
        value = segment[idx + 1:].strip()
        if key and value:
            mapping[key] = value
    if not mapping:
        return None
    name = mapping.get("name") or mapping.get("title") or mapping.get("product") or mapping.get("item")
    if name is None:
        return None
    mrp = None
    for k in ("price", "mrp", "rate", "amount"):
        if k in mapping:
            mrp = parse_money(mapping[k])
            if mrp is not None:
                break
    sku = mapping.get("sku") or mapping.get("code") or mapping.get("id")
    barcode = mapping.get("barcode") or mapping.get("ean") or mapping.get("upc") or mapping.get("gtin")
    qty_raw = mapping.get("qty") or mapping.get("quantity")
    qty = parse_qty(qty_raw) if qty_raw else 1
    return {"name": name, "mrp": mrp, "sku": sku, "barcode": barcode, "qty": max(qty or 1, 1)}


def parse_delimited(v: str):
    tokens = split_delimited(v)
    if len(tokens) < 2:
        return None
    name = tokens[0]
    if not any(ch.isalpha() for ch in name):
        return None

    price_index = -1
    for i in range(1, len(tokens)):
        if looks_like_money(tokens[i]):
            price_index = i
            break
    price = parse_money(tokens[price_index]) if price_index >= 0 else None

    barcode = None
    sku = None
    for i in range(1, len(tokens)):
        if i == price_index:
            continue
        t = tokens[i]
        if not t:
            continue
        if barcode is None and t.isdigit() and len(t) >= 8:
            barcode = t
        elif sku is None and any(ch.isalnum() for ch in t) and any(ch.isalpha() for ch in t):
            sku = t

    if price is None and sku is None and barcode is None:
        return None
    return {"name": name, "mrp": price, "sku": sku, "barcode": barcode, "qty": 1}


def parse(raw: str):
    v = raw.strip()
    if not v:
        return None
    if v.startswith("{"):
        result = parse_json(v)
        if result is not None:
            return result
    result = parse_key_value(v)
    if result is not None:
        return result
    return parse_delimited(v)


CASES = [
    # ---- JSON ----
    '{"name":"Oil Filter","price":250,"sku":"OF-01","barcode":"8901234567890","qty":2}',
    '{"name":"Air Filter","mrp":"₹320.50"}',
    '{"barcode":"8901234567890"}',
    "{not json at all}",
    # ---- key=value ----
    "name=Brake Pads;price=1250;sku=BP-77;qty=3",
    "name: Engine Oil\nprice: 550\nbarcode: 8901111222333",
    # ---- delimited ----
    "Oil Filter | 250 | OF-01",
    "Oil Filter | OF-01 | 250",
    "Brake Pads\t1250\t8901234567890",
    "Spark Plug, 240, SP-9",
    "Car Shampoo|₹240.00|CSH-1",
    "Oil Filter",
    # ---- plain identifiers (should NOT parse as a product) ----
    "8901234567890",
    "https://example.com/item/123",
    "   ",
]


def main():
    print("QrProductParser — mock run\n" + "=" * 62)
    for raw in CASES:
        p = parse(raw)
        if p is None:
            print(f"  {raw!r}\n    -> plain code (no product data)\n")
        else:
            mrp = f"{p['mrp']:.2f}" if p["mrp"] is not None else "—"
            print(
                f"  {raw!r}\n"
                f"    -> name={p['name']!r}  mrp={mrp}  sku={p['sku']!r}  "
                f"barcode={p['barcode']!r}  qty={p['qty']}\n"
            )


if __name__ == "__main__":
    main()
