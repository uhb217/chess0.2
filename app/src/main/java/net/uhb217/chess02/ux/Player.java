package net.uhb217.chess02.ux;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.uhb217.chess02.ux.utils.Color;
import net.uhb217.chess02.ux.utils.FirebaseUtils;

import java.io.Serializable;

public class Player implements Serializable {
  public final String username;
  public int rating = 1600; // Default rating
  public int timeSeconds;
  private Color color;
  public Player() {
    username = null;
  }


  public Player(String username, int rating, Color color) {
    this.username = username;
    this.rating = rating;
    this.color = color;
    this.timeSeconds = 10 * 60; // Default time
  }

  public Player(String username, int rating) {
    this.username = username;
    this.rating = rating;
    this.color = null;

  }
  public static Player Stockfish(int depth) {
    int rating;
    if (depth <= 0) rating = 0;

    if (depth <= 12) {
      rating = 2350 - (12 - depth) * 100;
    } else if (depth <= 20) {
      double ratio = (depth - 12) / 8.0;
      rating = (int) (2350 + ratio * (2850 - 2350));
    } else {
      rating = 2850 + (depth - 20) * 25;
    }
    return new Player("Stockfish", rating, Color.BLACK);
  }

  public String getTimeString() {
    int minutes = timeSeconds / 60;
    int seconds = timeSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }
  public static void fromFirebaseUsername(String username, PlayerCallback callback) {
      DatabaseReference ref = FirebaseDatabase.getInstance()
              .getReference("users")
              .child(username);
      ref.addListenerForSingleValueEvent(FirebaseUtils.ValueListener(snapshot -> {
        if (!snapshot.exists()) {
          callback.onPlayerFetched(null);
          return;
        }

        Integer rating = snapshot.child("rating").getValue(Integer.class);

        if (rating == null) {
          callback.onPlayerFetched(null);
          return;
        }

        Player player = new Player(username, rating);
        callback.onPlayerFetched(player);
      }));
  }
  public static void fromFirebaseUser(FirebaseUser user, PlayerCallback callback) {
    if (user == null) {
      callback.onPlayerFetched(null);
      return;
    }
    fromFirebaseUsername(user.getDisplayName(), callback);
  }

  public interface PlayerCallback {
    void onPlayerFetched(Player player);
  }

  public Player setColor(Color color) {
    this.color = color;
    return this;
  }

    public Color getColor() {
        return color;
    }
}
