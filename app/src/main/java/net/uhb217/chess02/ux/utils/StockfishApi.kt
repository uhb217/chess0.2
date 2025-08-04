package net.uhb217.chess02.ux.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit


object StockfishApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private const val API_URL = "https://stockfish-api-osfa.onrender.com/evaluate"
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun playBestMove(fen: String, depth: Int, waitAnimation: LottieAnimationView) {
        Handler(Looper.getMainLooper()).post {
            waitAnimation.playAnimation()
        }
        // Create the request
        val realDepth = if (MoveHistory.length <= 5) MoveHistory.length * 2 else depth
        val jsonBody = """
            {
              "fen": "$fen",
              "depth": $realDepth
            }
        """.trimIndent()
        val requestBody = jsonBody.toRequestBody(JSON)

        val request = Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .build()

        // Execute asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    waitAnimation.cancelAnimation()
                    waitAnimation.progress = 0f
                }
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

                    if (!json.has("bestmove")) {
                        Log.e(
                            "Stockfish", "Missing required fields in response: $json \n" +
                                    "fen: $fen"
                        )
                        return
                    }
                    Handler(Looper.getMainLooper()).post {
                        waitAnimation.cancelAnimation()
                        waitAnimation.progress = 0f
                        BoardUtils.playMove(BoardUtils.UCI2Move(json.getString("bestmove")))
                    }
                    Handler(Looper.getMainLooper()).post {
                    }
                }
            }
        })
    }
}