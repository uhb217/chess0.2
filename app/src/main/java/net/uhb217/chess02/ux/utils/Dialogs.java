package net.uhb217.chess02.ux.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.uhb217.chess02.R;

public class Dialogs {
  public static void showPromotionDialog(Context ctx, Color color, PromotionCallback callback) {
    Dialog dialog = new Dialog(ctx);
    dialog.setContentView(R.layout.dialog_promotion);
    dialog.setCancelable(false);

    String prefix = color == Color.WHITE ? "w" : "b";

    ImageButton queen = dialog.findViewById(R.id.promoQueen);
    ImageButton rook = dialog.findViewById(R.id.promoRook);
    ImageButton bishop = dialog.findViewById(R.id.promoBishop);
    ImageButton knight = dialog.findViewById(R.id.promoKnight);

    // Dynamically load the correct drawable
    queen.setImageResource(getDrawableId(ctx, prefix + "q"));
    rook.setImageResource(getDrawableId(ctx, prefix + "r"));
    bishop.setImageResource(getDrawableId(ctx, prefix + "b"));
    knight.setImageResource(getDrawableId(ctx, prefix + "n"));

    queen.setOnClickListener(v -> {
      callback.onPieceChosen('q');
      dialog.dismiss();
    });

    rook.setOnClickListener(v -> {
      callback.onPieceChosen('r');
      dialog.dismiss();
    });

    bishop.setOnClickListener(v -> {
      callback.onPieceChosen('b');
      dialog.dismiss();
    });

    knight.setOnClickListener(v -> {
      callback.onPieceChosen('n');
      dialog.dismiss();
    });

    dialog.show();
  }

  public interface PromotionCallback {
    void onPieceChosen(char piece);
  }

  private static int getDrawableId(Context ctx, String name) {
    return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
  }

  public static void showGameOverDialog(Context ctx, String winnerText, String reason, GameOverCallback callback) {
    Dialog dialog = new Dialog(ctx);
    dialog.setContentView(R.layout.dialog_game_over);
    dialog.setCancelable(false);

    TextView resultText = dialog.findViewById(R.id.textResult);
    TextView reasonText = dialog.findViewById(R.id.textReason);
    Button btnRematch = dialog.findViewById(R.id.btnRematch);
    Button btnExit = dialog.findViewById(R.id.btnExit);

    resultText.setText(winnerText);
    reasonText.setText(reason);

    btnRematch.setOnClickListener(v -> {
      dialog.dismiss();
      callback.onRematch();
    });

    btnExit.setOnClickListener(v -> {
      dialog.dismiss();
      callback.onExit();
    });

    dialog.show();
  }

  public interface GameOverCallback {
    void onRematch();

    void onExit();
  }

  private static AlertDialog waitingDialog;

  public static Dialog showWaitingDialog(Context ctx, String roomId, Runnable onCancel) {
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    View view = LayoutInflater.from(ctx).inflate(R.layout.waiting_dialog, null);
    builder.setView(view);
    builder.setCancelable(false);

    TextView roomIdText = view.findViewById(R.id.roomIdText);
    roomIdText.setText("Room ID: " + roomId);

    waitingDialog = builder.create();
    waitingDialog.show();

    TextView statusText = view.findViewById(R.id.statusText);
    Animation blinkAnimation = AnimationUtils.loadAnimation(ctx, R.anim.blink);
    statusText.startAnimation(blinkAnimation);


    Button cancelButton = view.findViewById(R.id.cancelButton);
    cancelButton.setOnClickListener(v -> {
      statusText.clearAnimation();
      if (waitingDialog != null && waitingDialog.isShowing()) {
        waitingDialog.dismiss();
      }
      if (onCancel != null) {
        onCancel.run();
      }
    });
    return waitingDialog;
  }
public static void dismissWaitingDialog() {
    if (waitingDialog != null && waitingDialog.isShowing()) {
      waitingDialog.dismiss();
    }
  }
  public static Dialog loginWaitingDialog(Context ctx) {
    Dialog dialog = new Dialog(ctx);
    View dialogView = LayoutInflater.from(ctx).inflate(R.layout.login_waiting_dialog, null);
    dialog.setContentView(dialogView);
    dialog.setCancelable(false);
    return dialog;
  }

  public static Dialog signupWaitingDialog(Context ctx) {
    Dialog dialog = loginWaitingDialog(ctx);
    TextView statusText = dialog.findViewById(R.id.statusText);
    statusText.setText("Creating account...");
    return dialog;
  }
}
