package loopmania;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import loopmania.Buildings.Building;
import loopmania.Cards.Card;
import loopmania.Enemies.Enemy;
import loopmania.Items.Item;
import loopmania.Items.ItemStrategy;
import loopmania.Shop.ShopController;

/**
 * the draggable types. If you add more draggable types, add an enum value here.
 * This is so we can see what type is being dragged.
 */
enum DRAGGABLE_TYPE {
  CARD, ITEM
}

/**
 * A JavaFX controller for the world.
 *
 * All event handlers and the timeline in JavaFX run on the JavaFX application
 * thread:
 * https://examples.javacodegeeks.com/desktop-java/javafx/javafx-concurrency-example/
 * Note in
 * https://openjfx.io/javadoc/11/javafx.graphics/javafx/application/Application.html
 * under heading "Threading", it specifies animation timelines are run in the
 * application thread. This means that the starter code does not need locks
 * (mutexes) for resources shared between the timeline KeyFrame, and all of the
 * event handlers (including between different event handlers). This will make
 * the game easier for you to implement. However, if you add time-consuming
 * processes to this, the game may lag or become choppy.
 *
 * If you need to implement time-consuming processes, we recommend: using Task
 * https://openjfx.io/javadoc/11/javafx.graphics/javafx/concurrent/Task.html by
 * itself or within a Service
 * https://openjfx.io/javadoc/11/javafx.graphics/javafx/concurrent/Service.html
 *
 * Tasks ensure that any changes to public properties, change notifications for
 * errors or cancellation, event handlers, and states occur on the JavaFX
 * Application thread, so is a better alternative to using a basic Java Thread:
 * https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm The Service class
 * is used for executing/reusing tasks. You can run tasks without Service,
 * however, if you don't need to reuse it.
 *
 * If you implement time-consuming processes in a Task or thread, you may need
 * to implement locks on resources shared with the application thread (i.e.
 * Timeline KeyFrame and drag Event handlers). You can check whether code is
 * running on the JavaFX application thread by running the helper method
 * printThreadingNotes in this class.
 *
 * NOTE: http://tutorials.jenkov.com/javafx/concurrency.html and
 * https://www.developer.com/design/multithreading-in-javafx/#:~:text=JavaFX%20has%20a%20unique%20set,in%20the%20JavaFX%20Application%20Thread.
 *
 * If you need to delay some code but it is not long-running, consider using
 * Platform.runLater
 * https://openjfx.io/javadoc/11/javafx.graphics/javafx/application/Platform.html#runLater(java.lang.Runnable)
 * This is run on the JavaFX application thread when it has enough time.
 */
public class LoopManiaWorldController {

  @FXML
  private Label health;

  @FXML
  private Label experience;

  @FXML
  private Label gold;

  @FXML
  private Label cycle;

  @FXML
  private Label doggieCoin;

  @FXML
  private Label scrapMetal;

  @FXML
  private Label pauseLabel;

  /**
   * squares gridpane includes path images, enemies, character, empty grass,
   * buildings
   */
  @FXML
  private GridPane squares;

  /**
   * cards gridpane includes cards and the ground underneath the cards
   */
  @FXML
  private GridPane cards;

  /**
   * anchorPaneRoot is the "background". It is useful since anchorPaneRoot
   * stretches over the entire game world, so we can detect dragging of
   * cards/items over this and accordingly update DragIcon coordinates
   */
  @FXML
  private AnchorPane anchorPaneRoot;

  /**
   * equippedItems gridpane is for equipped items (e.g. swords, shield, axe)
   */
  @FXML
  private GridPane equippedItems;

  @FXML
  private Pane shop;

  @FXML
  private Label goalLabel;

  @FXML
  private ImageView ally1;

  @FXML
  private ImageView ally2;

  @FXML
  private ImageView ally3;

  @FXML
  private GridPane unequippedInventory;

  // all image views including tiles, character, enemies, cards... even though
  // cards in separate gridpane...
  private List<ImageView> entityImages;
  private String goal;
  /**
   * when we drag a card/item, the picture for whatever we're dragging is set here
   * and we actually drag this node
   */
  private DragIcon draggedEntity;

  private boolean isPaused;
  private LoopManiaWorld world;
  private Pane shopPane;
  private ShopController shopController;
  /**
   * runs the periodic game logic - second-by-second moving of character through
   * maze, as well as enemies, and running of battles
   */
  private Timeline timeline;

  /**
   * the image currently being dragged, if there is one, otherwise null. Holding
   * the ImageView being dragged allows us to spawn it again in the drop location
   * if appropriate.
   */
  private ImageView currentlyDraggedImage;

  /**
   * null if nothing being dragged, or the type of item being dragged
   */
  private DRAGGABLE_TYPE currentlyDraggedType;

