package loopmania.Cards;

import javafx.scene.image.ImageView;
import loopmania.Buildings.BuildingStrategy;

public interface CardStrategy {
  /**
   * Gets the image for the card for javafx UI
   *
   * @return card image
   */
  public ImageView getImage(String imgLoc);

  /**
   * Returns the corresponding building strategy for the card
   *
   * @return corresponding building strategy
   */
  public BuildingStrategy getBuildingStrategy();
}
