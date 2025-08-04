package net.uhb217.chess02.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Player;

public class PlayerInfoView extends LinearLayout {
  private ImageView playerIcon;
  private TextView playerName;
  private TextView playerRating;
  private TextView playerTime;

  public PlayerInfoView(Context ctx, Player player, boolean againstStockfish) {
    super(ctx);
    setOrientation(HORIZONTAL);
    setGravity(Gravity.CENTER_VERTICAL);
    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
    setPadding(padding, padding, padding, padding);
    setBackground(ContextCompat.getDrawable(ctx, R.drawable.player_info_bg));
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(dp(ctx, 10), dp(ctx, 10), dp(ctx, 10), dp(ctx, 10));
    setLayoutParams(layoutParams);

    // Player Icon
    playerIcon = new ImageView(ctx);
    LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(ctx, 40), dp(ctx, 40));
    iconParams.setMarginEnd(dp(ctx, 12));
    playerIcon.setLayoutParams(iconParams);
    playerIcon.setImageResource(R.drawable.ic_player);
    playerIcon.setBackground(ContextCompat.getDrawable(ctx, R.drawable.circle_bg));
    addView(playerIcon);

    // Name and Rating
    LinearLayout nameRatingLayout = new LinearLayout(ctx);
    nameRatingLayout.setOrientation(VERTICAL);
    LinearLayout.LayoutParams nameRatingParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    nameRatingLayout.setLayoutParams(nameRatingParams);

    playerName = new TextView(ctx);
    playerName.setId(View.generateViewId());
    playerName.setText(player.username);
    playerName.setTextColor(Color.WHITE);
    playerName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    playerName.setTypeface(playerName.getTypeface(), android.graphics.Typeface.BOLD);
    nameRatingLayout.addView(playerName);

    playerRating = new TextView(ctx);
    playerRating.setId(View.generateViewId());
    playerRating.setText(String.valueOf(player.rating));
    playerRating.setTextColor(Color.WHITE);
    playerRating.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    nameRatingLayout.addView(playerRating);

    addView(nameRatingLayout);

    // Time
    playerTime = new TextView(ctx);
    playerTime.setId(View.generateViewId());
    if (!againstStockfish)
      playerTime.setText("10:00"); // Default, can be updated
    playerTime.setTextColor(Color.WHITE);
    playerTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    playerTime.setBackground(ContextCompat.getDrawable(ctx, R.drawable.time_bg));
    playerTime.setPadding(dp(ctx, 8), dp(ctx, 8), dp(ctx, 8), dp(ctx, 8));
    addView(playerTime);

    //if the player is stockfish add lottie animation between the time and the name 
    if (player.username.equals("Stockfish")) {
      LottieAnimationView lottieView = new LottieAnimationView(ctx);
      LinearLayout.LayoutParams lottieParams = new LinearLayout.LayoutParams(dp(ctx, 40), dp(ctx, 40));
      lottieParams.setMarginEnd(dp(ctx, 12));
      lottieView.setLayoutParams(lottieParams);
      lottieView.setAnimation("move_waiting.json");
      lottieView.setRepeatCount(-1);
      lottieView.setId(R.id.lottie);
      addView(lottieView, 2);
    }
    
  }

  private int dp(Context ctx, int dp) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
  }

  public void setTime(String time) {
    playerTime.setText(time);
  }

  public void setPlayer(Player player) {
    playerName.setText(player.username);
    playerRating.setText(String.valueOf(player.rating));
  }

  public static void overrideXMLPlayerInfoView(LinearLayout frame, Player player) {
    LinearLayout nameRating = (LinearLayout) frame.getChildAt(1);
    ((TextView) nameRating.getChildAt(0)).setText(player.username);
    ((TextView) nameRating.getChildAt(1)).setText(String.valueOf(player.rating));
    ((TextView) frame.getChildAt(2)).setText("10:00"); // Default time, can be updated later

  }
}