  /**
   * mapping from draggable type enum CARD/TYPE to the event handler triggered
   * when the draggable type is dropped over its appropriate gridpane
   */
  private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> gridPaneSetOnDragDropped;
  /**
   * mapping from draggable type enum CARD/TYPE to the event handler triggered
   * when the draggable type is dragged over the background
   */
  private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> anchorPaneRootSetOnDragOver;
  /**
   * mapping from draggable type enum CARD/TYPE to the event handler triggered
   * when the draggable type is dropped in the background
   */
  private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> anchorPaneRootSetOnDragDropped;
  /**
   * mapping from draggable type enum CARD/TYPE to the event handler triggered
   * when the draggable type is dragged into the boundaries of its appropriate
   * gridpane
   */
  private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> gridPaneNodeSetOnDragEntered;
  /**
   * mapping from draggable type enum CARD/TYPE to the event handler triggered
   * when the draggable type is dragged outside of the boundaries of its
   * appropriate gridpane
   */
  private EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>> gridPaneNodeSetOnDragExited;

  /**
   * object handling switching to the main menu
   */
  private MenuSwitcher mainMenuSwitcher;
  private MenuSwitcher gameSwitcher;
  private MenuSwitcher winSwitcher;
  private MenuSwitcher loseSwitcher;
  private String imgLoc;

  /**
   * Music & sound effects
   */
  MediaPlayer gameplayMusic;
  MediaPlayer menuMusic;
  MediaPlayer buttonClick;
  MediaPlayer sellSound;
  MediaPlayer buySound;
  MediaPlayer pauseSound;
  private boolean isPlayingGameMusic = false;
  private boolean isPlayingMenuMusic = false;

  /**
   * @param world           world object loaded from file
   * @param initialEntities the initial JavaFX nodes (ImageViews) which should be
   *                        loaded into the GUI
   */
  public LoopManiaWorldController(LoopManiaWorld world, List<ImageView> initialEntities, String imgLoc) {
    this.world = world;
    entityImages = new ArrayList<>(initialEntities);
    currentlyDraggedImage = null;
    currentlyDraggedType = null;
    this.imgLoc = imgLoc;

    // initialize them all...
    gridPaneSetOnDragDropped = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
    anchorPaneRootSetOnDragOver = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
    anchorPaneRootSetOnDragDropped = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
    gridPaneNodeSetOnDragEntered = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
    gridPaneNodeSetOnDragExited = new EnumMap<DRAGGABLE_TYPE, EventHandler<DragEvent>>(DRAGGABLE_TYPE.class);
  }

  /**
   * gets the image locations for game sprites
   *
   * @return image locations for game sprites
   */
  public String getImgLoc() {
    return imgLoc;
  }

  /**
   * sets the difficulty of the world
   *
   * @param difficulty the difficulty of the world (standard, survival, berserker
   *   confusing)
   */
  public void setDifficulty(String difficulty) {
    world.setDifficulty(difficulty);
  }

