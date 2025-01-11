package com.example.csci4176project.components
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

//api of frankfurter, a free open source currency convert api
class CurrencyConverter {

    companion object{
        // host
        private val host = "api.frankfurter.app"

        // convert method, return the to currency value
        fun convert(amount: Double, fromCurrency: String, toCurrency: String = "USD"): Double {
            return try {
                if(fromCurrency == "USD" && toCurrency == "USD") return amount
                if (amount <= 0.0) return 0.0
                // get url
                val url = URL("https://$host/latest?amount=$amount&from=$fromCurrency&to=$toCurrency")
                // response
                val response = runBlocking {
                    withContext(Dispatchers.IO) {
                        url.readText()
                    }
                }
                // response to json
                val jsonResponse = JSONObject(response)
                // get the toCurrency amount
                val convertedAmount = jsonResponse.getJSONObject("rates").getDouble(toCurrency)

                convertedAmount
            } catch (e: Exception) {
                Log.e("CurrencyConverter", "Error converting currency", e)
                -1.0
            }
        }
    }
}

// use case
//val convertedAmount : Double = CurrencyConverter(100.0, "EUR", "USD").convert()