package loopmania.Items;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Character;
import loopmania.Enemies.Enemy;
import loopmania.Enemies.VampireEnemy;
import loopmania.Soldier;

public class StakeStrategy implements ItemStrategy {
  private final double atkMultiplier = 0.5;
  private final double vampireAtkMultiplier = 3;
  private final double defMultiplier = 1;
  private final double critMultiplier = 1;
  private final int range = 0;
  private final int price = 20;

  /**
   * Stake item lowers danage dealt by 50% unless the Character is attacking a
   * vampire, in which case deal three times as much damage
   *
   * @param enemy The enemy that the Character is in combat with
   * @return The attack multiplier against the enemy, may vary depending on the
   *         enemy type
   */
  @Override
  public double getAtkMultiplier(Enemy enemy) {
    if (enemy instanceof VampireEnemy)
      return vampireAtkMultiplier;
    return atkMultiplier;
  }

  @Override
  public int getRange() {
    return range;
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
    Image image = new Image((new File("src/" + imgLoc + "/stake.png")).toURI().toString());
    ImageView view = new ImageView(image);
    return view;
  }

  @Override
  public int getPrice() {
    return price;
  }

}
