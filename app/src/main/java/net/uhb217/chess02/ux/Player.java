package net.uhb217.chess02.ux;

import java.util.Map;

public class Player {
    public final String username;
    public int rating = 1600; // Default rating

    public Player(String username, int rating) {
        this.username = username;
        this.rating = rating;
    }
    public Player fromHashMap(Map<String, Object> map) {
        String username = (String) map.get("username");
        int rating = map.containsKey("rating") ? (int) map.get("rating") : 1600;
        return new Player(username, rating);
    }

}
