# Bundled Seed & Reference Data

The app ships with a starter reference dataset so it is useful the moment it is
installed — no internet needed. On first launch the data is loaded from bundled
JSON assets and inserted into the local Room database.

## What's included

| File | Contents | Used for |
|---|---|---|
| `app/src/main/assets/seed/vehicles.json` | ~208 Indian-market cars & two-wheelers (make, model, variants, year range, fuel types, body type) | Fitment wizard ("Find Parts by Vehicle") |
| `app/src/main/assets/seed/categories.json` | 22 spare-parts categories with icons + default HSN code + indicative GST % | Category picker, HSN/GST auto-fill |
| `app/src/main/assets/seed/parts_reference.json` | ~196 common replacement parts (category, HSN code, GST %, common brands, search keywords) | "Quick add" suggestions when typing a part name |

## How it works

1. `ReferenceDataRepository` loads and caches the JSON assets from
   `app/src/main/assets/seed/`.
2. `CategoryRepository.ensureSeeded()` and `VehicleRepository.ensureSeeded()`
   seed the Room database on first launch (they run in `AppShellViewModel.init`
   and are idempotent — they only insert when the table is empty).
3. `AddPartScreen` uses `ReferenceDataRepository.searchParts(...)` for live
   quick-add suggestions and `defaultsForCategory(...)` to auto-fill the HSN
   code and GST % when a category is selected.

## Where the data comes from (free / public)

All of this is **factual, publicly-known information**:

- Vehicle makes/models/variants — general automotive knowledge (India market).
- Replacement-part names and common aftermarket brands — general trade knowledge.
- HSN codes & GST rates — the public Indian GST/HSN rate schedules.

No data is copied from any proprietary catalog, database, or paid feed. You are
free to use, edit, or replace any of it.

> ⚠️ **GST note:** rates in the dataset are *indicative* and vary by exact item
> and year. Your shop runs on the **Composition scheme** (Bill of Supply), so the
> app does not charge GST on bills today. Verify any HSN/GST values before you
> switch to the **Regular** scheme.

## Updating / extending the data

Edit `tools/generate_seed_data.py` and regenerate the JSON files:

```bash
python3 tools/generate_seed_data.py
```

Then rebuild the app. Because the data is bundled assets (not hardcoded in Kotlin),
you can expand it — e.g. add more models, two-wheeler brands, or your own parts —
without touching app code.

> If the app has already been installed and seeded, re-running the app will
> **not** overwrite existing data (seeding only happens on an empty table). To
> re-seed, clear the app's data or uninstall/reinstall.
