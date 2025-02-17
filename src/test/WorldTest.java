package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleIntegerProperty;
import loopmania.Character;
import loopmania.LoopManiaWorld;
import loopmania.PathPosition;
import loopmania.Buildings.Building;
import loopmania.Cards.BarracksCardStrategy;
import loopmania.Cards.CampfireCardStrategy;
import loopmania.Cards.Card;
import loopmania.Cards.CardStrategy;
import loopmania.Cards.TowerCardStrategy;
import loopmania.Cards.TrapCardStrategy;
import loopmania.Cards.VampireCastleCardStrategy;
import loopmania.Cards.VillageCardStrategy;
import loopmania.Cards.ZombiePitCardStrategy;

public class WorldTest {

  @Test
  public void testCardToBuilding() {
    // check that card to building generates a building and destroys the card
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    // create card
    world.generateCardDrops();
    Card testCard = world.loadCard();
    // convert card to building
    Building testBuilding = world.convertCardToBuildingByCoordinates(testCard.getX(), testCard.getY(), 1, 1);
    if (testBuilding == null) {
      // building type was not able to be placed on tile
      testBuilding = world.convertCardToBuildingByCoordinates(testCard.getX(), testCard.getY(), 2, 2);
    }
    assertNotEquals(null, testBuilding);
    // get list of buildings after card is used
    List<Building> buildingList = world.getBuildings();
    // assert second building list is greater than the first by 1
    assertEquals(1, buildingList.size());
    // assert there are no cards left
    assertEquals(0, world.getCards().size());

  }

  @Test
  public void testLoadCard() {
    // Test that load card creates a card in the world
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    // create card
    world.generateCardDrops();
    world.loadCard();
    List<Card> cardList = world.getCards();
    assertEquals(cardList.size(), 1);

  }

  @Test
  public void testCardNextPath() {
    // test behaviour of cardtobuilding when a card is next to path
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    // create card
    Card testCard = new Card(new SimpleIntegerProperty(0), new SimpleIntegerProperty(0), new CampfireCardStrategy());
    world.addCard(testCard);
    // test that isNeighbourPath returns the expected values for 1,1 and 2,2
    assertEquals(true, world.isNeighbourPath(1, 1));
    assertEquals(true, world.isNeighbourPath(2, 2));
    // convert card to building
    Building testBuilding = world.convertCardToBuildingByCoordinates(testCard.getX(), testCard.getY(), 1, 1);
    if (testBuilding == null) {
      // building type was not able to be placed on tile
      testBuilding = world.convertCardToBuildingByCoordinates(testCard.getX(), testCard.getY(), 2, 2);
    }
    assertNotEquals(null, testBuilding);
    // get list of buildings after card is used
    List<Building> buildingList = world.getBuildings();
    // assert second building list is greater than the first by 1
    assertEquals(1, buildingList.size());
    // assert there are no cards left
    assertEquals(0, world.getCards().size());
  }

  @Test
  public void testCardOnPath() {
    // test behaviour of cardtobuilding when a card on path
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    // create card
    Card testCard = new Card(new SimpleIntegerProperty(0), new SimpleIntegerProperty(0), new VillageCardStrategy());
    world.addCard(testCard);
    // test that isNeighbourPath returns the expected values
    assertEquals(true, world.isNeighbourPath(2, 2));
    assertEquals(false, world.isNeighbourPath(3, 3));
    assertEquals(true, world.isNeighbourPath(0, 0));
    assertEquals(true, world.isNeighbourPath(0, 1));
    assertEquals(true, world.isNeighbourPath(0, 2));

    // convert card to building on path
    Building testBuilding = world.convertCardToBuildingByCoordinates(testCard.getX(), testCard.getY(), 1, 1);
    assertEquals(null, testBuilding);
    // convert card to building off path
    testBuilding = world.convertCardToBuildingByCoordinates(testCard.getX(), testCard.getY(), 2, 2);
    assertNotEquals(null, testBuilding);
    // get list of buildings after card is used
    List<Building> buildingList = world.getBuildings();
    // assert second building list is greater than the first by 1
    assertEquals(1, buildingList.size());
    // assert there are no cards left
    assertEquals(0, world.getCards().size());
  }

