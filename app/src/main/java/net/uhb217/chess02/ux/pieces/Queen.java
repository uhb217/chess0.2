package net.uhb217.chess02.ux.pieces;

import android.content.Context;

import androidx.annotation.NonNull;

import net.uhb217.chess02.R;
import net.uhb217.chess02.ux.Color;
import net.uhb217.chess02.ux.Pos;

import java.util.Collections;
import java.util.List;

public class Queen extends Piece{
    public Queen(@NonNull Context ctx, Pos pos, Color color) {
        super(ctx, pos, color);
    }

    @Override
    protected int resId() {
        return color == Color.WHITE ? R.drawable.wq : R.drawable.bq;
    }

    @Override
    public List<Pos> getLegalMoves() {
        return Collections.emptyList();
    }
}
