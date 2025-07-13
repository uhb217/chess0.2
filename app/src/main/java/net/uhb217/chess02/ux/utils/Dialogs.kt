package net.uhb217.chess02.ux.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import net.uhb217.chess02.R
import net.uhb217.chess02.RoomActivity


object Dialogs {
    fun showPromotionDialog(ctx: Context, color: Color, callback: PromotionCallback) {
        val dialog = Dialog(ctx)
        dialog.setContentView(R.layout.dialog_promotion)
        dialog.setCancelable(false)

        val prefix = if (color == Color.WHITE) "w" else "b"

        val queen = dialog.findViewById<ImageButton>(R.id.promoQueen)
        val rook = dialog.findViewById<ImageButton>(R.id.promoRook)
        val bishop = dialog.findViewById<ImageButton>(R.id.promoBishop)
        val knight = dialog.findViewById<ImageButton>(R.id.promoKnight)

        // Dynamically load the correct drawable
        queen.setImageResource(getDrawableId(ctx, prefix + "q"))
        rook.setImageResource(getDrawableId(ctx, prefix + "r"))
        bishop.setImageResource(getDrawableId(ctx, prefix + "b"))
        knight.setImageResource(getDrawableId(ctx, prefix + "n"))

        queen.setOnClickListener { v: View ->
            callback.onPieceChosen('q')
            dialog.dismiss()
        }

        rook.setOnClickListener { v: View ->
            callback.onPieceChosen('r')
            dialog.dismiss()
        }

        bishop.setOnClickListener { v: View ->
            callback.onPieceChosen('b')
            dialog.dismiss()
        }

        knight.setOnClickListener { v: View ->
            callback.onPieceChosen('n')
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getDrawableId(ctx: Context, name: String): Int {
        return ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
    }

    fun showGameOverDialog(ctx: Context, winner: Color, reason: String) {
        val dialog = Dialog(ctx)
        dialog.setContentView(R.layout.dialog_game_over)
        dialog.setCancelable(false)

        val resultText = dialog.findViewById<TextView>(R.id.textResult)
        val reasonText = dialog.findViewById<TextView>(R.id.textReason)
        val btnSeePosition = dialog.findViewById<Button>(R.id.btnRematch)
        val btnExit = dialog.findViewById<Button>(R.id.btnExit)

        val backBtn = (ctx as Activity).findViewById<Button>(R.id.go_back_btn)
        backBtn.visibility = View.VISIBLE
        backBtn.setOnClickListener { btnExit.performClick() }

        resultText.text = "${winner.name} wins!"
        reasonText.text = "by $reason"

        btnSeePosition.setOnClickListener { v: View -> dialog.dismiss() }

        btnExit.setOnClickListener { v: View ->
            dialog.dismiss()
            ctx.startActivity(
                Intent(ctx, RoomActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        dialog.show()
    }

    private var waitingDialog: AlertDialog? = null

    fun showWaitingDialog(ctx: Context?, roomId: String, onCancel: Runnable): Dialog {
        val builder = AlertDialog.Builder(ctx)
        val view = LayoutInflater.from(ctx).inflate(R.layout.waiting_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)

        val roomIdText = view.findViewById<TextView>(R.id.roomIdText)
        roomIdText.text = "Room ID: " + roomId
        roomIdText.setFocusable(false)
        roomIdText.isFocusableInTouchMode = false

        waitingDialog = builder.create()
        waitingDialog!!.show()

        val statusText = view.findViewById<TextView>(R.id.statusText)
        val blinkAnimation = AnimationUtils.loadAnimation(ctx, R.anim.blink)
        statusText.startAnimation(blinkAnimation)


        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener { v: View? ->
            statusText.clearAnimation()
            if (waitingDialog != null && waitingDialog!!.isShowing) {
                waitingDialog!!.dismiss()
            }
            onCancel.run()
        }
        return waitingDialog!!
    }

    fun dismissWaitingDialog() {
        if (waitingDialog != null && waitingDialog!!.isShowing) {
            waitingDialog!!.dismiss()
        }
    }

    fun loginWaitingDialog(ctx: Context): Dialog {
        val dialog = Dialog(ctx)
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.login_waiting_dialog, null)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        return dialog
    }

    fun signupWaitingDialog(ctx: Context): Dialog {
        val dialog = loginWaitingDialog(ctx)
        val statusText = dialog.findViewById<TextView>(R.id.statusText)
        statusText.text = "Creating account..."
        return dialog
    }

    interface PromotionCallback {
        fun onPieceChosen(piece: Char)
    }
}