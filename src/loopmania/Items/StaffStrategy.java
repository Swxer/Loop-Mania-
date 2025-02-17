package loopmania.Items;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Character;
import loopmania.Enemies.Enemy;
import loopmania.Soldier;

public class StaffStrategy implements ItemStrategy {
  private final double atkMultiplier = 2;
  private final double defMultiplier = 1;
  private final double critMultiplier = 1;
  private final int range = 0;
  private final int price = 60;

  /**
   * Staff item reduces damange dealt by the Character by 65% so returns 0.35
   *
   * @param enemy The enemy that the Character is in combat with
   * @return The attack multiplier against the enemy, may vary depending on the
   *         enemy type
   */
  @Override
  public double getAtkMultiplier(Enemy enemy) {
    return atkMultiplier;
  }

  @Override
  public double getDefMultiplier(Enemy enemy) {
    return defMultiplier;
  }

  @Override
  public int getRange() {
    return range;
  }

  @Override
  public double getCritMultiplier(Enemy enemy) {
    return critMultiplier;
  }

  /**
   * "Random chance of inflicting a trance, which transforms the attacked enemy
   * into an allied soldier temporarily"
   *
   * @param enemy The enemy that the Character is in combat with
   */
  @Override
  public void onHitEffects(Enemy enemy, List<Soldier> allyList) {
    enemy.reduceHealth(100);
    allyList.add(new Soldier());
  };

  @Override
  public boolean isDestroyedOnUse() {
    return false;
  }

  @Override
  public void useItem(Character character) {
    return;
  }

  /**
   * returns the image of the item to be displayed
   *
   * @return the imageview of the item
   */
  @Override
  public ImageView getImage(String imgLoc) {
    Image image = new Image((new File("src/" + imgLoc + "/staff.png")).toURI().toString());
    ImageView view = new ImageView(image);
    return view;
  }

  @Override
  public int getPrice() {
    return price;
  }

}
