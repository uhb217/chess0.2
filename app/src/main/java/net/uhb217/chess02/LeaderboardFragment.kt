package net.uhb217.chess02

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import net.uhb217.chess02.ux.utils.FirebaseUtils

class LeaderboardFragment : Fragment() {
    private var playersInfo: MutableList<PlayerStats> = mutableListOf()
    private lateinit var leaderboardContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseUtils.fetchPlayersData { snapshot ->
            for (statsSnapshot in snapshot.children) {
                val player = statsSnapshot.getValue(PlayerStats::class.java)
                if (player != null) {
                    player.name = statsSnapshot.key ?: ""
                    playersInfo.add(player)
                }
            }
            activity?.runOnUiThread { updateLeaderboard() }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
         inflater.inflate(R.layout.fragment_leaderboard, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboardContainer = requireView().findViewById(R.id.leaderboard_container)
        updateLeaderboard()
    }
    private fun updateLeaderboard() {
        if (!::leaderboardContainer.isInitialized) return
        leaderboardContainer.removeAllViews()
        playersInfo.forEach { player ->
            leaderboardContainer.addView(LeaderboardPlayerStatsItem(context, player))
        }
    }

    class LeaderboardPlayerStatsItem(context: Context?, stats: PlayerStats) : LinearLayout(context) {
        private val playerIcon: ImageView
        private val playerName: TextView
        private val playerRating: TextView
        private val playerWins: TextView
        private val playerLosses: TextView
        private val playerDraws: TextView

        init {
            // Root layout setup
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 16.dpToPx()
                marginEnd = 16.dpToPx()
                topMargin = 4.dpToPx()
                bottomMargin = 4.dpToPx()
            }
            setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
            setBackgroundResource(if (stats.name == FirebaseAuth.getInstance().currentUser?.displayName) R.drawable.bright_time_bg else R.drawable.player_info_bg)

            // Player icon
            playerIcon = ImageView(context).apply {
                layoutParams = LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                    marginEnd = 12.dpToPx()
                }
                setBackgroundResource(R.drawable.squircle_bg)
                clipToOutline = true
                scaleType = ImageView.ScaleType.CENTER_CROP

                if (stats.base64encodedIcon != null) {
                    val decodedString: ByteArray = Base64.decode(stats.base64encodedIcon, Base64.DEFAULT)
                    setImageBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size))
                }else
                    setImageResource(R.drawable.ic_player)
            }
            addView(playerIcon)

            // Center container for name and rating
            val centerContainer = LinearLayout(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                orientation = VERTICAL
            }

            // Player name
            playerName = TextView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                text = stats.name
                setTextColor(Color.WHITE)
                textSize = 16f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            centerContainer.addView(playerName)

            // Player rating
            playerRating = TextView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                text = "Rating: ${stats.rating}"
                setTextColor(Color.WHITE)
                textSize = 12f
            }
            centerContainer.addView(playerRating)
            addView(centerContainer)

            // Stats container
            val statsContainer = LinearLayout(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                orientation = VERTICAL
                gravity = Gravity.END
            }

            // Wins text
            playerWins = TextView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                text = "W: ${stats.wins}"
                setTextColor(Color.parseColor("#4CAF50"))
                textSize = 12f
            }
            statsContainer.addView(playerWins)

            // Losses text
            playerLosses = TextView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                text = "L: ${stats.losses}"
                setTextColor(Color.parseColor("#F44336"))
                textSize = 12f
            }
            statsContainer.addView(playerLosses)

            // Draws text
            playerDraws = TextView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                text = "D: ${stats.draws}"
                setTextColor(Color.parseColor("#9E9E9E"))
                textSize = 12f
            }
            statsContainer.addView(playerDraws)
            addView(statsContainer)
        }

        private fun Int.dpToPx(): Int =
            (this * context.resources.displayMetrics.density).toInt()
    }
    data class PlayerStats(
        var name: String = "",
        val rating: Int = 1200,
        val base64encodedIcon: String? = null,
        val wins: Int = 0,
        val losses: Int = 0,
        val draws: Int = 0
    )
}