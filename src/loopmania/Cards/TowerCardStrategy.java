package loopmania.Cards;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Buildings.BuildingStrategy;
import loopmania.Buildings.TowerStrategy;

public class TowerCardStrategy implements CardStrategy {
  /**
   * returns the image of the item to be displayed
   *
   * @return the imageview of the item
   */
  @Override
  public ImageView getImage(String imgLoc) {
    Image towerImage = new Image((new File("src/" + imgLoc + "/tower_card.png")).toURI().toString());
    ImageView view = new ImageView(towerImage);
    return view;
  }

  @Override
  public BuildingStrategy getBuildingStrategy() {
    return new TowerStrategy();
  }
}
