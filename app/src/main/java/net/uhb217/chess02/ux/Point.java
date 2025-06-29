package net.uhb217.chess02.ux;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.pieces.Piece;

public class Point extends AppCompatImageView {
    private final Piece creator;
    private final Pos pos;
    public Point(@NonNull Context ctx, Pos pos, Piece creator) {
        super(ctx);
        this.creator = creator;
        this.pos = pos;
        int size = ctx.getResources().getDisplayMetrics().widthPixels / 8;
        this.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        setX(pos.x * size);
        setY(pos.y * size);
        this.setImageResource(R.drawable.circle);
        this.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        creator.move(getContext(),pos);
    }
}
