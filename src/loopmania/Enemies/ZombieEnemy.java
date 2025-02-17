package loopmania.Enemies;

import java.io.File;
import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.PathPosition;
import loopmania.Buffs.Buff;
import loopmania.Buffs.ZombieCritBuff;

public class ZombieEnemy extends Enemy {

  private final double health = 30;
  private final int battleRange = 2;
  private final int supportRange = 5;
  private final double damage = 10;
  private final int expDrop = 25;
  private final int goldDrop = 25;
  private final int critRate = 20;

  /**
   * Zombie constructor, sets Zombie stats
   *
   * @param position the position where the enemy will spawn in the map
   */
  public ZombieEnemy(PathPosition position) {
    super(position);
    setHealth(health);
    setMaxHealth(health);
    setBattleRange(battleRange);
    setSupportRange(supportRange);
    setDamage(damage);
    setExpDrop(expDrop);
    setGoldDrop(goldDrop);
    setCritRate(critRate);
  }

  @Override
  public Buff criticalHit() {
    return new ZombieCritBuff();
  }

  @Override
  public void move() {
    // zombie moves slower than all of the enemies
    int directionChoice = (new Random()).nextInt(3);
    if (directionChoice == 0)
      moveUpPath();
  }

  @Override
  public ImageView getImage(String imgLoc) {
    Image image = new Image((new File("src/" + imgLoc + "/zombie.png")).toURI().toString());
    ImageView view = new ImageView(image);
    return view;
  }

}
