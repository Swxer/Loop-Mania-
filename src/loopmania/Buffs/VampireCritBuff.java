package loopmania.Buffs;

import java.util.List;
import java.util.Random;

import loopmania.Character;
import loopmania.Soldier;
import loopmania.Enemies.Enemy;
import loopmania.Enemies.VampireEnemy;

public class VampireCritBuff extends Buff {

  public VampireCritBuff() {
    return;
  }

  @Override
  public void activateEffect(Character character, Enemy enemy, List<Soldier> allyList, List<Enemy> zombieSoldiers) {
    if (enemy instanceof VampireEnemy) {
      subtractTurns(1);
      Random random = new Random();
      int randInt = random.nextInt(6) + 5;
      character.reduceHealth(randInt);
    }
  };

  @Override
  public void activateEffect(Soldier soldier, Enemy enemy, List<Soldier> allyList, List<Enemy> zombieSoldiers) {
    if (enemy instanceof VampireEnemy) {
      subtractTurns(1);
      Random random = new Random();
      int randInt = random.nextInt(6) + 5;
      soldier.reduceHealth(randInt);
    }
  };
}
