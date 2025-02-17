package loopmania.Goals;

import java.util.ArrayList;
import java.util.List;

public class Goal {
  private List<SimpleGoal> simpleGoals;
  private List<ComplexGoal> complexGoals;
  public static final String EXPERIENCE_GOAL = "Experience";
  public static final String GOLD_GOAL = "Gold";
  public static final String CYCLE_GOAL = "Cycle";
  public static final String BOSS_GOAL = "Boss";

  /**
   * A goal constructur which will hold lists of simple goals
   */
  public Goal() {
    this.simpleGoals = new ArrayList<SimpleGoal>();
    this.complexGoals = new ArrayList<ComplexGoal>();
  }

  /**
   * Add the simple goal into the goal list
   *
   * @param goal the goal will be added into the goal list
   */
  public void addSimpleGoal(SimpleGoal goal) {
    simpleGoals.add(goal);
  }

  /**
   * Add the simple goal into the goal list
   *
   * @param goal the goal will be added into the goal list
   */
  public void addComplexGoal(ComplexGoal goal) {
    complexGoals.add(goal);
  }

  /**
   * Get the list of simple goals
   *
   * @return the list of simple goals
   */
  public List<SimpleGoal> getSimpleGoals() {
    return simpleGoals;
  }

  /**
   * Get the list of goals
   *
   * @return the list of complex goals
   */
  public List<ComplexGoal> getComplexGoals() {
    return complexGoals;
  }

  /**
   * Set the list of the goals
   *
   * @param goals the set of new goals
   */
  public void setGoals(List<SimpleGoal> goals) {
    this.simpleGoals = goals;
  }

  /**
   * A method which checks each goal whether it has meet the requirement
   *
   * @return the boolean if the character has meet all the requirements
   */
  public boolean isGameWon() {
    return isSimpleCompleted() || isComplexCompleted();
  }

  /**
   * Check if the user has meet the requirement for the experience goal
   *
   * @param exp the exp will be compared to the goal's quantity
   */
  public void updateExperienceStatus(int exp) {
    // update goal for simple goals
    for (SimpleGoal g : simpleGoals) {
      if (g.isExperienceGoal() && g.goalMeetsRequirement(exp)) {
        g.setGoalCheck(true);
      }
    }
    // update goal for complex goals
    updateComplexGoals(exp, Goal.EXPERIENCE_GOAL);
  }

  /**
   * Check if the user has meet the requirement for the gold goal
   *
   * @param gold the gold will be compared to the goal's quantity
   */
  public void updateGoldStatus(int gold) {
    for (SimpleGoal g : simpleGoals) {
      if (g.isGoldGoal() && g.goalMeetsRequirement(gold)) {
        g.setGoalCheck(true);
      }
    }
    // update goal for complex goals
    updateComplexGoals(gold, Goal.GOLD_GOAL);
  }

  /**
   * Check if the user has meet the requirement for the cycle goal
   *
   * @param cycle the cycle will be compared to the goal's quantity
   */
  public void updateCycleStatus(int cycle) {
    for (SimpleGoal g : simpleGoals) {
      if (g.isCycleGoal() && g.goalMeetsRequirement(cycle)) {
        g.setGoalCheck(true);
      }
    }
    // update goal for complex goals
    updateComplexGoals(cycle, Goal.CYCLE_GOAL);
  }

  /**
   * Check if the user has meet the requirement for the boss goal
   *
   * @param bossCount the kill count will be compared to the goal's quantity
   */
  public void updateBossStatus(int bossCount) {
    for (SimpleGoal g : simpleGoals) {
      if (g.isBossGoal() && g.goalMeetsRequirement(bossCount)) {
        g.setGoalCheck(true);
      }
    }
    // update goal for complex goals
    updateComplexGoals(bossCount, Goal.BOSS_GOAL);
  }

  /**
   * A helper function for isGameWon() Check if the simple goal is completed
   *
   * @return the boolean to indicate if the simple goal is completed
   */
  public boolean isSimpleCompleted() {
    if (getSimpleGoals().size() != 0) {
      int count = 0;
      int numGoals = getSimpleGoals().size();
      for (SimpleGoal g : getSimpleGoals()) {
        if (g.isValue())
          count++;
      }

      // game won
      if (count == numGoals) {
        return true;
      }
    }
    return false;
  }

  /**
   * A helper function for isGameWon() Check if the complex goal is completed
   *
   * @return the boolean to indicate if the complex goal is completed
   */
  public boolean isComplexCompleted() {
    for (ComplexGoal g : getComplexGoals()) {
      if (g.evaluate()) {
        // all the requirement has met
        return true;
      }
    }
    // hasn't been completed yet
    return false;
  }

  /**
   * Update the complex goal status as the observer detects the changes
   *
   * @param value    the quantity that the character holds for either exp, cycle
   *                 or gold
   * @param goalType the type that distinguish individial simple goal
   */
  public void updateComplexGoals(int value, String goalType) {
    for (ComplexGoal g : getComplexGoals()) {
      g.updateValue(value, goalType);
    }
  }
}
