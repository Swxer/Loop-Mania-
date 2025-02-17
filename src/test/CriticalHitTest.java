package test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import loopmania.Character;
import loopmania.LoopManiaWorld;
import loopmania.PathPosition;
import loopmania.Enemies.VampireEnemy;
import loopmania.Enemies.ZombieEnemy;

public class CriticalHitTest {
  @Test
  public void testVampireCriticalHit() {
    // tests that vampire deals additional damage after it crits
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    Character testChar = new Character(new PathPosition(1, world.getOrderedPath()));
    world.setCharacter(testChar);
    VampireEnemy vampire = new VampireEnemy(new PathPosition(1, world.getOrderedPath()));
    // set the vampire's crit rate too 100% for testing
    vampire.setCritRate(100);
    world.addEnemy(vampire);
    world.runBattles();
    // check that the character hp is lower than 40, which is what it would
    // have been if the vampire never crit
    assertTrue(testChar.getHealth() < 40);
  }

  @Test
  public void testZombieCriticalHit() {
    // tests that zombies turn allies into zombie after it crits
    TestSetup setup = new TestSetup();
    LoopManiaWorld world = setup.makeTestWorld();
    Character testChar = new Character(new PathPosition(1, world.getOrderedPath()));
    world.setCharacter(testChar);
    ZombieEnemy zombie = new ZombieEnemy(new PathPosition(1, world.getOrderedPath()));
    // set the zombies's crit rate too 100% for testing
    zombie.setCritRate(100);
    world.addEnemy(zombie);
    testChar.addSoldier();
    // check there is intially one ally soldier
    assertTrue(testChar.getSoldiers().size() == 1);
    world.runBattles();
    // check that after zombie crit, the solider turned into a zombie
    assertTrue(testChar.getSoldiers().size() == 0);
  }

}
