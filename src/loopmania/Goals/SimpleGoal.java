package loopmania.Goals;

public class SimpleGoal implements ComplexNode {
  private int quantity;
  private boolean value;
  private String goalType;

  /**
   * Create a simple goal varying on the goal type
   *
   * @param goalValue the amount needed to complete its respective goal
   * @param goalType  a string which classify the simple goal (gold, exp, cycle)
   */
  public SimpleGoal(int goalValue, String goalType) {
    this.quantity = goalValue;
    this.value = false;
    this.goalType = goalType;
  }

  /**
   * Get the quantity for a particular goal
   *
   * @return the quantitiy for the goal
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * Set the quantity for a particular goal
   *
   * @param goalValue the new quantitiy for the goal
   */
  public void setGoalValue(int goalValue) {
    this.quantity = goalValue;
  }

  /**
   * Check if a goal is completed
   *
   * @return the boolean for if the goal is completed
   */
  public boolean isValue() {
    return value;
  }

  /**
   * Set a goal's status to either completed or not
   *
   * @param goalCheck the boolean to change the state of goal check
   */
  public void setGoalCheck(boolean goalCheck) {
    this.value = goalCheck;
  }

  /**
   * Set a goal's type to differentiate other goals
   *
   * @param goalType the type that will classify the goal
   */
  public void setGoalType(String goalType) {
    this.goalType = goalType;
  }

  /**
   * Gets the type of the goal
   *
   * @return the goal type
   */
  public String getGoalType() {
    return goalType;
  }

  /**
   * Checks if the value meets the goal's quantity requirement
   *
   * @param value check if the value has meet the goal's quantity
   */
  public boolean goalMeetsRequirement(int value) {
    if ((value >= quantity) && !isValue()) {
      setGoalCheck(true);
      return true;
    }
    return false;
  }

  /**
   * Returns the completion status of the goal
   */
  public boolean evaluate() {
    return value;
  }

  /**
   * Checks if goal is a gold goal
   *
   * @return true if gold goal else false
   */
  public boolean isGoldGoal() {
    return getGoalType().equals(Goal.GOLD_GOAL);
  }

  /**
   * Checks if goal is a experience goal
   *
   * @return true if experience goal else false
   */
  public boolean isExperienceGoal() {
    return getGoalType().equals(Goal.EXPERIENCE_GOAL);
  }

  /**
   * Checks if goal is a cycle goal
   *
   * @return true if cycle goal else false
   */
  public boolean isCycleGoal() {
    return getGoalType().equals(Goal.CYCLE_GOAL);
  }

  /**
   * Checks if goal is a boss goal
   *
   * @return true if boss goal else false
   */
  public boolean isBossGoal() {
    return getGoalType().equals(Goal.BOSS_GOAL);
  }

  /**
   * Update the goal status as completed once the requirement has been met
   *
   * @param amount   the quantity needed to complete a particular goal
   * @param goalType to compare the quantity with its matching goal type
   */
  public void updateValue(int amount, String goalType) {
    if (goalType.equals(this.goalType) && (amount >= quantity) && !isValue()) {
      setGoalCheck(true);
    }

  }

  @Override
  public String getValue() {
    if (goalType.equals(Goal.EXPERIENCE_GOAL)) {
      return ("obtaining " + quantity + " experience points");
    } else if (goalType.equals(Goal.CYCLE_GOAL)) {
      return ("completing " + quantity + " cycles");
    } else if (goalType.equals(Goal.GOLD_GOAL)) {
      return ("amassing " + quantity + " gold");
    }

    return ("no goal");
  }
}
