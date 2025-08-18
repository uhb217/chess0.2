package net.uhb217.chess02.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import net.uhb217.chess02.R
import net.uhb217.chess02.RoomFragment
import net.uhb217.chess02.ui.Dialogs.getStockfishElo
import net.uhb217.chess02.ux.Player
import net.uhb217.chess02.ux.Player.PlayerCallback
import net.uhb217.chess02.ux.utils.Color

object Dialogs {

    fun showPromotionDialog(ctx: Context, color: Color, callback: PromotionCallback) {
        val dialog = TransparentDialog(ctx)
        dialog.setContentView(R.layout.dialog_promotion)
        dialog.setCancelable(false)

        val prefix = if (color == Color.WHITE) "w" else "b"

        val queen = dialog.findViewById<ImageButton>(R.id.promoQueen)
        val rook = dialog.findViewById<ImageButton>(R.id.promoRook)
        val bishop = dialog.findViewById<ImageButton>(R.id.promoBishop)
        val knight = dialog.findViewById<ImageButton>(R.id.promoKnight)

        queen.setImageResource(getDrawableId(ctx, "${prefix}q"))
        rook.setImageResource(getDrawableId(ctx, "${prefix}r"))
        bishop.setImageResource(getDrawableId(ctx, "${prefix}b"))
        knight.setImageResource(getDrawableId(ctx, "${prefix}n"))

        queen.setOnClickListener {
            callback.onPieceChosen('q')
            dialog.dismiss()
        }

        rook.setOnClickListener {
            callback.onPieceChosen('r')
            dialog.dismiss()
        }

        bishop.setOnClickListener {
            callback.onPieceChosen('b')
            dialog.dismiss()
        }

        knight.setOnClickListener {
            callback.onPieceChosen('n')
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getDrawableId(ctx: Context, name: String): Int {
        return ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
    }

    fun showGameOverDialog(ctx: Context, winner: Color?, reason: String) {
        val dialog = TransparentDialog(ctx)
        dialog.setContentView(R.layout.dialog_game_over)
        dialog.setCancelable(false)

        val resultText = dialog.findViewById<TextView>(R.id.textResult)
        val reasonText = dialog.findViewById<TextView>(R.id.textReason)
        val btnSeePosition = dialog.findViewById<Button>(R.id.btnRematch)
        val btnExit = dialog.findViewById<Button>(R.id.btnExit)

        val backBtn = (ctx as Activity).findViewById<Button>(R.id.go_back_btn)
        backBtn.visibility = View.VISIBLE
        backBtn.setOnClickListener { btnExit.performClick() }

        if (winner == null) {
            resultText.text = "Draw!"
            reasonText.text = "by agreement"
        } else {
            resultText.text = "${winner.name} wins!"
            reasonText.text = "by $reason"
        }

        btnSeePosition.setOnClickListener { dialog.dismiss() }

        btnExit.setOnClickListener {
            dialog.dismiss()
            ctx.finish()
        }

        dialog.show()
    }

    private var waitingDialog: AlertDialog? = null

    fun showWaitingDialog(ctx: Context, roomId: String, onCancel: Runnable): Dialog {
        val dialog = TransparentDialog(ctx)
        val view = LayoutInflater.from(ctx).inflate(R.layout.waiting_dialog, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        val roomIdText = view.findViewById<TextView>(R.id.roomIdText)
        roomIdText.text = "Room ID: $roomId"

        val statusText = view.findViewById<TextView>(R.id.statusText)
        val blinkAnimation = AnimationUtils.loadAnimation(ctx, R.anim.blink)
        statusText.startAnimation(blinkAnimation)

        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            statusText.clearAnimation()
            dialog.dismiss()
            onCancel.run()
        }

        dialog.show()
        return dialog
    }


    fun dismissWaitingDialog() {
        waitingDialog?.takeIf { it.isShowing }?.dismiss()
    }

    fun loginWaitingDialog(ctx: Context): Dialog {
        val dialog = TransparentDialog(ctx)
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

    fun drawOfferDialog(ctx: Context, db: DatabaseReference) {
        val dialog = TransparentDialog(ctx)
        dialog.setContentView(R.layout.dialog_draw_offer)
        dialog.setCancelable(false)

        val btnAgree = dialog.findViewById<ImageButton>(R.id.btn_draw_agree)
        val btnDisagree = dialog.findViewById<ImageButton>(R.id.btn_draw_disagree)

        btnAgree.setOnClickListener {
            db.child("draw").setValue(2)
            dialog.dismiss()
        }
        btnDisagree.setOnClickListener {
            db.child("draw").setValue(3)
            dialog.dismiss()
        }
        dialog.show()
    }

    private var waitForDrawResponse: Dialog? = null

    fun waitForDrawResponseDialog(ctx: Context) {
        waitForDrawResponse = TransparentDialog(ctx).apply {
            setContentView(R.layout.dialog_draw_offer_sent)
            setCancelable(false)
            show()
        }
    }

    fun dismissWaitForDrawResponseDialog() {
        waitForDrawResponse?.dismiss()
    }
    private var stockfishElo: Int = 15
    fun stockfishDialog(ctx: Context){
        val dialog = TransparentDialog(ctx)
        dialog.setContentView(R.layout.dialog_stockfish_conf)

        dialog.findViewById<Button>(R.id.start_game).setOnClickListener {
            val isWhite = dialog.findViewById<RadioButton>(R.id.white_choice_radio).isChecked

            Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser(), { player ->
                player.color = if (isWhite) Color.WHITE else Color.BLACK
                RoomFragment.startGameActivity(ctx, getStockfishElo().toString(), player
                    ,Player.Stockfish(getStockfishElo(), player.color.opposite()))
            })
//            Player.fromFirebaseUser(
//                FirebaseAuth.getInstance().getCurrentUser(),
//                PlayerCallback { player: Player? ->
//                    startGameActivity(
//                        getStockfishElo().toString(), player!!.setColor(Color.WHITE),
//                        Player.Stockfish(getStockfishElo())
//                    )
//                })
//            Player.fromFirebaseUser(FirebaseAuth.getInstance().getCurrentUser()
//            , player -> startGameActivity(String.valueOf(Dialogs.INSTANCE.getStockfishElo()), player.setColor(Color.WHITE)
//                , Player.Stockfish(Dialogs.INSTANCE.getStockfishElo())))
            dialog.dismiss()
        }

        val seekBar: SeekBar = dialog.findViewById(R.id.seekbar_stockfish_elo)
        val eloView: TextView = dialog.findViewById(R.id.stockfish_elo_value)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, p2: Boolean) {
                eloView.text = value.toString()
                stockfishElo = value
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        dialog.show()
    }
    fun getStockfishElo(): Int{
        return stockfishElo
    }
    interface PromotionCallback {
        fun onPieceChosen(piece: Char)
    }

    class TransparentDialog(ctx: Context) : Dialog(ctx) {
        init {
            window?.setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
        }
    }
}
