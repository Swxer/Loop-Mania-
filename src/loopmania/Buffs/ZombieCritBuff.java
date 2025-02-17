package loopmania.Buffs;

import java.util.List;

import loopmania.Character;
import loopmania.Soldier;
import loopmania.Enemies.Enemy;

public class ZombieCritBuff extends Buff {
  public ZombieCritBuff() {
    return;
  }

  @Override
  public void activateEffect(Character character, Enemy enemy, List<Soldier> allyList, List<Enemy> zombieSoldiers) {
    return;
  };

  @Override
  public void activateEffect(Soldier soldier, Enemy enemy, List<Soldier> allyList, List<Enemy> zombieSoldiers) {
    soldier.reduceHealth(20);
    Enemy newZombie = enemy;
    zombieSoldiers.add(newZombie);
  };
}