  @Test
  public void testLoadCardMax() {
    // Test that load card destorys a card when player has max cards
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    Character testChar = new Character(new PathPosition(1, world.getOrderedPath()));
    world.setCharacter(testChar);
    // create cards until max ammount (width of world) is reached
    world.generateCardDrops();
    for (int i = 0; i < world.getWidth(); i++) {
      world.loadCard();
    }
    List<Card> cardListBefore = world.getCards();
    // load another card after max has been reached
    world.loadCard();
    List<Card> cardListAfter = world.getCards();
    // assert that the ammount of cards hasn't changed
    assertEquals(cardListBefore, cardListAfter);
    // assert that player gains exp and gold from card
    assertEquals(50, testChar.getExp());
    assertEquals(50, testChar.getGold());
  }

  @Test
  public void testNeighbourPath() {
    // test that the neighbourPath method returns the coords of an adjacent path
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    // test for 1,1
    Pair<Integer, Integer> neighbourPath = world.neighbourPath(1, 1);
    Pair<Integer, Integer> expected = new Pair<>(1, 0);
    assertEquals(expected, neighbourPath);
    // test for 2,2
    neighbourPath = world.neighbourPath(2, 2);
    expected = new Pair<>(2, 1);
    assertEquals(expected, neighbourPath);
    // test for 0,0
    neighbourPath = world.neighbourPath(0, 0);
    expected = new Pair<>(1, 0);
    assertEquals(expected, neighbourPath);
    // test for 0,2
    neighbourPath = world.neighbourPath(0, 2);
    expected = new Pair<>(1, 2);
    assertEquals(expected, neighbourPath);
    // test for 2,0
    neighbourPath = world.neighbourPath(2, 0);
    expected = new Pair<>(1, 0);
    assertEquals(expected, neighbourPath);
    // test for null
    neighbourPath = world.neighbourPath(4, 4);
    assertEquals(null, neighbourPath);
  }

  @Test
  public void testHighRarityCards() {
    // test that specific item will spawn from certain rarity categories.

    // set up that will spawn high rarity card
    TestSetupWithSeed setup = new TestSetupWithSeed();
    LoopManiaWorld world = setup.makeTestWorld(37);
    Character testChar = new Character(new PathPosition(1, world.getOrderedPath()));
    world.setCharacter(testChar);
    // generate card
    world.generateCardDrops();
    // spawn high rarity card
    CardStrategy highRareCard = world.randomCardStrategy();
    assertEquals(true, highRareCard instanceof TowerCardStrategy || highRareCard instanceof BarracksCardStrategy);
  }

  @Test
  public void testMediumRarityCards() {
    // test that specific item will spawn from certain rarity categories.

    // set up that will spawn high rarity card
    TestSetupWithSeed setup = new TestSetupWithSeed();
    LoopManiaWorld world = setup.makeTestWorld(65);
    Character testChar = new Character(new PathPosition(1, world.getOrderedPath()));
    world.setCharacter(testChar);
    // generate card
    world.generateCardDrops();
    // spawn medium rarity card
    CardStrategy medRareCard = world.randomCardStrategy();
    assertEquals(true, medRareCard instanceof VillageCardStrategy || medRareCard instanceof CampfireCardStrategy);
  }

  @Test
  public void testLowRarityCards() {
    // test that specific item will spawn from certain rarity categories.

    // set up that will spawn high rarity card
    TestSetupWithSeed setup = new TestSetupWithSeed();
    LoopManiaWorld world = setup.makeTestWorld(82);
    Character testChar = new Character(new PathPosition(1, world.getOrderedPath()));
    world.setCharacter(testChar);
    // generate card
    world.generateCardDrops();
    // spawn low rarity card
    CardStrategy lowRareCard = world.randomCardStrategy();
    assertEquals(true, lowRareCard instanceof ZombiePitCardStrategy || lowRareCard instanceof TrapCardStrategy
        || lowRareCard instanceof VampireCastleCardStrategy);
  }

}
