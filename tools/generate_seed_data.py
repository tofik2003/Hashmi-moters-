#!/usr/bin/env python3
"""
Generate bundled seed/reference datasets for the Hashmi Motors app.

All data here is factual, publicly-known information (vehicle makes/models,
standard replacement-part names, HSN codes and GST rates from the public
Indian GST/HSN schedules). It contains no copyrighted/proprietary content and
is safe to bundle with the app. GST rates are *indicative* — the app owner
should verify against their own GST scheme (composition vs regular).

Outputs (written to app/src/main/assets/seed/):
  - vehicles.json        -> fitment lookup (make/model/variants/years/fuel/body)
  - categories.json      -> part category taxonomy + default HSN & GST
  - parts_reference.json -> common replacement parts (category, HSN, GST, brands)

Usage:
    python3 tools/generate_seed_data.py

Edit the lists below and re-run to update the bundled data — no code changes needed.
"""

import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
OUT_DIR = os.path.join(HERE, "..", "app", "src", "main", "assets", "seed")


def v(make, model, variants, yf, yt, fuels, body):
    """Helper to build one vehicle record."""
    return {
        "make": make,
        "model": model,
        "variants": variants,
        "yearFrom": yf,
        "yearTo": yt,
        "fuelTypes": fuels,
        "bodyType": body,
    }