  @FXML
  public void initialize() {
    Image pathTilesImage = new Image((new File("src/images/32x32GrassAndDirtPath.png")).toURI().toString());
    Image inventorySlotImage = new Image((new File("src/images/empty_slot.png")).toURI().toString());
    Rectangle2D imagePart = new Rectangle2D(0, 0, 32, 32);
    shop = new Pane();
    goalLabel.setText(goal);
    // Add the ground first so it is below all other entities (inculding all the
    // twists and turns)
    for (int x = 0; x < world.getWidth(); x++) {
      for (int y = 0; y < world.getHeight(); y++) {
        ImageView groundView = new ImageView(pathTilesImage);
        groundView.setViewport(imagePart);
        squares.add(groundView, x, y);
      }
    }
    FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
    try {
      shopPane = loader.load();
      shopController = loader.getController();
      shopController.setWorldController(this);
      shopController.setImages();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // load entities loaded from the file in the loader into the squares gridpane
    for (ImageView entity : entityImages) {
      squares.getChildren().add(entity);
    }
    // add the ground underneath the cards
    for (int x = 0; x < world.getWidth(); x++) {
      ImageView groundView = new ImageView(pathTilesImage);
      groundView.setViewport(imagePart);
      cards.add(groundView, x, 0);
    }

    // add the empty slot images for the unequipped inventory
    for (int x = 0; x < LoopManiaWorld.unequippedInventoryWidth; x++) {
      for (int y = 0; y < LoopManiaWorld.unequippedInventoryHeight; y++) {
        ImageView emptySlotView = new ImageView(inventorySlotImage);
        unequippedInventory.add(emptySlotView, x, y);
      }
    }

    // create the draggable icon
    draggedEntity = new DragIcon();
    draggedEntity.setVisible(false);
    draggedEntity.setOpacity(0);
    anchorPaneRoot.getChildren().add(draggedEntity);

    // character stats
    Character worldCharacter = world.getCharacter();

    health.textProperty().bind(Bindings.convert(worldCharacter.getHealthProperty()));
    experience.textProperty().bind(Bindings.convert(worldCharacter.getExpProperty()));
    gold.textProperty().bind(Bindings.convert(worldCharacter.getGoldProperty()));
    cycle.textProperty().bind(Bindings.convert(worldCharacter.getCycleProperty()));
    doggieCoin.textProperty().bind(Bindings.convert(worldCharacter.getDoggieCoinProperty()));
    scrapMetal.textProperty().bind(Bindings.convert(worldCharacter.getScrapMetalProperty()));
  }

  /**
   * handles exiting the shop in the front end
   * @throws IOException
   */
  public void exitShop() throws IOException {
    shopController.setBuyErrorMessage("");
    shopController.setSellErrorMessage("");
    anchorPaneRoot.getChildren().remove(shopPane);
    anchorPaneRoot.requestFocus();
    startTimer();
  }

  /**
   * gets the game world
   *
   * @return the game world
   */
  public LoopManiaWorld getWorld() {
    return world;
  }

  /**
   * create and run the timer
   */
  public void startTimer() {

    if (!isPlayingGameMusic) {
      // if the game music is not playing after switching scene
      gameMusic();
      isPlayingGameMusic = true;
    }

    // if the menu music is playing after switching scene
    if (isPlayingMenuMusic)
      menuMusic.stop();

    System.out.println("starting timer");
    isPaused = false;
    // trigger adding code to process main game logic to queue. JavaFX will target
    // framerate of 0.3 seconds
    timeline = new Timeline(new KeyFrame(Duration.seconds(0.05), event -> {
      world.runTickMoves();
      loadAllySoliders();
      List<Enemy> defeatedEnemies = world.runBattles();
      for (Enemy e : defeatedEnemies) {
        reactToEnemyDefeat(e);
      }
      List<Enemy> newEnemies = world.possiblySpawnEnemies();
      for (Enemy newEnemy : newEnemies) {
        onLoad(newEnemy);
      }
      openHerosCastle();
      List<Item> newItems = world.possiblySpawnItems();
      for (Item item : newItems) {
        onLoadPath(item);
      }

      if (world.getGameStatus() == "Won") {
        try {
          pause();
          switchToWin();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      } else if (world.getGameStatus() == "Lost") {
        try {
          pause();
          switchToLose();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
      // printThreadingNotes("HANDLED TIMER");
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  /**
   * sets the goal for the world
   *
   * @param goal the goal for the world
   */
  public void setGoal(String goal) {
    this.goal = goal;
  }

  /**
   * handles loading an ally soldier into the UI
   *
   */
  private void loadAllySoliders() {
    int numAllies = world.getCharacter().getSoldiers().size();
    if (numAllies == 0) {
      ally1.setImage(null);
      ally2.setImage(null);
      ally3.setImage(null);
    } else if (numAllies == 1) {
      ally1.setImage(new Soldier().getImage(imgLoc).getImage());
      ally2.setImage(null);
      ally3.setImage(null);
    } else if (numAllies == 2) {
      ally1.setImage(new Soldier().getImage(imgLoc).getImage());
      ally2.setImage(new Soldier().getImage(imgLoc).getImage());
      ally3.setImage(null);
    } else if (numAllies == 3) {
      ally1.setImage(new Soldier().getImage(imgLoc).getImage());
      ally2.setImage(new Soldier().getImage(imgLoc).getImage());
      ally3.setImage(new Soldier().getImage(imgLoc).getImage());
    }
  }

  /**
   * handles opening the Heros Castle in the UI/frontend
   *
   */
  public void openHerosCastle() {
    if (world.getCharacter().getCycleCount() == world.getNextHeroCastleCycle()) {
      pause();
      world.addHeroCastleCycles(1);
      world.addNextHeroCastleCycle(world.getHeroCastleCycles());
      pauseSound();

      // shop = anchorPaneRoot;
      anchorPaneRoot.getChildren().remove(shopPane);
      anchorPaneRoot.getChildren().add(shopPane);
    }
  }

  /**
   * pause the execution of the game loop the human player can still drag and drop
   * items during the game pause
   */
  public void pause() {
    isPaused = true;
    System.out.println("pausing");
    timeline.stop();
  }

  public void terminate() {
    pause();
  }

  /**
   * pair the entity an view so that the view copies the movements of the entity.
   * add view to list of entity images
   *
   * @param entity backend entity to be paired with view
   * @param view   frontend imageview to be paired with backend entity
   */
  private void addEntity(Entity entity, ImageView view) {
    trackPosition(entity, view);
    entityImages.add(view);
  }

  /**
   * load a card from the world, and pair it with an image in the GUI
   */
  private void loadCard() {
    Card card = world.loadCard();
    onLoad(card);
    if (world.getCardDestroyed())
      loadItem();
  }

  /**
   * load a sword from the world, and pair it with an image in the GUI
   */
  private void loadItem() {
    Item item = world.addUnequippedItem();
    onLoad(item);
  }

  /**
   * buys an item from the shop, deducts gold from the charcter, if character does
   * not have enough gold, item is set to null
   *
   * @param strat item strategy of the item to be bought
   */
  public void buyItem(ItemStrategy strat) {
    Item item = world.buyItem(strat);
    if (item != null) {
      onLoad(item);
      shopController.setBuyErrorMessage("");
      buySound();
    } else {
      shopController.setBuyErrorMessage("Cannot afford item");
    }
  }

  /**
   * crafts an item from the shop, deducts scrap metal from the charcter, if
   * character does not have enough scrap metal, item is set to null
   *
   * @param strat item strategy of the item to be crafted
   */
  public void craftItem(ItemStrategy strat) {
    Item item = world.craftItem(strat);
    if (item != null) {
      onLoad(item);
      shopController.setCraftErrorMessage("");
    } else {
      shopController.setCraftErrorMessage("You do not have enough scrap metal");
    }
  }

  /**
   * sells an item from the shop, adds gold to the charcter, if character does not
   * have the item, nothing happens
   *
   * @param strat item strategy of the item to be sold
   */
  public void sellItem(Class<?> strategy) {
    if (world.sellItem(strategy) == null) {
      shopController.setSellErrorMessage("You do not have this item");
    } else {
      shopController.setSellErrorMessage("");
      sellSound();
    }
  }

  /**
   * sells an item from the shop, adds gold to the charcter, if character does not
   * have the item, nothing happens
   *
   * @param strat item strategy of the item to be sold
   */
  public void sellDoggieCoin() {
    if (world.sellDoggieCoin() == null) {
      shopController.setSellErrorMessage("You do not have any DoggieCoin");
    } else {
      shopController.setSellErrorMessage("");
      sellSound();
    }
  }

  /**
   * run GUI events after an enemy is defeated, such as spawning
   * items/experience/gold
   *
   * @param enemy defeated enemy for which we should react to the death of
   */
  private void reactToEnemyDefeat(Enemy enemy) {
    // react to character defeating an enemy
    // in starter code, spawning extra card/weapon...
    loadItem();
    loadCard();
  }

  /**
   * load a card image into the GUI. Particularly, we must connect to the drag
   * detection event handler, and load the image into the cards GridPane.
   *
   * @param card
   */
  private void onLoad(Card card) {
    ImageView view = card.getImage(imgLoc);

    // FROM
    // https://stackoverflow.com/questions/41088095/javafx-drag-and-drop-to-gridpane
    // note target setOnDragOver and setOnDragEntered defined in initialize method
    addDragEventHandlers(view, DRAGGABLE_TYPE.CARD, cards, squares);

    addEntity(card, view);
    cards.getChildren().add(view);
  }

  /**
   * load a sword into the GUI. Particularly, we must connect to the drag
   * detection event handler, and load the image into the unequippedInventory
   * GridPane.
   *
   * @param sword
   */
  private void onLoad(Item item) {
    ImageView view = item.getImage(imgLoc);
    addDragEventHandlers(view, DRAGGABLE_TYPE.ITEM, unequippedInventory, equippedItems);
    addEntity(item, view);
    unequippedInventory.getChildren().add(view);
  }

  /**
   * load a item into the GUI. Particularly, we must connect to the drag detection
   * event handler, and load the image into the equippedInventory GridPane.
   *
   * @param item item to be loaded
   */
  private void onLoadEquipped(Item item) {
    ImageView view = item.getImage(imgLoc);
    addDragEventHandlers(view, DRAGGABLE_TYPE.ITEM, equippedItems, equippedItems);
    addEntity(item, view);
    equippedItems.getChildren().add(view);
  }

  /**
   * load an enemy into the GUI
   *
   * @param enemy
   */
  private void onLoadPath(Item item) {
    ImageView view = item.getImage(imgLoc);
    addEntity(item, view);
    squares.getChildren().add(view);
  }

  /**
   * load an enemy into the GUI
   *
   * @param enemy
   */
  private void onLoad(Enemy enemy) {
    ImageView view = enemy.getImage(imgLoc);
    addEntity(enemy, view);
    squares.getChildren().add(view);
  }

  /**
   * load a building into the GUI
   *
   * @param building
   */
  private void onLoad(Building building) {
    ImageView view = building.getImage(imgLoc);
    addEntity(building, view);

    world.addBuildingToWorld(building);
    squares.getChildren().add(view);
  }

  /**
   * add drag event handlers for dropping into gridpanes, dragging over the
   * background, dropping over the background. These are not attached to invidual
   * items such as swords/cards.
   *
   * @param draggableType  the type being dragged - card or item
   * @param sourceGridPane the gridpane being dragged from
   * @param targetGridPane the gridpane the human player should be dragging to
   *                       (but we of course cannot guarantee they will do so)
   */
  private void buildNonEntityDragHandlers(DRAGGABLE_TYPE draggableType, GridPane sourceGridPane,
      GridPane targetGridPane) {

    gridPaneSetOnDragDropped.put(draggableType, new EventHandler<DragEvent>() {
      public void handle(DragEvent event) {
        /*
         * you might want to design the application so dropping at an invalid location
         * drops at the most recent valid location hovered over, or simply allow the
         * card/item to return to its slot (the latter is easier, as you won't have to
         * store the last valid drop location!)
         */
        if (currentlyDraggedType == draggableType) {
          // problem = event is drop completed is false when should be true...
          // https://bugs.openjdk.java.net/browse/JDK-8117019
          // putting drop completed at start not making complete on VLAB...

          // Data dropped
          // If there is an image on the dragboard, read it and use it
          Dragboard db = event.getDragboard();
          Node node = event.getPickResult().getIntersectedNode();
          if (node != targetGridPane && db.hasImage()) {

            Integer cIndex = GridPane.getColumnIndex(node);
            Integer rIndex = GridPane.getRowIndex(node);
            int x = cIndex == null ? 0 : cIndex;
            int y = rIndex == null ? 0 : rIndex;
            // Places at 0,0 - will need to take coordinates once that is implemented
            ImageView image = new ImageView(db.getImage());

            int nodeX = GridPane.getColumnIndex(currentlyDraggedImage);
            int nodeY = GridPane.getRowIndex(currentlyDraggedImage);
            switch (draggableType) {
              case CARD:
                removeDraggableDragEventHandlers(draggableType, targetGridPane);
                Building newBuilding = convertCardToBuildingByCoordinates(nodeX, nodeY, x, y);

                // check if null and dont load into ui if null
                // else building has already spawned in backend, add to UI with onLoad(building)
                onLoad(newBuilding);
                break;
              case ITEM:
                removeDraggableDragEventHandlers(draggableType, targetGridPane);

                if (targetGridPane == equippedItems) {
                  Item currItem = world.getEquippedInventoryItemEntityByCoordinates(x, y);
                  if (currItem != null) {
                    world.removeEquippedInventoryItemByCoordinates(x, y);
                  }
                  Item item = world.getUnequippedInventoryItemEntityByCoordinates(nodeX, nodeY);
                  Item newItem = new Item(new SimpleIntegerProperty(x), new SimpleIntegerProperty(y),
                      item.getStrategy());
                  onLoadEquipped(newItem);
                  world.addEquippedInventoryItem(item);
                  removeItemByCoordinates(nodeX, nodeY);
                  world.removeUnequippedInventoryItem(item);
                } else {
                  removeItemByCoordinates(nodeX, nodeY);
                }
                targetGridPane.add(image, x, y, 1, 1);
                break;
              default:
                break;
            }

            draggedEntity.setVisible(false);
            draggedEntity.setMouseTransparent(false);
            // remove drag event handlers before setting currently dragged image to null
            currentlyDraggedImage = null;
            currentlyDraggedType = null;
            printThreadingNotes("DRAG DROPPED ON GRIDPANE HANDLED");
          }
        }
        event.setDropCompleted(true);
        // consuming prevents the propagation of the event to the anchorPaneRoot (as a
        // sub-node of anchorPaneRoot, GridPane is prioritized)
        // https://openjfx.io/javadoc/11/javafx.base/javafx/event/Event.html#consume()
        // to understand this in full detail, ask your tutor or read
        // https://docs.oracle.com/javase/8/javafx/events-tutorial/processing.htm
        event.consume();
      }
    });

    // this doesn't fire when we drag over GridPane because in the event handler for
    // dragging over GridPanes, we consume the event
    anchorPaneRootSetOnDragOver.put(draggableType, new EventHandler<DragEvent>() {
      // https://github.com/joelgraff/java_fx_node_link_demo/blob/master/Draggable_Node/DraggableNodeDemo/src/application/RootLayout.java#L110
      @Override
      public void handle(DragEvent event) {
        if (currentlyDraggedType == draggableType) {
          if (event.getGestureSource() != anchorPaneRoot && event.getDragboard().hasImage()) {
            event.acceptTransferModes(TransferMode.MOVE);
          }
        }
        if (currentlyDraggedType != null) {
          draggedEntity.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
        }
        event.consume();
      }
    });

    // this doesn't fire when we drop over GridPane because in the event handler for
    // dropping over GridPanes, we consume the event
    anchorPaneRootSetOnDragDropped.put(draggableType, new EventHandler<DragEvent>() {
      public void handle(DragEvent event) {
        if (currentlyDraggedType == draggableType) {
          // Data dropped
          // If there is an image on the dragboard, read it and use it
          Dragboard db = event.getDragboard();
          Node node = event.getPickResult().getIntersectedNode();
          if (node != anchorPaneRoot && db.hasImage()) {
            // Places at 0,0 - will need to take coordinates once that is implemented
            currentlyDraggedImage.setVisible(true);
            draggedEntity.setVisible(false);
            draggedEntity.setMouseTransparent(false);
            // remove drag event handlers before setting currently dragged image to null
            removeDraggableDragEventHandlers(draggableType, targetGridPane);

            currentlyDraggedImage = null;
            currentlyDraggedType = null;
          }
        }
        // let the source know whether the image was successfully transferred and used
        event.setDropCompleted(true);
        event.consume();
      }
    });
  }

  /**
   * remove the card from the world, and spawn and return a building instead where
   * the card was dropped
   *
   * @param cardNodeX     the x coordinate of the card which was dragged, from 0
   *                      to width-1
   * @param cardNodeY     the y coordinate of the card which was dragged (in
   *                      starter code this is 0 as only 1 row of cards)
   * @param buildingNodeX the x coordinate of the drop location for the card,
   *                      where the building will spawn, from 0 to width-1
   * @param buildingNodeY the y coordinate of the drop location for the card,
   *                      where the building will spawn, from 0 to height-1
   * @return building entity returned from the world
   */
  private Building convertCardToBuildingByCoordinates(int cardNodeX, int cardNodeY, int buildingNodeX,
      int buildingNodeY) {
    return world.convertCardToBuildingByCoordinates(cardNodeX, cardNodeY, buildingNodeX, buildingNodeY);
  }

  /**
   * remove an item from the unequipped inventory by its x and y coordinates in
   * the unequipped inventory gridpane
   *
   * @param nodeX x coordinate from 0 to unequippedInventoryWidth-1
   * @param nodeY y coordinate from 0 to unequippedInventoryHeight-1
   */
  private void removeItemByCoordinates(int nodeX, int nodeY) {
    world.removeUnequippedInventoryItemByCoordinates(nodeX, nodeY);
  }

  /**
   * add drag event handlers to an ImageView
   *
   * @param view           the view to attach drag event handlers to
   * @param draggableType  the type of item being dragged - card or item
   * @param sourceGridPane the relevant gridpane from which the entity would be
   *                       dragged
   * @param targetGridPane the relevant gridpane to which the entity would be
   *                       dragged to
   */
  private void addDragEventHandlers(ImageView view, DRAGGABLE_TYPE draggableType, GridPane sourceGridPane,
      GridPane targetGridPane) {
    view.setOnDragDetected(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        currentlyDraggedImage = view; // set image currently being dragged, so squares setOnDragEntered can detect
                                      // it...
        currentlyDraggedType = draggableType;
        // Drag was detected, start drap-and-drop gesture
        // Allow any transfer node
        Dragboard db = view.startDragAndDrop(TransferMode.MOVE);

        // Put ImageView on dragboard
        ClipboardContent cbContent = new ClipboardContent();
        cbContent.putImage(view.getImage());
        db.setContent(cbContent);
        view.setVisible(false);

        buildNonEntityDragHandlers(draggableType, sourceGridPane, targetGridPane);

        draggedEntity.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
        switch (draggableType) {
          case CARD:
            draggedEntity.setImage(view.getImage());
            break;
          case ITEM:
            draggedEntity.setImage(view.getImage());
            break;
          default:
            break;
        }

        draggedEntity.setVisible(true);
        draggedEntity.setMouseTransparent(true);
        draggedEntity.toFront();

        // IMPORTANT!!!
        // to be able to remove event handlers, need to use addEventHandler
        // https://stackoverflow.com/a/67283792
        targetGridPane.addEventHandler(DragEvent.DRAG_DROPPED, gridPaneSetOnDragDropped.get(draggableType));
        anchorPaneRoot.addEventHandler(DragEvent.DRAG_OVER, anchorPaneRootSetOnDragOver.get(draggableType));
        anchorPaneRoot.addEventHandler(DragEvent.DRAG_DROPPED, anchorPaneRootSetOnDragDropped.get(draggableType));

        for (Node n : targetGridPane.getChildren()) {
          // events for entering and exiting are attached to squares children because that
          // impacts opacity change
          // these do not affect visibility of original image...
          // https://stackoverflow.com/questions/41088095/javafx-drag-and-drop-to-gridpane
          gridPaneNodeSetOnDragEntered.put(draggableType, new EventHandler<DragEvent>() {
            // dropped in the location, the location shouldn't be highlighted!
            public void handle(DragEvent event) {
              if (currentlyDraggedType == draggableType) {
                // The drag-and-drop gesture entered the target
                // show the user that it is an actual gesture target
                if (event.getGestureSource() != n && event.getDragboard().hasImage()) {
                  n.setOpacity(0.7);
                }
              }
              event.consume();
            }
          });
          gridPaneNodeSetOnDragExited.put(draggableType, new EventHandler<DragEvent>() {
            // could program the game so if the new highlight location is invalid the
            // highlighting doesn't change, or leave this as-is
            public void handle(DragEvent event) {
              if (currentlyDraggedType == draggableType) {
                n.setOpacity(1);
              }

              event.consume();
            }
          });
          n.addEventHandler(DragEvent.DRAG_ENTERED, gridPaneNodeSetOnDragEntered.get(draggableType));
          n.addEventHandler(DragEvent.DRAG_EXITED, gridPaneNodeSetOnDragExited.get(draggableType));
        }
        event.consume();
      }

    });
  }

  /**
   * remove drag event handlers so that we don't process redundant events this is
   * particularly important for slower machines such as over VLAB.
   *
   * @param draggableType  either cards, or items in unequipped inventory
   * @param targetGridPane the gridpane to remove the drag event handlers from
   */
  private void removeDraggableDragEventHandlers(DRAGGABLE_TYPE draggableType, GridPane targetGridPane) {
    // remove event handlers from nodes in children squares, from anchorPaneRoot,
    // and squares
    targetGridPane.removeEventHandler(DragEvent.DRAG_DROPPED, gridPaneSetOnDragDropped.get(draggableType));

    anchorPaneRoot.removeEventHandler(DragEvent.DRAG_OVER, anchorPaneRootSetOnDragOver.get(draggableType));
    anchorPaneRoot.removeEventHandler(DragEvent.DRAG_DROPPED, anchorPaneRootSetOnDragDropped.get(draggableType));

    for (Node n : targetGridPane.getChildren()) {
      n.removeEventHandler(DragEvent.DRAG_ENTERED, gridPaneNodeSetOnDragEntered.get(draggableType));
      n.removeEventHandler(DragEvent.DRAG_EXITED, gridPaneNodeSetOnDragExited.get(draggableType));
    }
  }

  /**
   * handle the pressing of keyboard keys. Specifically, we should pause when
   * pressing SPACE
   *
   * @param event some keyboard key press
   */
  @FXML
  public void handleKeyPress(KeyEvent event) {
    switch (event.getCode()) {
      case SPACE:
        if (isPaused) {
          if (!world.isAtHerosCastle()) {
            pauseLabel.setText("");
            startTimer();
          }
        } else {
          if (!world.isAtHerosCastle()) {
            pause();
            pauseLabel.setText("Paused");
            pauseSound();
          }

        }
        break;
      default:
        break;
    }
  }

  public void setMainMenuSwitcher(MenuSwitcher mainMenuSwitcher) {
    this.mainMenuSwitcher = mainMenuSwitcher;
  }

  /**
   * facilitates switching to main game
   */
  public void setGameSwitcher(MenuSwitcher gameSwitcher) {
    this.gameSwitcher = gameSwitcher;
  }

  /**
   * facilitates switching to main game upon button click
   *
   * @throws IOException
   */
  @FXML
  private void switchToGame() throws IOException {
    gameSwitcher.switchMenu();
  }

  /**
   * this method is triggered when click button to go to main menu in FXML
   *
   * @throws IOException
   */
  @FXML
  private void switchToMainMenu() throws IOException {
    pause();
    // stop the game music
    changeMusicState();
    mainMenuSwitcher.switchMenu();
  }

  /**
   * facilitates switching to main game
   */
  public void setWinSwitcher(MenuSwitcher gameSwitcher) {
    this.winSwitcher = gameSwitcher;
  }

  /**
   * facilitates switching to main game upon button click
   *
   * @throws IOException
   */
  @FXML
  private void switchToWin() throws IOException {
    winSwitcher.switchMenu();
  }

  /**
   * facilitates switching to main game
   */
  public void setLoseSwitcher(MenuSwitcher gameSwitcher) {
    this.loseSwitcher = gameSwitcher;
  }

  /**
   * facilitates switching to main game upon button click
   *
   * @throws IOException
   */
  @FXML
  private void switchToLose() throws IOException {
    loseSwitcher.switchMenu();
  }

  /**
   * Change the condition of the music
   */
  private void changeMusicState() {
    gameplayMusic.stop();
    isPlayingGameMusic = false;
    isPlayingMenuMusic = true;
    buttonClickSound();
  }

  /**
   * Set a node in a GridPane to have its position track the position of an entity
   * in the world.
   *
   * By connecting the model with the view in this way, the model requires no
   * knowledge of the view and changes to the position of entities in the model
   * will automatically be reflected in the view.
   *
   * note that this is put in the controller rather than the loader because we
   * need to track positions of spawned entities such as enemy or items which
   * might need to be removed should be tracked here
   *
   * NOTE teardown functions setup here also remove nodes from their GridPane. So
   * it is vital this is handled in this Controller class
   *
   * @param entity
   * @param node
   */
  private void trackPosition(Entity entity, Node node) {
    GridPane.setColumnIndex(node, entity.getX());
    GridPane.setRowIndex(node, entity.getY());

    ChangeListener<Number> xListener = new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        GridPane.setColumnIndex(node, newValue.intValue());
      }
    };
    ChangeListener<Number> yListener = new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        GridPane.setRowIndex(node, newValue.intValue());
      }
    };

    // if need to remove items from the equipped inventory, add code to remove from
    // equipped inventory gridpane in the .onDetach part
    ListenerHandle handleX = ListenerHandles.createFor(entity.x(), node).onAttach((o, l) -> o.addListener(xListener))
        .onDetach((o, l) -> {
          o.removeListener(xListener);
          entityImages.remove(node);
          squares.getChildren().remove(node);
          cards.getChildren().remove(node);
          equippedItems.getChildren().remove(node);
          unequippedInventory.getChildren().remove(node);
        }).buildAttached();
    ListenerHandle handleY = ListenerHandles.createFor(entity.y(), node).onAttach((o, l) -> o.addListener(yListener))
        .onDetach((o, l) -> {
          o.removeListener(yListener);
          entityImages.remove(node);
          squares.getChildren().remove(node);
          cards.getChildren().remove(node);
          equippedItems.getChildren().remove(node);
          unequippedInventory.getChildren().remove(node);
        }).buildAttached();
    handleX.attach();
    handleY.attach();

    // this means that if we change boolean property in an entity tracked from here,
    // position will stop being tracked
    // this wont work on character/path entities loaded from loader classes
    entity.shouldExist().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> obervable, Boolean oldValue, Boolean newValue) {
        handleX.detach();
        handleY.detach();
      }
    });
  }

  /**
   * we added this method to help with debugging so you could check your code is
   * running on the application thread. By running everything on the application
   * thread, you will not need to worry about implementing locks, which is outside
   * the scope of the course. Always writing code running on the application
   * thread will make the project easier, as long as you are not running
   * time-consuming tasks. We recommend only running code on the application
   * thread, by using Timelines when you want to run multiple processes at once.
   * EventHandlers will run on the application thread.
   */
  private void printThreadingNotes(String currentMethodLabel) {
    // System.out.println("\n###########################################");
    // System.out.println("current method = " + currentMethodLabel);
    // System.out.println("In application thread? =" +
    // Platform.isFxApplicationThread());
    // System.out.println("Current system time =" +
    // java.time.LocalDateTime.now().toString().replace('T', ' '));
  }

  /**
   * music for the main menu
   */
  public void gameMusic() {
    String path = "src/audio/MainGameMusic.mp3";
    Media music = new Media(Paths.get(path).toUri().toString());
    gameplayMusic = new MediaPlayer(music);
    gameplayMusic.setOnEndOfMedia(new Runnable() {
      public void run() {
        gameplayMusic.seek(Duration.ZERO);
      }
    });
    gameplayMusic.play();
    gameplayMusic.setVolume(0.03);
  }

  /**
   * sound effect for the button clicking
   */
  public void buttonClickSound() {
    String path = "src/audio/buttonClick.wav";
    Media music = new Media(Paths.get(path).toUri().toString());
    buttonClick = new MediaPlayer(music);
    buttonClick.play();
  }

  /**
   * sound effect for the selling item
   */
  public void sellSound() {
    String path = "src/audio/coin.wav";
    Media music = new Media(Paths.get(path).toUri().toString());
    sellSound = new MediaPlayer(music);
    sellSound.setVolume(0.04);
    sellSound.play();
  }

  /**
   * sound effect for the selling item
   */
  public void buySound() {
    String path = "src/audio/coin2.wav";
    Media music = new Media(Paths.get(path).toUri().toString());
    buySound = new MediaPlayer(music);
    buySound.setVolume(0.1);
    buySound.play();
  }

  /**
   * sound effect for the pausing screen
   */
  public void pauseSound() {
    String path = "src/audio/pause.mp3";
    Media music = new Media(Paths.get(path).toUri().toString());
    pauseSound = new MediaPlayer(music);
    pauseSound.setVolume(0.35);
    pauseSound.play();
  }

}
