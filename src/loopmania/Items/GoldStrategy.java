package loopmania.Items;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import loopmania.Enemies.Enemy;
import loopmania.Soldier;
import loopmania.Character;

public class GoldStrategy implements ItemStrategy {
  private final double atkMultiplier = 1;
  private final double defMultiplier = 1;
  private final double critMultiplier = 1;
  private final int range = 1;
  private final int goldAmt = 10;
  private final int price = 100;
  MediaPlayer goldSound;

  @Override
  public double getAtkMultiplier(Enemy enemy) {
    return atkMultiplier;
  }

  @Override
  public double getDefMultiplier(Enemy enemy) {
    return defMultiplier;
  }

  @Override
  public double getCritMultiplier(Enemy enemy) {
    return critMultiplier;
  }

  @Override
  public void onHitEffects(Enemy enemy, List<Soldier> allyList) {
    return;
  }

  @Override
  public boolean isDestroyedOnUse() {
    return false;
  }

  @Override
  public int getRange() {
    return range;
  }

  @Override
  public void useItem(Character character) {
    character.addGold(goldAmt);
    goldSound();
  }

  /**
   * returns the image of the item to be displayed
   *
   * @return the imageview of the item
   */
  @Override
  public ImageView getImage(String imgLoc) {
    Image image = new Image((new File("src/" + imgLoc + "/gold_pile.png")).toURI().toString());
    ImageView view = new ImageView(image);
    return view;
  }

  @Override
  public int getPrice() {
    return price;
  }

  /**
   * sound effect for collecting items from the ground
   */
  public void goldSound() {
    String path = "src/audio/collectGold.wav";
    Media music = new Media(Paths.get(path).toUri().toString());
    goldSound = new MediaPlayer(music);
    goldSound.setVolume(0.3);
    goldSound.play();

  }

}
