package loopmania.Buildings;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Character;
import loopmania.PathPosition;
import loopmania.Enemies.Enemy;

public class BarracksStrategy implements BuildingStrategy {
  private final int range = 1;

  @Override
  public void useBuilding(Character character) {
    if (character.getSoldiers().size() < 3)
      character.addSoldier();
  }

  @Override
  public void useBuilding(Enemy enemy) {
    return;
  }

  @Override
  public int getRange() {
    return range;
  }

  @Override
  public boolean usableOutsideCombat() {
    return true;
  }

  @Override
  public ImageView getImage(String imgLoc) {
    Image image = new Image((new File("src/" + imgLoc + "/barracks.png")).toURI().toString());
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
    return false;
  }

  @Override
  public boolean canOnlySpawnOnPath() {
    return true;
  }

}
