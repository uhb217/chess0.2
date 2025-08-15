package net.uhb217.chess02

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import net.uhb217.chess02.ui.LeaderboardPlayerStatsItem

class LeaderboardFragment : Fragment() {
    private lateinit var playersInfo: List<PlayerStats>
    private lateinit var leaderboardContainer: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playersInfo = listOf(
            PlayerStats("Alice", 1650, 25, 10, 5),
            PlayerStats("Bob", 1580, 20, 12, 8),
            PlayerStats("Charlie", 1550, 18, 15, 7),
            PlayerStats("David", 1700, 30, 5, 3),
            PlayerStats("Eve", 1600, 22, 10, 8),
            PlayerStats("Frank", 1520, 15, 18, 6),
            PlayerStats("Grace", 1680, 28, 8, 4),
            PlayerStats("Hank", 1750, 35, 2, 2),
            PlayerStats("Ivy", 1620, 23, 12, 6),
            PlayerStats("Jack", 1570, 19, 14, 7),
            PlayerStats("Kelly", 1690, 27, 6, 4),
            PlayerStats("Leo", 1720, 32, 3, 2),
            PlayerStats("Mia", 1590, 21, 11, 7),
            PlayerStats("Nate", 1630, 24, 9, 5)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
         inflater.inflate(R.layout.fragment_leaderboard, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboardContainer = requireView().findViewById(R.id.leaderboard_container)

        playersInfo.forEach { player ->
            leaderboardContainer.addView(LeaderboardPlayerStatsItem(context,player))
        }
    }
    data class PlayerStats(
        val name: String,
        val rating: Int,
        val wins: Int,
        val losses: Int,
        val draws: Int
    )


}