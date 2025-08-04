package net.uhb217.chess02.ui

import android.app.Activity
import android.view.View
import net.uhb217.chess02.R
import net.uhb217.chess02.ux.Board
import net.uhb217.chess02.ux.utils.MoveHistory

object BottomGameControls {
    fun backBtn(activity: Activity): View = activity.findViewById(R.id.back_btn)
    fun forwardBtn(activity: Activity): View = activity.findViewById(R.id.forward_btn)
    fun resignBtn(activity: Activity): View = activity.findViewById(R.id.resign_btn)
    fun drawBtn(activity: Activity): View = activity.findViewById(R.id.draw_btn)

    fun setup(activity: Activity, withDrawOption: Boolean = true) {
        val board = Board.getInstance()
        backBtn(activity).setOnClickListener { board.fromFEN(MoveHistory.moveBack()) }
        forwardBtn(activity).setOnClickListener { board.fromFEN(MoveHistory.moveForward()) }
        resignBtn(activity).setOnClickListener { board.resign() }

        if (withDrawOption)
            drawBtn(activity).setOnClickListener { }
    }

    fun disable(activity: Activity) {
        resignBtn(activity).isEnabled = false
        drawBtn(activity).isEnabled = false
    }
}