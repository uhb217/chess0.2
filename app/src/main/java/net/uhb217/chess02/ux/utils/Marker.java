package net.uhb217.chess02.ux.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.pieces.Piece;

public class Marker extends AppCompatImageView {
  private final Piece creator;
  private final Pos pos;

  //if creator != null its a point for indicating legal moves
  public Marker(@NonNull Context ctx, Pos pos, Piece creator, boolean background) {
    super(ctx);
    this.creator = creator;
    this.pos = pos;
    if (background)
      creator.setBackgroundColor(Color.parseColor("#B3F6EB94"));
    else {
      int size = ctx.getResources().getDisplayMetrics().widthPixels / 8;
      this.setLayoutParams(new FrameLayout.LayoutParams(size, size));
      setX(pos.x * size);
      setY(pos.y * size);
      setImageResource(R.drawable.baseline_circle_24);
      setScaleType(ScaleType.CENTER_INSIDE);
      this.setOnClickListener(this::onClick);
    }

  }

  private void onClick(View view) {
    creator.move(pos);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    creator.setBackgroundColor(Color.TRANSPARENT);
  }
}
