package loopmania.Cards;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import loopmania.Buildings.BuildingStrategy;
import loopmania.Buildings.TrapStrategy;

public class TrapCardStrategy implements CardStrategy {
  /**
   * returns the image of the item to be displayed
   *
   * @return the imageview of the item
   */
  @Override
  public ImageView getImage(String imgLoc) {
    Image trapImage = new Image((new File("src/" + imgLoc + "/trap_card.png")).toURI().toString());
    ImageView view = new ImageView(trapImage);
    return view;
  }

  @Override
  public BuildingStrategy getBuildingStrategy() {
    return new TrapStrategy();
  }
}
