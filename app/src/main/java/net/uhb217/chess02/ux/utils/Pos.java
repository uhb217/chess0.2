package net.uhb217.chess02.ux.utils;

import java.util.List;

public class Pos {
    public int x;
    public int y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Pos pos) {
        return x == pos.x && y == pos.y;
    }

    public boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }
    public static boolean contains(List<Pos> positions, Pos pos) {
        for (Pos position : positions)
            if (position.equals(pos))
                return true;
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
