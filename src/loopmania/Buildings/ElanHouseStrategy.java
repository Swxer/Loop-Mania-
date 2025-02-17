package loopmania.Buildings;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Character;
import loopmania.PathPosition;
import loopmania.Enemies.ElanEnemy;
import loopmania.Enemies.Enemy;

public class ElanHouseStrategy implements BuildingStrategy {
  private final int range = 1;
  private final int cycleNumberToSpawnAt = 40;
  private final int experiencePointsToSpawnAt = 10000;

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
    return;
  }

  @Override
  public boolean usableOutsideCombat() {
    return true;
  }

  @Override
  public ImageView getImage(String imgLoc) {
    Image image = new Image((new File("src/" + imgLoc + "/elan_house.png")).toURI().toString());
    ImageView view = new ImageView(image);
    return view;
  }

  @Override
  public boolean canSpawnEnemy(Character character) {
    return character.getCycleCount() == cycleNumberToSpawnAt && character.getExp() >= experiencePointsToSpawnAt;
  }

  @Override
  public Enemy spawnEnemy(PathPosition position) {
    return new ElanEnemy(position);
  }

  @Override
  public boolean isSpawnLocation() {
    return false;
  }

  @Override
  public boolean canOnlySpawnNextToPath() {
    return false;
  }

  @Override
  public boolean canOnlySpawnOnPath() {
    return true;
  }

}
