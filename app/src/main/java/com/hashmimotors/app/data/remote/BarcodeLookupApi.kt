package com.hashmimotors.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * A product identified from a barcode using free, no-key web catalogs.
 * Note: barcodes only encode a number, so lookup depends on a public database.
 */
data class OnlineProduct(
    val barcode: String,
    val name: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val source: String = "Online catalog"
)

/**
 * Best-effort online barcode identification. Falls back gracefully to null
 * (never throws) so the app keeps working offline.
 */
object BarcodeLookupApi {

    suspend fun lookup(barcode: String): OnlineProduct? = withContext(Dispatchers.IO) {
        // Try UPCitemdb first (broadest coverage), then Open Food Facts.
        lookupUpcItemDb(barcode) ?: lookupOpenFoodFacts(barcode)
    }

    private fun lookupUpcItemDb(barcode: String): OnlineProduct? {
        return try {
            val url = "https://api.upcitemdb.com/prod/trial/lookup?upc=${barcode}"
            val json = getJson(url) ?: return null
            if (json.optString("code") != "OK") return null
            val item = json.optJSONArray("items")?.optJSONObject(0) ?: return null
            val name = item.optString("title").ifBlank { null }
            if (name == null) return null
            val offers = item.optJSONArray("offers")
            val price = offers?.let { arr ->
                val p = arr.optJSONObject(0)?.optDouble("price")
                if (p != null && !p.isNaN() && p > 0) p else null
            }
            val image = item.optJSONArray("images")?.optString(0)?.ifBlank { null }
            OnlineProduct(
                barcode = barcode,
                name = name,
                brand = item.optString("brand").ifBlank { null },
                category = item.optString("category").ifBlank { null },
                price = price,
                imageUrl = image,
                source = "UPCitemdb"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun lookupOpenFoodFacts(barcode: String): OnlineProduct? {
        return try {
            val url = "https://world.openfoodfacts.org/api/v2/product/${barcode}.json?fields=code,product_name,brands,categories,image_url"
            val json = getJson(url) ?: return null
            if (json.optInt("status") != 1) return null
            val product = json.optJSONObject("product") ?: return null
            val name = product.optString("product_name").ifBlank { null }
            if (name == null) return null
            OnlineProduct(
                barcode = barcode,
                name = name,
                brand = product.optString("brands").ifBlank { null }?.take(60),
                category = product.optString("categories").ifBlank { null }
                    ?.substringBefore(',')?.trim(),
                imageUrl = product.optString("image_url").ifBlank { null },
                source = "Open Food Facts"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getJson(urlString: String): JSONObject? {
        return try {
            val conn = URL(urlString).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty(
                "User-Agent",
                "HashmiMotorsPremium/1.0 (personal inventory app)"
            )
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return null
            }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            JSONObject(text)
        } catch (e: Exception) {
            null
        }
    }
}
