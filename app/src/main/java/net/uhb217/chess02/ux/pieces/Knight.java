package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Color;
import net.uhb217.chess02.ux.Pos;

import java.util.Collections;
import java.util.List;

public class Knight extends Piece{
    public Knight(@NonNull Context ctx, Pos pos, Color color) {
        super(ctx, pos, color);
    }

    @Override
    protected int resId() {
        return color == Color.WHITE ? R.drawable.wn : R.drawable.bn;
    }

    @Override
    public List<Pos> getLegalMoves() {
        return Collections.emptyList();
    }
}
