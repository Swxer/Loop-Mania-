package loopmania.Cards;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Buildings.BarracksStrategy;
import loopmania.Buildings.BuildingStrategy;

public class BarracksCardStrategy implements CardStrategy {
  /**
   * returns the image of the item to be displayed
   *
   * @return the imageview of the item
   */
  @Override
  public ImageView getImage(String imgLoc) {
    Image barracksImage = new Image((new File("src/" + imgLoc + "/barracks_card.png")).toURI().toString());
    ImageView view = new ImageView(barracksImage);
    return view;
  }

  @Override
  public BuildingStrategy getBuildingStrategy() {
    return new BarracksStrategy();
  }

}
