package loopmania.Buildings;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Character;
import loopmania.PathPosition;
import loopmania.Enemies.Enemy;

public class TowerStrategy implements BuildingStrategy {
  private final int range = 2;
  private final int towerDamage = 10;

  @Override
  public int getRange() {
    return range;
  }

  @Override
  public void useBuilding(Character character) {
    return;
  }

  @Override
  public void useBuilding(Enemy enemy) {
    enemy.reduceHealth(towerDamage);
  }

  @Override
  public boolean usableOutsideCombat() {
    return false;
  }

  @Override
  public ImageView getImage(String imgLoc) {
    Image image = new Image((new File("src/" + imgLoc + "/tower.png")).toURI().toString());
    ImageView view = new ImageView(image);
    return view;
  }

  @Override
  public boolean canSpawnEnemy(Character character) {
    return false;
  }

  @Override
  public Enemy spawnEnemy(PathPosition position) {
    return null;
  }

  @Override
  public boolean isSpawnLocation() {
    return false;
  }

  @Override
  public boolean canOnlySpawnNextToPath() {
    return true;
  }

  @Override
  public boolean canOnlySpawnOnPath() {
    return false;
  }

}
