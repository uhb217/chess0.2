package net.uhb217.chess02.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import net.uhb217.chess02.LeaderboardFragment
import net.uhb217.chess02.R

class LeaderboardPlayerStatsItem(context: Context?, stats: LeaderboardFragment.PlayerStats) : LinearLayout(context) {
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
        setBackgroundResource(R.drawable.player_info_bg)

        // Player icon
        playerIcon = ImageView(context).apply {
            layoutParams = LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
                marginEnd = 12.dpToPx()
            }
            setBackgroundResource(R.drawable.circle_bg)
            clipToOutline = true
            scaleType = ImageView.ScaleType.CENTER_CROP
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

