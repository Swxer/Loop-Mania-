package test;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONObject;

import javafx.beans.property.SimpleIntegerProperty;
import loopmania.LoopManiaWorld;
import loopmania.PathTile;
import loopmania.Goals.CycleGoal;
import loopmania.Goals.Goal;

public class TestSetup {
  public LoopManiaWorld makeTestWorld() {
    ArrayList<String> pathString = new ArrayList<>();
    pathString.add("RIGHT");
    pathString.add("RIGHT");
    pathString.add("DOWN");
    pathString.add("DOWN");
    pathString.add("LEFT");
    pathString.add("LEFT");
    pathString.add("UP");
    pathString.add("UP");

    JSONObject path = new JSONObject();
    path.put("type", "path_tile");
    path.put("x", 0);
    path.put("y", 0);
    path.put("path", pathString);

    Goal gg = new Goal();
    gg.addSimpleGoal(new CycleGoal(200));

    List<Pair<Integer, Integer>> orderedPath = loadPathTiles(path, 3, 3);
    return new LoopManiaWorld(3, 3, orderedPath, gg);
  }

  public List<Pair<Integer, Integer>> loadPathTiles(JSONObject path, int width, int height) {
    if (!path.getString("type").equals("path_tile")) {
      // ... possible extension
      throw new RuntimeException("Path object requires path_tile type.  No other path types supported at this moment.");
    }
    PathTile starting = new PathTile(new SimpleIntegerProperty(path.getInt("x")),
        new SimpleIntegerProperty(path.getInt("y")));
    if (starting.getY() >= height || starting.getY() < 0 || starting.getX() >= width || starting.getX() < 0) {
      throw new IllegalArgumentException("Starting point of path is out of bounds");
    }
    // load connected path tiles
    List<PathTile.Direction> connections = new ArrayList<>();
    for (Object dir : path.getJSONArray("path").toList()) {
      connections.add(Enum.valueOf(PathTile.Direction.class, dir.toString()));
    }

    if (connections.size() == 0) {
      throw new IllegalArgumentException(
          "This path just consists of a single tile, it needs to consist of multiple to form a loop.");
    }

    // load the first position into the orderedPath
    PathTile.Direction first = connections.get(0);
    List<Pair<Integer, Integer>> orderedPath = new ArrayList<>();
    orderedPath.add(Pair.with(starting.getX(), starting.getY()));

    int x = starting.getX() + first.getXOffset();
    int y = starting.getY() + first.getYOffset();

    // add all coordinates of the path into the orderedPath
    for (int i = 1; i < connections.size(); i++) {
      orderedPath.add(Pair.with(x, y));

      if (y >= height || y < 0 || x >= width || x < 0) {
        throw new IllegalArgumentException(
            "Path goes out of bounds at direction index " + (i - 1) + " (" + connections.get(i - 1) + ")");
      }

      PathTile.Direction dir = connections.get(i);
      x += dir.getXOffset();
      y += dir.getYOffset();
      if (orderedPath.contains(Pair.with(x, y)) && !(x == starting.getX() && y == starting.getY())) {
        throw new IllegalArgumentException("Path crosses itself at direction index " + i + " (" + dir + ")");
      }
    }
    // we should connect back to the starting point
    if (x != starting.getX() || y != starting.getY()) {
      throw new IllegalArgumentException(String.format(
          "Path must loop back around on itself, this path doesn't finish where it began, it finishes at %d, %d.", x,
          y));
    }
    return orderedPath;
  }
}
