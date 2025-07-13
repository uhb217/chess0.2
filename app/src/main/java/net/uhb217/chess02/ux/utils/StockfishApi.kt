package net.uhb217.chess02.ux.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder


object StockfishApi {

    private val client = OkHttpClient()

    //    private const val API_URL = "https://chess-api.com/v1"
    private const val API_URL = "https://stockfish.online/api/s/v2.php"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    fun playBestMove(fen: String, depth: Int) {
        playBestMove(0, fen, depth)
    }

    fun playBestMove(count: Int, fen: String, depth: Int) {
        if (count > 10) throw RuntimeException("Too many requests")
        // Create the request
        val encodedFEN = URLEncoder.encode(fen, "UTF-8")
        val url = "$API_URL?fen=$encodedFEN&depth=$depth"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // Execute asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Stockfish", "API call failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("Stockfish", "Unexpected code $it")
                        return
                    }
                    val json = JSONObject(it.body.string())
                    // Log the response for debugging
                    Log.d("Stockfish", "Response: $json")

                    // Check if the required fields exist
                    if (!json.has("success")) {
                        Log.e(
                            "Stockfish", "Missing required fields in response: $json \n" +
                                    "fen: $fen"
                        )
                        return
                    }
                    if (json.getString("success") != "true") {
                        Log.e("Stockfish", "API call failed: $json \n" + "fen: $fen")
                        playBestMove(count + 1, fen, depth)
                        return
                    }
                    if (!json.has("bestmove")) {
                        Log.e("Stockfish", "Missing required fields in response: $json \n" +
                                "fen: $fen")
                        return
                    }
                    val bestMove = json.getString("bestmove").substring(9, 14).trimEnd()
                    Handler(Looper.getMainLooper()).post {
                        BoardUtils.playMove(BoardUtils.UCI2Move(bestMove))
                    }
                }
            }
        })
    }

//    fun playBestMove(fen: String, depth: Int) {
//        val jsonBody = """
//            {
//              "fen": "$fen",
//              "depth": $depth
//            }
//        """.trimIndent()
//        val requestBody = jsonBody.toRequestBody(JSON)
//
//        val request = Request.Builder()
//            .url(API_URL)
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("Stockfish", e.message.toString())
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!it.isSuccessful) {
//                        Log.e("Stockfish", "Unexpected code $it")
//                        return
//                    }
//                    val json = JSONObject(it.body.string())
//                    // Log the response for debugging
//                    Log.d("Stockfish", "Response: $json")
//
//                    // Check if the required fields exist
//                    if (!json.has("move")) {
//                        Log.e("Stockfish", "Missing required fields in response: $json \n" +
//                                "fen: $fen")
//                        return
//                    }
//
//                    val bestMove = json.getString("move")
//
//                    Handler(Looper.getMainLooper()).post {
//                        BoardUtils.playMove(BoardUtils.UCI2Move(bestMove))
//                    }
//                }
//            }
//        })
//    }
}