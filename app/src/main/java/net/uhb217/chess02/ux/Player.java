package net.uhb217.chess02.ux;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.uhb217.chess02.ux.utils.Color;

import java.io.Serializable;

public class Player implements Serializable {
  public final String username;
  public int rating = 1600; // Default rating
  public int timeSeconds;
  private Color color;


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

  public Player() {
    this.username  = "Offline Player";
    this.rating = 1600; // Default rating
    this.color = Color.WHITE; // Default color
    this.timeSeconds = 10 * 60; // Default time
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

      ref.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
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
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
              callback.onPlayerFetched(null);
          }
      });
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

  public void setColor(Color color) {
    this.color = color;
  }

    public Color getColor() {
        return color;
    }
}