# ---------------------------------------------------------------------------
# VEHICLES — Indian-market cars & popular two-wheelers.
# ---------------------------------------------------------------------------
VEHICLES = [
    # ===================== MARUTI SUZUKI =====================
    v("Maruti", "Alto 800", ["STD", "LXI", "VXI", "VXI+"], 2012, 2023, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "Alto K10", ["STD", "LXI", "VXI", "VXI+"], 2022, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "S-Presso", ["STD", "LXI", "VXI", "VXI+"], 2019, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "Celerio", ["LXI", "VXI", "ZXI", "ZXI+"], 2021, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "Wagon R", ["LXI", "VXI", "ZXI", "ZXI+"], 2019, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "Swift", ["LXI", "VXI", "ZXI", "ZXI+"], 2014, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "Baleno", ["Sigma", "Delta", "Zeta", "Alpha"], 2015, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Maruti", "Ignis", ["Sigma", "Delta", "Zeta", "Alpha"], 2017, 2025, ["Petrol"], "Hatchback"),
    v("Maruti", "Dzire", ["LXI", "VXI", "ZXI", "ZXI+"], 2017, 2025, ["Petrol", "CNG"], "Sedan"),
    v("Maruti", "Ciaz", ["Sigma", "Delta", "Zeta", "Alpha"], 2014, 2025, ["Petrol"], "Sedan"),
    v("Maruti", "Ertiga", ["LXI", "VXI", "ZXI", "ZXI+"], 2018, 2025, ["Petrol", "CNG"], "MPV"),
    v("Maruti", "XL6", ["Zeta", "Alpha", "Alpha+"], 2019, 2025, ["Petrol", "CNG"], "MPV"),
    v("Maruti", "Vitara Brezza", ["LXI", "VXI", "ZXI", "ZXI+"], 2016, 2020, ["Petrol", "Diesel"], "Compact SUV"),
    v("Maruti", "Brezza", ["LXI", "VXI", "ZXI", "ZXI+"], 2022, 2025, ["Petrol", "CNG"], "Compact SUV"),
    v("Maruti", "Fronx", ["Sigma", "Delta", "Zeta", "Alpha"], 2023, 2025, ["Petrol", "CNG"], "Compact SUV"),
    v("Maruti", "Grand Vitara", ["Sigma", "Delta", "Zeta", "Alpha"], 2022, 2025, ["Petrol", "Hybrid", "CNG"], "SUV"),
    v("Maruti", "Jimny", ["Zeta", "Alpha"], 2023, 2025, ["Petrol"], "SUV"),
    v("Maruti", "Invicto", ["Zeta+", "Alpha+"], 2023, 2025, ["Hybrid"], "MPV"),
    v("Maruti", "Eeco", ["Standard", "AC"], 2010, 2025, ["Petrol", "CNG"], "Van"),
    v("Maruti", "S-Cross", ["Sigma", "Delta", "Zeta", "Alpha"], 2015, 2022, ["Petrol", "Diesel"], "Crossover"),
    # ===================== HYUNDAI =====================
    v("Hyundai", "Grand i10 Nios", ["Era", "Magna", "Sportz", "Asta"], 2019, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Hyundai", "i20", ["Magna", "Sportz", "Asta", "Asta(O)"], 2020, 2025, ["Petrol", "Diesel"], "Hatchback"),
    v("Hyundai", "i20 N Line", ["N6", "N8"], 2021, 2025, ["Petrol"], "Hatchback"),
    v("Hyundai", "Aura", ["E", "S", "SX", "SX+"], 2020, 2025, ["Petrol", "CNG"], "Sedan"),
    v("Hyundai", "Verna", ["EX", "S", "SX", "SX(O)"], 2017, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("Hyundai", "Creta", ["E", "EX", "S", "SX", "SX(O)"], 2015, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Hyundai", "Venue", ["E", "S", "S+", "SX", "SX(O)"], 2019, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Hyundai", "Exter", ["EX", "S", "SX", "SX(O)"], 2023, 2025, ["Petrol", "CNG"], "Micro SUV"),
    v("Hyundai", "Alcazar", ["Prestige", "Platinum", "Signature"], 2021, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Hyundai", "Tucson", ["Platinum", "Signature"], 2022, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Hyundai", "Kona Electric", ["Premium", "Premium Dual Tone"], 2019, 2025, ["EV"], "Compact SUV"),
    v("Hyundai", "Ioniq 5", ["Premium", "Signature"], 2023, 2025, ["EV"], "Crossover"),
    # ===================== TATA =====================
    v("Tata", "Tiago", ["XE", "XM", "XT", "XZ"], 2016, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Tata", "Tiago EV", ["XE", "XT", "XZ"], 2022, 2025, ["EV"], "Hatchback"),
    v("Tata", "Tigor", ["XE", "XM", "XT", "XZ"], 2017, 2025, ["Petrol", "CNG"], "Sedan"),
    v("Tata", "Tigor EV", ["XE", "XT", "XZ"], 2021, 2025, ["EV"], "Sedan"),
    v("Tata", "Altroz", ["XE", "XM", "XZ", "XZ+"], 2020, 2025, ["Petrol", "Diesel", "CNG"], "Hatchback"),
    v("Tata", "Punch", ["Pure", "Adventure", "Accomplished", "Creative"], 2021, 2025, ["Petrol", "CNG"], "Micro SUV"),
    v("Tata", "Punch EV", ["Smart", "Adventure", "Empowered"], 2024, 2025, ["EV"], "Micro SUV"),
    v("Tata", "Nexon", ["XE", "XM", "XZ", "XZ+", "Fearless", "Creative"], 2017, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Tata", "Nexon EV", ["XM", "XZ+", "Empowered"], 2020, 2025, ["EV"], "Compact SUV"),
    v("Tata", "Curvv", ["Smart", "Pure", "Creative", "Accomplished"], 2024, 2025, ["Petrol", "Diesel", "EV"], "Crossover"),
    v("Tata", "Harrier", ["XE", "XM", "XZ", "XZ+", "Fearless"], 2019, 2025, ["Diesel"], "SUV"),
    v("Tata", "Safari", ["XE", "XM", "XZ", "XZ+", "Accomplished"], 2021, 2025, ["Diesel"], "SUV"),
    # ===================== MAHINDRA =====================
    v("Mahindra", "Bolero", ["B4", "B6", "B6(O)"], 2010, 2025, ["Diesel"], "SUV"),
    v("Mahindra", "Bolero Neo", ["N4", "N8", "N10"], 2021, 2025, ["Diesel"], "SUV"),
    v("Mahindra", "Scorpio", ["S3", "S5", "S7", "S9", "S11"], 2014, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Mahindra", "Scorpio-N", ["Z4", "Z6", "Z8"], 2022, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Mahindra", "XUV300", ["W4", "W6", "W8", "W8(O)"], 2019, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Mahindra", "XUV3XO", ["MX1", "MX2", "MX3", "AX5", "AX7"], 2024, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Mahindra", "XUV400", ["EC", "EL"], 2023, 2025, ["EV"], "Compact SUV"),
    v("Mahindra", "XUV700", ["MX", "AX3", "AX5", "AX7"], 2021, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Mahindra", "Thar", ["AX", "LX"], 2010, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Mahindra", "Thar Roxx", ["MX1", "MX3", "AX5", "AX7"], 2024, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Mahindra", "Marazzo", ["M2", "M4", "M6", "M8"], 2018, 2025, ["Diesel"], "MPV"),
    v("Mahindra", "BE 6", ["Pack One", "Pack Two", "Pack Three"], 2024, 2025, ["EV"], "SUV"),
    v("Mahindra", "XEV 9e", ["Pack One", "Pack Two", "Pack Three"], 2024, 2025, ["EV"], "SUV"),
    # ===================== HONDA (CARS) =====================
    v("Honda", "Amaze", ["E", "S", "VX", "ZX"], 2018, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("Honda", "City", ["SV", "V", "VX", "ZX"], 2014, 2025, ["Petrol", "Diesel", "Hybrid"], "Sedan"),
    v("Honda", "Elevate", ["SV", "V", "VX", "ZX"], 2023, 2025, ["Petrol"], "SUV"),
    v("Honda", "Jazz", ["V", "VX", "ZX"], 2015, 2020, ["Petrol", "Diesel"], "Hatchback"),
    v("Honda", "WR-V", ["SV", "VX", "ZX"], 2017, 2020, ["Petrol", "Diesel"], "Crossover"),
    v("Honda", "Brio", ["E", "S", "VX"], 2011, 2020, ["Petrol"], "Hatchback"),
    v("Honda", "Civic", ["V", "VX", "ZX"], 2019, 2021, ["Petrol", "Diesel"], "Sedan"),
    v("Honda", "CR-V", ["RV", "EV"], 2018, 2020, ["Petrol", "Diesel"], "SUV"),
    # ===================== TOYOTA =====================
    v("Toyota", "Innova Crysta", ["G", "GX", "VX", "ZX", "ZX(O)"], 2016, 2022, ["Petrol", "Diesel"], "MPV"),
    v("Toyota", "Innova Hycross", ["GX", "VX", "ZX", "ZX(O)"], 2022, 2025, ["Petrol", "Hybrid"], "MPV"),
    v("Toyota", "Fortuner", ["4x2", "4x4", "Legender"], 2016, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Toyota", "Glanza", ["E", "S", "G", "V"], 2019, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Toyota", "Urban Cruiser", ["Mid", "High", "Premium"], 2020, 2022, ["Petrol"], "Compact SUV"),
    v("Toyota", "Urban Cruiser Hyryder", ["E", "S", "G", "V"], 2022, 2025, ["Petrol", "Hybrid", "CNG"], "Compact SUV"),
    v("Toyota", "Taisor", ["E", "S", "G", "V"], 2024, 2025, ["Petrol", "CNG"], "Compact SUV"),
    v("Toyota", "Rumion", ["S", "G", "V"], 2023, 2025, ["Petrol", "CNG"], "MPV"),
    v("Toyota", "Camry", ["Hybrid"], 2020, 2025, ["Hybrid"], "Sedan"),
    v("Toyota", "Vellfire", ["Luxury"], 2023, 2025, ["Hybrid"], "MPV"),
    v("Toyota", "Hilux", ["High"], 2022, 2025, ["Diesel"], "Pickup"),
    # ===================== KIA =====================
    v("Kia", "Sonet", ["HTE", "HTK", "HTX", "HTX+", "GTX", "GTX+"], 2020, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Kia", "Seltos", ["HTE", "HTK", "HTX", "HTX+", "GTX", "GTX+", "X-Line"], 2019, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Kia", "Carens", ["Premium", "Prestige", "Prestige Plus", "Luxury", "Luxury Plus"], 2022, 2025, ["Petrol", "Diesel"], "MPV"),
    v("Kia", "Carnival", ["Premium", "Prestige", "Limousine"], 2020, 2025, ["Diesel"], "MPV"),
    v("Kia", "EV6", ["GT Line"], 2022, 2025, ["EV"], "Crossover"),
    # ===================== RENAULT =====================
    v("Renault", "Kwid", ["RXE", "RXL", "RXT", "Climber"], 2015, 2025, ["Petrol"], "Hatchback"),
    v("Renault", "Triber", ["RXE", "RXL", "RXT", "RXZ"], 2019, 2025, ["Petrol", "CNG"], "MPV"),
    v("Renault", "Kiger", ["RXE", "RXL", "RXT", "RXZ"], 2021, 2025, ["Petrol", "CNG"], "Compact SUV"),
    v("Renault", "Duster", ["RXE", "RXL", "RXS", "RXZ"], 2012, 2022, ["Petrol", "Diesel"], "SUV"),
    # ===================== NISSAN =====================
    v("Nissan", "Magnite", ["XE", "XL", "XV", "XV Premium"], 2020, 2025, ["Petrol", "CNG"], "Compact SUV"),
    v("Nissan", "Kicks", ["XL", "XV", "XV Premium"], 2019, 2022, ["Petrol", "Diesel"], "Compact SUV"),
    # ===================== VOLKSWAGEN =====================
    v("Volkswagen", "Polo", ["Trendline", "Comfortline", "Highline", "GT"], 2014, 2022, ["Petrol", "Diesel"], "Hatchback"),
    v("Volkswagen", "Vento", ["Trendline", "Comfortline", "Highline"], 2014, 2022, ["Petrol", "Diesel"], "Sedan"),
    v("Volkswagen", "Virtus", ["Comfortline", "Highline", "Topline", "GT"], 2022, 2025, ["Petrol"], "Sedan"),
    v("Volkswagen", "Taigun", ["Comfortline", "Highline", "Topline", "GT"], 2021, 2025, ["Petrol"], "Compact SUV"),
    v("Volkswagen", "Tiguan", ["Elegance"], 2017, 2025, ["Petrol", "Diesel"], "SUV"),
    # ===================== SKODA =====================
    v("Skoda", "Rapid", ["Active", "Ambition", "Style", "Monte Carlo"], 2014, 2022, ["Petrol", "Diesel"], "Sedan"),
    v("Skoda", "Slavia", ["Active", "Ambition", "Style", "Monte Carlo"], 2022, 2025, ["Petrol"], "Sedan"),
    v("Skoda", "Kushaq", ["Active", "Ambition", "Style", "Monte Carlo"], 2021, 2025, ["Petrol"], "Compact SUV"),
    v("Skoda", "Octavia", ["Ambition", "Style", "L&K", "RS"], 2014, 2023, ["Petrol", "Diesel"], "Sedan"),
    v("Skoda", "Superb", ["Sportline", "L&K"], 2016, 2023, ["Petrol", "Diesel"], "Sedan"),
    v("Skoda", "Kodiaq", ["Sportline", "L&K"], 2022, 2025, ["Petrol"], "SUV"),
    # ===================== MG =====================
    v("MG", "Hector", ["Style", "Super", "Sharp", "Savvy", "Plus"], 2019, 2025, ["Petrol", "Diesel", "Hybrid"], "SUV"),
    v("MG", "Hector Plus", ["Style", "Super", "Sharp", "Savvy"], 2020, 2025, ["Petrol", "Diesel", "Hybrid"], "SUV"),
    v("MG", "Astor", ["Style", "Super", "Smart", "Sharp", "Savvy"], 2021, 2025, ["Petrol"], "Compact SUV"),
    v("MG", "ZS EV", ["Excite", "Exclusive", "Essence"], 2020, 2025, ["EV"], "Compact SUV"),
    v("MG", "Comet EV", ["Play", "Plush"], 2023, 2025, ["EV"], "Hatchback"),
    v("MG", "Gloster", ["Super", "Sharp", "Savvy"], 2020, 2025, ["Diesel"], "SUV"),
    v("MG", "Windsor EV", ["Excite", "Exclusive", "Essence"], 2024, 2025, ["EV"], "Crossover"),
    # ===================== JEEP =====================
    v("Jeep", "Compass", ["Sport", "Longitude", "Limited", "Model S"], 2017, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Jeep", "Meridian", ["Limited", "Model S", "Overland"], 2022, 2025, ["Diesel"], "SUV"),
    v("Jeep", "Wrangler", ["Rubicon"], 2021, 2025, ["Petrol"], "SUV"),
    # ===================== CITROEN =====================
    v("Citroen", "C3", ["Live", "Feel", "Shine", "Shine(O)"], 2022, 2025, ["Petrol", "CNG"], "Hatchback"),
    v("Citroen", "eC3", ["Live", "Feel", "Shine"], 2023, 2025, ["EV"], "Hatchback"),
    v("Citroen", "C3 Aircross", ["You", "Plus", "Max"], 2023, 2025, ["Petrol"], "Compact SUV"),
    v("Citroen", "C5 Aircross", ["Feel", "Shine"], 2021, 2025, ["Diesel"], "SUV"),
    v("Citroen", "Basalt", ["You", "Plus", "Max"], 2024, 2025, ["Petrol"], "Crossover"),
    # ===================== FORCE =====================
    v("Force", "Gurkha", ["Xplorer", "Xpedition"], 2021, 2025, ["Diesel"], "SUV"),
    # ===================== FORD (exited, still on road) =====================
    v("Ford", "Figo", ["Base", "Titanium", "Titanium+"], 2015, 2021, ["Petrol", "Diesel"], "Hatchback"),
    v("Ford", "Aspire", ["Trend", "Titanium", "Titanium+"], 2015, 2021, ["Petrol", "Diesel"], "Sedan"),
    v("Ford", "Freestyle", ["Titanium", "Titanium+"], 2018, 2021, ["Petrol", "Diesel"], "Crossover"),
    v("Ford", "EcoSport", ["Trend", "Titanium", "Titanium+", "S"], 2013, 2022, ["Petrol", "Diesel"], "Compact SUV"),
    v("Ford", "Endeavour", ["Trend", "Titanium", "Titanium+"], 2016, 2022, ["Diesel"], "SUV"),
    # ===================== FIAT (exited, still on road) =====================
    v("Fiat", "Punto Evo", ["Active", "Dynamic", "Emotion"], 2012, 2019, ["Petrol", "Diesel"], "Hatchback"),
    v("Fiat", "Linea", ["Active", "Dynamic", "Emotion"], 2012, 2019, ["Petrol", "Diesel"], "Sedan"),
    # ===================== DATSUN (exited, still on road) =====================
    v("Datsun", "redi-GO", ["D", "A", "S"], 2016, 2022, ["Petrol"], "Hatchback"),
    v("Datsun", "GO", ["D", "A", "T"], 2014, 2020, ["Petrol"], "Hatchback"),
    v("Datsun", "GO+", ["D", "A", "T"], 2015, 2020, ["Petrol"], "MPV"),
    # ===================== CHEVROLET (exited, still on road) =====================
    v("Chevrolet", "Beat", ["LS", "LT", "LTZ"], 2011, 2017, ["Petrol", "Diesel"], "Hatchback"),
    v("Chevrolet", "Sail", ["LS", "LT", "LTZ"], 2013, 2017, ["Petrol", "Diesel"], "Sedan"),
    v("Chevrolet", "Cruze", ["LT", "LTZ"], 2011, 2017, ["Petrol", "Diesel"], "Sedan"),
    v("Chevrolet", "Tavera", ["LS", "LT"], 2005, 2017, ["Diesel"], "MPV"),
    # ===================== MERCEDES-BENZ =====================
    v("Mercedes-Benz", "A-Class", ["A200", "A200d"], 2020, 2023, ["Petrol", "Diesel"], "Hatchback"),
    v("Mercedes-Benz", "C-Class", ["C200", "C220d", "C300d"], 2015, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("Mercedes-Benz", "E-Class", ["E200", "E220d", "E350d"], 2016, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("Mercedes-Benz", "S-Class", ["S350d", "S450"], 2018, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("Mercedes-Benz", "GLA", ["200", "220d"], 2021, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("Mercedes-Benz", "GLC", ["200", "220d", "300"], 2016, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Mercedes-Benz", "GLE", ["300d", "400d", "450"], 2016, 2025, ["Petrol", "Diesel"], "SUV"),
    # ===================== BMW =====================
    v("BMW", "3 Series", ["320Ld", "330i", "M340i"], 2015, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("BMW", "5 Series", ["520d", "530i"], 2015, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("BMW", "7 Series", ["740Ld", "740Li"], 2016, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("BMW", "2 Series Gran Coupe", ["220i"], 2021, 2025, ["Petrol"], "Sedan"),
    v("BMW", "X1", ["sDrive20i", "sDrive18d"], 2016, 2025, ["Petrol", "Diesel"], "Compact SUV"),
    v("BMW", "X3", ["xDrive20d", "xDrive30i"], 2018, 2025, ["Petrol", "Diesel"], "SUV"),
    v("BMW", "X5", ["xDrive30d", "xDrive40i"], 2019, 2025, ["Petrol", "Diesel"], "SUV"),
    v("BMW", "X7", ["xDrive30d", "xDrive40i"], 2019, 2025, ["Petrol", "Diesel"], "SUV"),
    # ===================== AUDI =====================
    v("Audi", "A4", ["Premium", "Premium Plus", "Technology"], 2016, 2025, ["Petrol"], "Sedan"),
    v("Audi", "A6", ["Premium", "Premium Plus", "Technology"], 2016, 2025, ["Petrol"], "Sedan"),
    v("Audi", "Q3", ["Premium", "Premium Plus", "Technology"], 2016, 2025, ["Petrol"], "SUV"),
    v("Audi", "Q5", ["Premium", "Premium Plus", "Technology"], 2016, 2025, ["Petrol"], "SUV"),
    v("Audi", "Q7", ["Premium Plus", "Technology"], 2016, 2025, ["Petrol"], "SUV"),
    v("Audi", "Q8", ["Celebration", "Technology"], 2020, 2025, ["Petrol"], "SUV"),
    # ===================== VOLVO =====================
    v("Volvo", "XC40", ["Momentum", "R-Design", "Inscription"], 2019, 2025, ["Petrol", "EV"], "Compact SUV"),
    v("Volvo", "XC60", ["Momentum", "R-Design", "Inscription"], 2018, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Volvo", "XC90", ["Momentum", "Inscription"], 2016, 2025, ["Petrol", "Diesel", "Hybrid"], "SUV"),
    v("Volvo", "S90", ["Momentum", "Inscription"], 2017, 2025, ["Petrol", "Diesel"], "Sedan"),
    v("Volvo", "C40 Recharge", ["Plus", "Ultimate"], 2023, 2025, ["EV"], "Crossover"),
    # ===================== LAND ROVER =====================
    v("Land Rover", "Range Rover Evoque", ["SE", "HSE", "Dynamic"], 2016, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Land Rover", "Range Rover Velar", ["S", "R-Dynamic"], 2018, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Land Rover", "Range Rover Sport", ["SE", "HSE"], 2016, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Land Rover", "Discovery Sport", ["SE", "HSE"], 2016, 2025, ["Petrol", "Diesel"], "SUV"),
    v("Land Rover", "Defender", ["90", "110"], 2021, 2025, ["Petrol", "Diesel"], "SUV"),
    # ===================== LEXUS =====================
    v("Lexus", "ES", ["Luxury", "F-Sport"], 2019, 2025, ["Petrol", "Hybrid"], "Sedan"),
    v("Lexus", "NX", ["Luxury", "F-Sport"], 2018, 2025, ["Petrol", "Hybrid"], "SUV"),
    v("Lexus", "RX", ["Luxury", "F-Sport"], 2018, 2025, ["Petrol", "Hybrid"], "SUV"),
    # ===================== BYD =====================
    v("BYD", "Atto 3", ["Dynamic", "Premium", "Luxury"], 2022, 2025, ["EV"], "Compact SUV"),
    v("BYD", "Seal", ["Dynamic", "Premium", "Performance"], 2024, 2025, ["EV"], "Sedan"),
    v("BYD", "e6", ["Standard"], 2023, 2025, ["EV"], "MPV"),
    # ===================== TWO-WHEELERS: HERO =====================
    v("Hero", "Splendor Plus", ["Kick", "Self", "i3S"], 1994, 2025, ["Petrol"], "Motorcycle"),
    v("Hero", "Passion Pro", ["Pro", "i3S"], 2001, 2025, ["Petrol"], "Motorcycle"),
    v("Hero", "HF Deluxe", ["Kick", "Self"], 2005, 2025, ["Petrol"], "Motorcycle"),
    v("Hero", "Glamour", ["Kick", "Self"], 2006, 2025, ["Petrol"], "Motorcycle"),
    v("Hero", "Xpulse 200", ["Xpulse", "Xpulse 200 4V"], 2019, 2025, ["Petrol"], "Motorcycle"),
    v("Hero", "Karizma XMR", ["Standard"], 2023, 2025, ["Petrol"], "Motorcycle"),
    # ===================== TWO-WHEELERS: HONDA =====================
    v("Honda", "Activa", ["6G", "7G", "DLX", "H-Smart"], 2001, 2025, ["Petrol"], "Scooter"),
    v("Honda", "Dio", ["STD", "DLX"], 2002, 2025, ["Petrol"], "Scooter"),
    v("Honda", "Shine", ["Drum", "Disc"], 2006, 2025, ["Petrol"], "Motorcycle"),
    v("Honda", "SP 125", ["Drum", "Disc"], 2019, 2025, ["Petrol"], "Motorcycle"),
    v("Honda", "Unicorn", ["Standard"], 2004, 2025, ["Petrol"], "Motorcycle"),
    v("Honda", "CB350", ["DLX", "RS"], 2020, 2025, ["Petrol"], "Motorcycle"),
    # ===================== TWO-WHEELERS: TVS =====================
    v("TVS", "Jupiter", ["STD", "ZX", "Classic"], 2013, 2025, ["Petrol"], "Scooter"),
    v("TVS", "Ntorq 125", ["Drum", "Disc", "Race Edition"], 2018, 2025, ["Petrol"], "Scooter"),
    v("TVS", "Apache RTR 160", ["Drum", "Disc", "4V"], 2007, 2025, ["Petrol"], "Motorcycle"),
    v("TVS", "Apache RTR 200 4V", ["Standard"], 2018, 2025, ["Petrol"], "Motorcycle"),
    v("TVS", "Raider", ["Drum", "Disc"], 2021, 2025, ["Petrol"], "Motorcycle"),
    v("TVS", "Sport", ["Standard"], 2007, 2025, ["Petrol"], "Motorcycle"),
    # ===================== TWO-WHEELERS: BAJAJ =====================
    v("Bajaj", "Pulsar 150", ["Standard", "Twin Disc"], 2001, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "Pulsar NS160", ["Standard"], 2017, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "Pulsar NS200", ["Standard"], 2012, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "Pulsar 220F", ["Standard"], 2007, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "Platina 110", ["Drum", "Disc"], 2006, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "CT100", ["Standard"], 2004, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "Dominar 400", ["Standard"], 2016, 2025, ["Petrol"], "Motorcycle"),
    v("Bajaj", "Chetak", ["Premium", "Urbane"], 2020, 2025, ["EV"], "Scooter"),
    # ===================== TWO-WHEELERS: ROYAL ENFIELD =====================
    v("Royal Enfield", "Classic 350", ["Redditch", "Halcyon", "Signals"], 2009, 2025, ["Petrol"], "Motorcycle"),
    v("Royal Enfield", "Bullet 350", ["Standard", "ES"], 2010, 2025, ["Petrol"], "Motorcycle"),
    v("Royal Enfield", "Hunter 350", ["Retro", "Metro"], 2022, 2025, ["Petrol"], "Motorcycle"),
    v("Royal Enfield", "Meteor 350", ["Fireball", "Stellar", "Supernova"], 2020, 2025, ["Petrol"], "Motorcycle"),
    v("Royal Enfield", "Himalayan", ["Standard", "450"], 2016, 2025, ["Petrol"], "Motorcycle"),
    v("Royal Enfield", "Interceptor 650", ["Standard"], 2018, 2025, ["Petrol"], "Motorcycle"),
    v("Royal Enfield", "Continental GT 650", ["Standard"], 2018, 2025, ["Petrol"], "Motorcycle"),
    # ===================== TWO-WHEELERS: YAMAHA =====================
    v("Yamaha", "FZ", ["FZ", "FZS", "FZ-X"], 2008, 2025, ["Petrol"], "Motorcycle"),
    v("Yamaha", "R15", ["R15", "R15 V4", "R15M"], 2008, 2025, ["Petrol"], "Motorcycle"),
    v("Yamaha", "MT-15", ["Standard", "V2"], 2019, 2025, ["Petrol"], "Motorcycle"),
    v("Yamaha", "Fascino", ["Standard", "Fi"], 2015, 2025, ["Petrol"], "Scooter"),
    v("Yamaha", "RayZR", ["Standard", "Street Rally"], 2016, 2025, ["Petrol"], "Scooter"),
    v("Yamaha", "Saluto RX", ["Standard"], 2015, 2025, ["Petrol"], "Motorcycle"),
    # ===================== TWO-WHEELERS: SUZUKI =====================
    v("Suzuki", "Access 125", ["STD", "Special Edition"], 2007, 2025, ["Petrol"], "Scooter"),
    v("Suzuki", "Burgman Street", ["Standard"], 2018, 2025, ["Petrol"], "Scooter"),
    v("Suzuki", "Gixxer", ["Standard", "SF"], 2014, 2025, ["Petrol"], "Motorcycle"),
    v("Suzuki", "Gixxer 250", ["Standard"], 2019, 2025, ["Petrol"], "Motorcycle"),
]

# ---------------------------------------------------------------------------
# CATEGORIES — part taxonomy with default HSN & indicative GST%.
# ---------------------------------------------------------------------------
CATEGORIES = [
    {"name": "Engine", "icon": "engine", "defaultHsn": "87089900", "defaultGstPercent": 28.0},
    {"name": "Brakes", "icon": "brake", "defaultHsn": "87083000", "defaultGstPercent": 28.0},
    {"name": "Suspension", "icon": "suspension", "defaultHsn": "87088000", "defaultGstPercent": 28.0},
    {"name": "Steering", "icon": "steering", "defaultHsn": "87089400", "defaultGstPercent": 28.0},
    {"name": "Clutch", "icon": "clutch", "defaultHsn": "87089300", "defaultGstPercent": 28.0},
    {"name": "Transmission", "icon": "transmission", "defaultHsn": "87084000", "defaultGstPercent": 28.0},
    {"name": "Filters", "icon": "filter", "defaultHsn": "84212300", "defaultGstPercent": 28.0},
    {"name": "Electrical", "icon": "bolt", "defaultHsn": "85119000", "defaultGstPercent": 28.0},
    {"name": "Lighting", "icon": "light", "defaultHsn": "85122010", "defaultGstPercent": 28.0},
    {"name": "Batteries", "icon": "battery", "defaultHsn": "85071000", "defaultGstPercent": 28.0},
    {"name": "Ignition", "icon": "ignition", "defaultHsn": "85113000", "defaultGstPercent": 28.0},
    {"name": "Cooling", "icon": "cooling", "defaultHsn": "87089100", "defaultGstPercent": 28.0},
    {"name": "Air Conditioning", "icon": "ac", "defaultHsn": "84159000", "defaultGstPercent": 28.0},
    {"name": "Oils & Fluids", "icon": "oil", "defaultHsn": "27101990", "defaultGstPercent": 18.0},
    {"name": "Belts & Hoses", "icon": "belt", "defaultHsn": "40103990", "defaultGstPercent": 18.0},
    {"name": "Body Parts", "icon": "body", "defaultHsn": "87082900", "defaultGstPercent": 28.0},
    {"name": "Glass", "icon": "glass", "defaultHsn": "70072190", "defaultGstPercent": 28.0},
    {"name": "Exhaust", "icon": "exhaust", "defaultHsn": "87089200", "defaultGstPercent": 28.0},
    {"name": "Wheels & Hubs", "icon": "wheel", "defaultHsn": "87087000", "defaultGstPercent": 28.0},
    {"name": "Bearings & Seals", "icon": "bearing", "defaultHsn": "84821090", "defaultGstPercent": 18.0},
    {"name": "Accessories", "icon": "star", "defaultHsn": "87089900", "defaultGstPercent": 28.0},
    {"name": "Tools", "icon": "tool", "defaultHsn": "82055990", "defaultGstPercent": 18.0},
]

# ---------------------------------------------------------------------------
# PARTS REFERENCE — common replacement parts (category, HSN, GST, brands).
# ---------------------------------------------------------------------------
def p(name, category, hsn, gst, brands=None, keywords=None):
    return {
        "name": name,
        "category": category,
        "hsnCode": hsn,
        "gstPercent": gst,
        "brands": brands or [],
        "keywords": keywords or [],
    }


PARTS = [
    # ---- Engine ----
    p("Cylinder Head Gasket", "Engine", "84841090", 18.0, ["Goetze", "Perfect Circle", "India Gaskets"], ["head gasket"]),
    p("Valve Cover Gasket", "Engine", "84841090", 18.0, ["Goetze", "Perfect Circle"], ["rocker cover gasket"]),
    p("Engine Overhaul Gasket Kit", "Engine", "84849000", 18.0, ["Goetze", "Perfect Circle"], ["full gasket set"]),
    p("Piston Ring Set", "Engine", "84099990", 28.0, ["Goetze", "Perfect Circle", "Mahle"], ["piston rings"]),
    p("Timing Chain Kit", "Engine", "73151100", 18.0, ["Tsubaki", "LGB", "Rolon"], ["timing chain", "cam chain"]),
    p("Water Pump", "Engine", "84133090", 28.0, ["Bosch", "Valeo", "TVS", "Aisin"], ["coolant pump"]),
    p("Oil Pump", "Engine", "84133090", 28.0, ["Bosch", "TVS"], []),
    p("Fuel Pump", "Engine", "84133090", 28.0, ["Bosch", "Delphi", "Denso"], []),
    p("Fuel Injector", "Engine", "84099199", 28.0, ["Bosch", "Delphi", "Denso"], ["injector"]),
    p("Turbocharger", "Engine", "84148090", 28.0, ["Garrett", "BorgWarner", "Honeywell"], ["turbo"]),
    p("Engine Mounting", "Engine", "87089900", 28.0, ["Rane", "Sai Deepa", "Avon"], ["engine mount"]),
    p("Cylinder Head", "Engine", "84099990", 28.0, [], ["head assembly"]),
    p("Crankshaft", "Engine", "84831099", 28.0, [], ["crank"]),
    p("Connecting Rod", "Engine", "84099990", 28.0, [], ["conrod", "con rod"]),
    p("Camshaft", "Engine", "84831099", 28.0, [], ["cam"]),
    p("Rocker Arm", "Engine", "84099199", 28.0, [], ["rocker"]),
    p("Engine Valve", "Engine", "84099990", 28.0, [], ["intake valve", "exhaust valve"]),
    p("Valve Stem Seal", "Engine", "84849000", 18.0, [], ["valve seal"]),
    # ---- Brakes ----
    p("Front Brake Pads", "Brakes", "87083000", 28.0, ["Bosch", "Brembo", "TVS Girling", "Ferodo"], ["brake pad"]),
    p("Rear Brake Pads", "Brakes", "87083000", 28.0, ["Bosch", "Brembo", "TVS Girling", "Ferodo"], ["brake pad"]),
    p("Brake Shoes", "Brakes", "87083000", 28.0, ["Bosch", "TVS Girling", "Ferodo"], ["brake shoe"]),
    p("Brake Disc / Rotor", "Brakes", "87083000", 28.0, ["Bosch", "Brembo"], ["rotor", "disc"]),
    p("Brake Drum", "Brakes", "87083000", 28.0, [], ["drum"]),
    p("Brake Master Cylinder", "Brakes", "87083000", 28.0, ["Bosch", "TVS Girling"], ["master cylinder"]),
    p("Brake Caliper", "Brakes", "87083000", 28.0, ["Bosch", "Brembo"], ["caliper"]),
    p("Brake Booster", "Brakes", "87083000", 28.0, ["Bosch", "TVS Girling"], ["booster"]),
    p("Wheel Cylinder", "Brakes", "87083000", 28.0, ["Bosch", "TVS Girling"], ["wheel cylinder"]),
    p("ABS / Wheel Speed Sensor", "Brakes", "90318000", 28.0, ["Bosch", "Continental"], ["abs sensor", "speed sensor"]),
    # ---- Suspension ----
    p("Front Shock Absorber", "Suspension", "87088000", 28.0, ["Gabriel", "Munjal Showa", "Endurance", "Duroshox"], ["front shock", "damper"]),
    p("Rear Shock Absorber", "Suspension", "87088000", 28.0, ["Gabriel", "Munjal Showa", "Endurance", "Duroshox"], ["rear shock", "damper"]),
    p("Strut Assembly", "Suspension", "87088000", 28.0, ["Gabriel", "Munjal Showa"], ["strut"]),
    p("Coil Spring", "Suspension", "73201011", 18.0, ["Jamna", "Mubea"], ["spring"]),
    p("Leaf Spring", "Suspension", "73201019", 18.0, ["Jamna"], ["leaf"]),
    p("Control Arm / Wishbone", "Suspension", "87089900", 28.0, ["Rane", "Sai Deepa"], ["lower arm", "wishbone"]),
    p("Ball Joint", "Suspension", "87089900", 28.0, ["Rane", "Sai Deepa"], ["lower ball joint"]),
    p("Tie Rod End", "Suspension", "87089400", 28.0, ["Rane", "Sai Deepa"], ["tie rod"]),
    p("Stabilizer Bar", "Suspension", "87088000", 28.0, [], ["anti-roll bar", "sway bar"]),
    p("Stabilizer Bar Bush", "Suspension", "87088000", 28.0, [], ["bush"]),
    p("Strut Mount Bearing", "Suspension", "84821090", 18.0, ["SKF", "FAG", "NBC"], ["strut bearing"]),
    p("Bump Stop", "Suspension", "87088000", 28.0, [], []),
    # ---- Steering ----
    p("Steering Rack", "Steering", "87089400", 28.0, ["Rane", "ZF", "Sona"], ["rack"]),
    p("Power Steering Pump", "Steering", "84136090", 28.0, ["ZF", "Rane", "Bosch"], ["ps pump"]),
    p("Steering Column", "Steering", "87089400", 28.0, [], ["column"]),
    p("Steering Knuckle", "Steering", "87089400", 28.0, [], ["knuckle"]),
    p("Tie Rod (Inner/Outer)", "Steering", "87089400", 28.0, ["Rane", "Sai Deepa"], ["tie rod"]),
    p("Steering Gear Box", "Steering", "87089400", 28.0, ["ZF", "Rane"], ["gear box"]),
    # ---- Clutch ----
    p("Clutch Plate", "Clutch", "87089300", 28.0, ["Valeo", "TVS", "Luk", "Gripwell"], ["clutch disc"]),
    p("Pressure Plate", "Clutch", "87089300", 28.0, ["Valeo", "TVS", "Luk", "Gripwell"], ["cover assembly"]),
    p("Clutch Kit", "Clutch", "87089300", 28.0, ["Valeo", "TVS", "Luk"], ["clutch set"]),
    p("Clutch Release Bearing", "Clutch", "84828000", 18.0, ["SKF", "NBC", "FAG"], ["release bearing", "throwout"]),
    p("Clutch Cable", "Clutch", "87089300", 28.0, [], ["clutch wire"]),
    p("Clutch Master Cylinder", "Clutch", "87089300", 28.0, ["Valeo", "TVS"], ["master cylinder"]),
    p("Clutch Slave Cylinder", "Clutch", "87089300", 28.0, ["Valeo", "TVS"], ["slave cylinder"]),
    p("Flywheel", "Clutch", "84835090", 28.0, [], []),
    # ---- Transmission ----
    p("Drive Shaft / Axle", "Transmission", "87085000", 28.0, ["GKN", "Sona"], ["cv axle", "half shaft"]),
    p("CV Joint", "Transmission", "87085000", 28.0, ["GKN", "Sona"], ["cv"]),
    p("CV Boot Kit", "Transmission", "40169990", 18.0, [], ["cv boot", "boot kit"]),
    p("Differential Assembly", "Transmission", "87085000", 28.0, [], ["diff"]),
    p("Axle Shaft", "Transmission", "87085000", 28.0, [], ["axle"]),
    p("Propeller Shaft", "Transmission", "87085000", 28.0, ["GKN", "Sona"], ["prop shaft"]),
    p("Universal Joint", "Transmission", "87089900", 28.0, [], ["u-joint"]),
    p("Gear Lever / Shift Assembly", "Transmission", "87084000", 28.0, [], ["gear shifter"]),
    # ---- Filters ----
    p("Oil Filter", "Filters", "84212300", 28.0, ["Bosch", "Purolator", "Mann", "Mahle"], ["engine oil filter"]),
    p("Air Filter", "Filters", "84213100", 28.0, ["Bosch", "Purolator", "Mann", "Mahle"], ["engine air filter"]),
    p("Fuel Filter", "Filters", "84212300", 28.0, ["Bosch", "Purolator", "Mahle"], ["petrol filter", "diesel filter"]),
    p("Cabin / AC Filter", "Filters", "84213100", 28.0, ["Bosch", "Mann", "Mahle"], ["pollen filter", "cabin filter"]),
    p("Transmission Filter", "Filters", "84212900", 28.0, ["Bosch", "Mahle"], ["atf filter"]),
    p("Hydraulic Filter", "Filters", "84212900", 28.0, [], []),
    p("Fuel Strainer", "Filters", "84212900", 28.0, [], ["strainer"]),
    # ---- Electrical ----
    p("Starter Motor", "Electrical", "85114000", 28.0, ["Bosch", "Lucas-TVS", "Valeo", "Denso"], ["self motor", "starter"]),
    p("Alternator", "Electrical", "85115000", 28.0, ["Bosch", "Lucas-TVS", "Valeo", "Denso"], ["dynamo"]),
    p("Wiper Motor", "Electrical", "85013119", 18.0, ["Bosch", "Valeo", "TVS"], ["wiper"]),
    p("Blower Motor", "Electrical", "85013100", 18.0, ["Valeo", "Subros"], ["blower"]),
    p("Radiator Fan Motor", "Electrical", "85013100", 18.0, ["Valeo", "Bosch"], ["fan motor"]),
    p("Fuse Box", "Electrical", "85361090", 18.0, [], ["fuse"]),
    p("Relay", "Electrical", "85364100", 18.0, ["Bosch", "Hella", "Minda"], ["relay"]),
    p("Horn", "Electrical", "85123010", 28.0, ["Roots", "Minda", "Bosch", "Hella"], []),
    p("Wiring Harness", "Electrical", "85443000", 28.0, ["Motherson", "Minda"], ["harness"]),
    p("ECU / Engine Control Module", "Electrical", "90328990", 28.0, ["Bosch", "Continental", "Denso"], ["ecu", "ecm"]),
    p("Battery Cable", "Electrical", "85443000", 28.0, [], ["battery terminal cable"]),
    p("Central Locking Motor", "Electrical", "85013100", 18.0, ["Minda", "Autocop"], ["lock motor"]),
    # ---- Lighting ----
    p("Headlamp Assembly", "Lighting", "85122010", 28.0, ["Lumax", "Varroc", "Minda", "Hella"], ["headlight"]),
    p("Tail Lamp Assembly", "Lighting", "85122020", 28.0, ["Lumax", "Varroc", "Minda", "Hella"], ["tail light"]),
    p("Fog Lamp", "Lighting", "85122020", 28.0, ["Lumax", "Varroc", "Hella"], ["fog light"]),
    p("Indicator / Turn Signal Lamp", "Lighting", "85122020", 28.0, ["Lumax", "Varroc"], ["indicator", "blinker"]),
    p("Headlamp Bulb", "Lighting", "85392100", 28.0, ["Osram", "Philips", "Hella"], ["bulb", "h4", "h7"]),
    p("LED Bulb", "Lighting", "85395000", 28.0, ["Osram", "Philips"], ["led"]),
    p("Side Mirror Indicator", "Lighting", "85122020", 28.0, ["Lumax", "Varroc"], []),
    p("Number Plate Lamp", "Lighting", "85122020", 28.0, ["Lumax"], []),
    # ---- Batteries ----
    p("Car Battery (12V)", "Batteries", "85071000", 28.0, ["Exide", "Amaron", "Livguard", "Tata Green"], ["battery"]),
    p("Bike Battery", "Batteries", "85071000", 28.0, ["Exide", "Amaron", "Livguard"], ["battery"]),
    p("Battery Terminal", "Batteries", "85369090", 18.0, [], ["terminal"]),
    p("Battery Charger", "Batteries", "85044010", 18.0, [], ["charger"]),
    # ---- Ignition ----
    p("Ignition Coil", "Ignition", "85113000", 28.0, ["Bosch", "NGK", "Denso"], ["coil pack"]),
    p("Spark Plug", "Ignition", "85111000", 28.0, ["Bosch", "NGK", "Denso"], ["plug"]),
    p("Glow Plug", "Ignition", "85111000", 28.0, ["Bosch", "NGK", "Denso"], []),
    p("Distributor Cap", "Ignition", "85119000", 28.0, ["Bosch", "Lucas-TVS"], ["distributor"]),
    p("Rotor Arm", "Ignition", "85119000", 28.0, ["Bosch", "Lucas-TVS"], ["rotor"]),
    p("Ignition Switch", "Ignition", "85365090", 18.0, ["Minda", "Lucas-TVS"], ["key switch"]),
    p("Spark Plug Wire / HT Lead", "Ignition", "85443000", 28.0, ["Bosch", "NGK"], ["ht lead", "plug wire"]),
    p("CDI Unit", "Ignition", "85119000", 28.0, ["Minda", "Bosch"], ["cdi"]),
    # ---- Cooling ----
    p("Radiator", "Cooling", "87089100", 28.0, ["Valeo", "Denso", "TVS"], []),
    p("Radiator Fan", "Cooling", "84145990", 28.0, ["Valeo", "Bosch"], ["fan"]),
    p("Thermostat", "Cooling", "90321090", 28.0, ["Mahle", "Gates"], []),
    p("Radiator Cap", "Cooling", "87089100", 28.0, [], ["radiator cap"]),
    p("Intercooler", "Cooling", "87089100", 28.0, ["Valeo"], []),
    p("Coolant Temperature Sensor", "Cooling", "90259000", 28.0, ["Bosch", "Denso"], ["cts", "temp sensor"]),
    p("Heater Core", "Cooling", "87089100", 28.0, ["Valeo", "Subros"], ["heater matrix"]),
    # ---- Air Conditioning ----
    p("AC Compressor", "Air Conditioning", "84143000", 28.0, ["Denso", "Subros", "Sanden"], ["compressor"]),
    p("AC Condenser", "Air Conditioning", "84159000", 28.0, ["Subros", "Denso"], ["condenser"]),
    p("AC Evaporator", "Air Conditioning", "84159000", 28.0, ["Subros", "Denso"], ["evaporator"]),
    p("AC Expansion Valve", "Air Conditioning", "84159000", 28.0, ["Denso", "Subros"], ["expansion valve"]),
    p("AC Refrigerant (R134a)", "Air Conditioning", "29034500", 18.0, [], ["gas", "refrigerant", "r134a"]),
    p("AC Hose", "Air Conditioning", "40093100", 18.0, [], ["ac pipe"]),
    # ---- Oils & Fluids ----
    p("Engine Oil", "Oils & Fluids", "27101990", 18.0, ["Castrol", "Shell", "Mobil", "Motul", "Gulf"], ["lubricant", "motor oil"]),
    p("Diesel Engine Oil", "Oils & Fluids", "27101990", 18.0, ["Castrol", "Shell", "Mobil"], ["diesel oil"]),
    p("Two-Wheeler Engine Oil", "Oils & Fluids", "27101990", 18.0, ["Motul", "Castrol", "Gulf"], ["bike oil"]),
    p("Gear Oil / Transmission Oil", "Oils & Fluids", "27101990", 18.0, ["Castrol", "Motul", "Valvoline"], ["gear oil", "transmission fluid"]),
    p("Brake Fluid (DOT 3/4)", "Oils & Fluids", "38190000", 18.0, ["Bosch", "Castrol", "Motul"], ["dot3", "dot4"]),
    p("Power Steering Fluid", "Oils & Fluids", "38190000", 18.0, ["Castrol", "Motul"], ["steering fluid"]),
    p("Coolant / Antifreeze", "Oils & Fluids", "38200000", 18.0, ["Castrol", "Shell", "Motul"], ["antifreeze"]),
    p("Grease", "Oils & Fluids", "27101990", 18.0, ["Castrol", "Shell", "Balmer Lawrie"], ["lithium grease"]),
    p("Windshield Washer Fluid", "Oils & Fluids", "34029099", 18.0, [], ["washer fluid"]),
    p("Penetrating Oil", "Oils & Fluids", "27101990", 18.0, [], ["wd40", "spray"]),
    # ---- Belts & Hoses ----
    p("Timing Belt", "Belts & Hoses", "40103990", 18.0, ["Gates", "Continental", "Bando"], ["cambelt"]),
    p("Serpentine / Fan Belt", "Belts & Hoses", "40103990", 18.0, ["Gates", "Continental", "Bando"], ["fan belt", "drive belt"]),
    p("AC Belt", "Belts & Hoses", "40103990", 18.0, ["Gates", "Bando"], ["compressor belt"]),
    p("Alternator Belt", "Belts & Hoses", "40103990", 18.0, ["Gates", "Bando"], []),
    p("Radiator Hose", "Belts & Hoses", "40093100", 18.0, ["Gates", "Radiant"], ["upper hose", "lower hose"]),
    p("Heater Hose", "Belts & Hoses", "40093100", 18.0, ["Gates"], []),
    p("Fuel Hose", "Belts & Hoses", "40091100", 18.0, ["Gates"], ["fuel line"]),
    p("Vacuum Hose", "Belts & Hoses", "40091100", 18.0, [], ["vacuum line"]),
    p("Brake Hose", "Belts & Hoses", "40093200", 18.0, ["Bosch", "Gates"], ["brake line"]),
    p("Power Steering Hose", "Belts & Hoses", "40093100", 18.0, ["Gates"], ["ps hose"]),
    # ---- Body Parts ----
    p("Front Bumper", "Body Parts", "87081090", 28.0, ["Minda", "Varroc"], ["bumper"]),
    p("Rear Bumper", "Body Parts", "87081090", 28.0, ["Minda", "Varroc"], ["bumper"]),
    p("Bonnet / Hood", "Body Parts", "87082900", 28.0, [], ["bonnet"]),
    p("Front Fender", "Body Parts", "87082900", 28.0, [], ["fender", "mudguard"]),
    p("Door Assembly", "Body Parts", "87082900", 28.0, [], ["door"]),
    p("Boot Lid / Tailgate", "Body Parts", "87082900", 28.0, [], ["dickey", "boot"]),
    p("Side Mirror", "Body Parts", "87082900", 28.0, ["Minda", "Varroc"], ["rvm", "orm"]),
    p("Door Handle", "Body Parts", "87082900", 28.0, [], ["handle"]),
    p("Grille", "Body Parts", "87082900", 28.0, [], ["grill"]),
    p("Mud Flap", "Body Parts", "87082900", 28.0, [], ["mudflap"]),
    p("Fuel Tank Cap", "Body Parts", "87089900", 28.0, [], ["fuel cap"]),
    p("Roof Rack", "Body Parts", "87082900", 28.0, [], ["carrier"]),
    p("Running Board / Foot Step", "Body Parts", "87082900", 28.0, [], ["footstep", "side step"]),
    # ---- Glass ----
    p("Front Windshield", "Glass", "70072190", 28.0, ["Asahi India", "Saint-Gobain Sekurit"], ["windscreen", "windshield"]),
    p("Rear Windshield", "Glass", "70072190", 28.0, ["Asahi India", "Saint-Gobain Sekurit"], ["rear glass"]),
    p("Door Glass", "Glass", "70072190", 28.0, ["Asahi India"], ["window glass"]),
    p("Quarter Glass", "Glass", "70072190", 28.0, [], []),
    p("Windshield Wiper Blade", "Glass", "85124000", 28.0, ["Bosch", "Hella", "Valeo"], ["wiper blade"]),
    p("Windshield Wiper Arm", "Glass", "85124000", 28.0, ["Bosch", "Hella"], ["wiper arm"]),
    # ---- Exhaust ----
    p("Silencer / Muffler", "Exhaust", "87089200", 28.0, ["Minda", "Bosal"], ["muffler"]),
    p("Exhaust Pipe", "Exhaust", "87089200", 28.0, [], ["downpipe"]),
    p("Catalytic Converter", "Exhaust", "84213990", 28.0, [], ["cat converter"]),
    p("Oxygen Sensor", "Exhaust", "90271000", 28.0, ["Bosch", "Denso"], ["o2 sensor", "lambda sensor"]),
    p("Exhaust Manifold", "Exhaust", "87089200", 28.0, [], ["manifold"]),
    p("Exhaust Mounting Rubber", "Exhaust", "87089200", 28.0, [], ["exhaust mount"]),
    p("Tail Pipe Tip", "Exhaust", "87089200", 28.0, [], ["tip"]),
    # ---- Wheels & Hubs ----
    p("Wheel Bearing", "Wheels & Hubs", "84821090", 18.0, ["SKF", "FAG", "Timken", "NBC"], ["hub bearing"]),
    p("Hub Assembly", "Wheels & Hubs", "87087000", 28.0, [], ["hub"]),
    p("Wheel Rim / Alloy", "Wheels & Hubs", "87087000", 28.0, [], ["rim", "alloy wheel"]),
    p("Wheel Nut / Bolt", "Wheels & Hubs", "73181500", 18.0, [], ["lug nut"]),
    p("Wheel Hub Cap", "Wheels & Hubs", "87087000", 28.0, [], ["hub cap"]),
    p("TPMS Sensor", "Wheels & Hubs", "90262000", 28.0, ["Continental", "Schrader"], ["tpms"]),
    # ---- Bearings & Seals ----
    p("Ball Bearing", "Bearings & Seals", "84821090", 18.0, ["SKF", "FAG", "Timken", "NBC"], ["bearing"]),
    p("Tapered Roller Bearing", "Bearings & Seals", "84822000", 18.0, ["SKF", "Timken", "NBC"], ["taper bearing"]),
    p("Clutch Release Bearing", "Bearings & Seals", "84828000", 18.0, ["SKF", "NBC", "FAG"], ["release bearing"]),
    p("Oil Seal", "Bearings & Seals", "84879000", 18.0, ["NOK", "SKF", "Rane"], ["crankshaft seal", "axle seal", "oil seal"]),
    p("O-Ring Kit", "Bearings & Seals", "40169390", 18.0, [], ["o ring"]),
    p("General Gasket", "Bearings & Seals", "84841090", 18.0, [], ["gasket"]),
    # ---- Accessories ----
    p("Car Floor Mat", "Accessories", "40169100", 18.0, [], ["mat", "foot mat"]),
    p("Seat Cover", "Accessories", "87089900", 28.0, [], ["seat cover"]),
    p("Steering Wheel Cover", "Accessories", "87089900", 28.0, [], ["steering cover"]),
    p("Car Perfume", "Accessories", "33074900", 18.0, ["Ambipur", "Godrej"], ["perfume", "freshener"]),
    p("Mobile Holder", "Accessories", "39269099", 18.0, [], ["phone holder"]),
    p("Dash Cam", "Accessories", "85258900", 18.0, [], ["dashcam"]),
    p("Parking Sensor", "Accessories", "90318000", 28.0, [], ["reverse sensor"]),
    p("Reverse Camera", "Accessories", "85258900", 18.0, [], ["back camera"]),
    p("Seat Belt", "Accessories", "87082100", 28.0, [], ["seatbelt"]),
    p("Sun Visor", "Accessories", "87082900", 28.0, [], ["visor"]),
    p("Car Cover", "Accessories", "63079090", 18.0, [], ["cover"]),
    p("Windshield Sunshade", "Accessories", "39269099", 18.0, [], ["sunshade"]),
    p("Key Fob / Remote", "Accessories", "85269200", 18.0, ["Minda", "Autocop"], ["remote key"]),
    # ---- Tools ----
    p("Hydraulic Jack", "Tools", "84254200", 18.0, [], ["jack"]),
    p("Wheel Wrench / Spanner", "Tools", "82041110", 18.0, [], ["lug wrench"]),
    p("Screwdriver Set", "Tools", "82054000", 18.0, [], ["screwdriver"]),
    p("Pliers", "Tools", "82032000", 18.0, [], []),
    p("Socket Set", "Tools", "82042000", 18.0, [], ["socket"]),
    p("Torque Wrench", "Tools", "82041120", 18.0, [], []),
    p("Portable Air Compressor", "Tools", "84148090", 28.0, [], ["inflator", "compressor"]),
    p("Oil Filter Wrench", "Tools", "82055990", 18.0, [], []),
    p("Multimeter", "Tools", "90303100", 18.0, [], []),
    p("Jump Starter Cable", "Tools", "85444290", 18.0, [], ["jumper cable"]),
    p("Work Light", "Tools", "94054090", 18.0, [], ["inspection lamp"]),
]


def validate():
    category_names = {c["name"] for c in CATEGORIES}
    for part in PARTS:
        assert part["category"] in category_names, f"Unknown category {part['category']!r} in {part['name']!r}"
    for veh in VEHICLES:
        assert veh["yearFrom"] <= veh["yearTo"], f"Bad year range for {veh['make']} {veh['model']}"
        assert veh["fuelTypes"], f"No fuels for {veh['make']} {veh['model']}"
    print(f"OK: {len(VEHICLES)} vehicles, {len(CATEGORIES)} categories, {len(PARTS)} parts")


def write(name, data):
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"wrote {os.path.relpath(path, os.path.join(HERE, '..'))}")


def main():
    validate()
    vehicles = sorted(VEHICLES, key=lambda x: (x["make"].lower(), x["model"].lower(), x["yearFrom"]))
    write("vehicles.json", vehicles)
    write("categories.json", CATEGORIES)
    write("parts_reference.json", PARTS)


if __name__ == "__main__":
    main()
