package com.example.algorithmsnake;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

// class for npc snake pathfinding
public class Pathfinder {
    public static List<Point> aStar(Snake snake, Eatable eatable, List<List<Point>> allSnakeBodies, boolean optimal, int minPos, int maxPos) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Point, Node> nodes = new HashMap<>();
        Set<Point> visited = new HashSet<>();
        Point start = snake.getHead();
        Point end = eatable.position;
        Node startNode = new Node(start, null, 0, heuristic(start, end));
        nodes.put(start, startNode);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.position.equals(end)) {
                return reconstructPath(current);
            }
            visited.add(current.position);

            for (Point neighbor : getNeighbors(current.position, minPos, maxPos)) {
                if (visited.contains(neighbor) || isObstacle(neighbor, allSnakeBodies, snake)) {
                    continue;
                }
                int g = current.g + 1;
                int h = heuristic(neighbor, end);
                Node neighborNode = nodes.getOrDefault(neighbor, new Node(neighbor, null, Integer.MAX_VALUE, h));
                if (g < neighborNode.g) {
                    neighborNode.g = g;
                    neighborNode.f = g + h;
                    neighborNode.parent = current;
                    nodes.put(neighbor, neighborNode);

                    if (!optimal && neighbor.equals(end)) {
                        return reconstructPath(neighborNode);
                    }
                    queue.add(neighborNode);
                }
            }
        }
        return new ArrayList<>(); // no path found
    }

    public static List<Point> bfs(Snake snake, Eatable eatable, List<List<Point>> allSnakeBodies, boolean optimal, int minPos, int maxPos) {
        Queue<Node> queue = new LinkedList<>();
        Map<Point, Node> visited = new HashMap<>();
        Point start = snake.getHead();
        Point end = eatable.position;
        Node startNode = new Node(start, null, 0, 0);
        queue.add(startNode);
        visited.put(start, startNode);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.position.equals(end)) {
                return reconstructPath(current);
            }
            for (Point neighbor : getNeighbors(current.position, minPos, maxPos)) {
                if (!visited.containsKey(neighbor) && !isObstacle(neighbor, allSnakeBodies, snake)) {
                    int g = current.g + 1;
                    Node neighborNode = new Node(neighbor, current, g, 0);
                    visited.put(neighbor, neighborNode);
                    queue.add(neighborNode);
                    if (!optimal && neighbor.equals(end)) {
                        return reconstructPath(neighborNode);
                    }
                }
            }
        }
        return new ArrayList<>(); //no path found
    }

    public static List<Point> dijkstra(Snake snake, Eatable eatable, List<List<Point>> allSnakeBodies, boolean optimal, int minPos, int maxPos) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.g));
        Map<Point, Integer> bestG = new HashMap<>();
        Point start = snake.getHead();
        Point end = eatable.position;
        Node startNode = new Node(start, null, 0, 0);
        bestG.put(start, 0);
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.g > bestG.getOrDefault(current.position, Integer.MAX_VALUE)) {
                continue;
            }
            if (current.position.equals(end)) {
                return reconstructPath(current);
            }
            for (Point neighbor : getNeighbors(current.position, minPos, maxPos)) {
                if (isObstacle(neighbor, allSnakeBodies, snake)) {
                    continue;
                }
                int tentativeG = current.g + 1;
                if (tentativeG < bestG.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    bestG.put(neighbor, tentativeG);
                    Node neighborNode = new Node(neighbor, current, tentativeG, 0);
                    queue.add(neighborNode);
                }
            }
        }
        return new ArrayList<>();
    }

    private static int heuristic(Point a, Point b) {
        // Manhattan distance
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static List<Point> reconstructPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(0, node.position);
            node = node.parent;
        }
        return path;
    }

    private static List<Point> getNeighbors(Point p, int minPos, int maxPos) {
        List<Point> neighbors = new ArrayList<>();
        if (p.x < maxPos) neighbors.add(new Point(p.x + 1, p.y));
        if (p.x > minPos) neighbors.add(new Point(p.x - 1, p.y));
        if (p.y < maxPos) neighbors.add(new Point(p.x, p.y + 1));
        if (p.y > minPos) neighbors.add(new Point(p.x, p.y - 1));
        return neighbors;
    }

    private static boolean isObstacle(Point p, List<List<Point>> bodies, Snake currentSnake) {
        for (List<Point> body : bodies) {
            if (body == currentSnake.body) {
                // tail is no obstacle because moves
                for (int i = 0; i < body.size() - 1; i++) {
                    if (body.get(i).equals(p)) {
                        return true;
                    }
                }
            } else {
                if (body.contains(p)) {
                    return true;
                }
            }
        }
        return false;
    }
}

class Node implements Comparable<Node> {
    public Point position;
    public Node parent;
    public int g;
    public int f;

    public Node(Point position, Node parent, int g, int h) {
        this.position = position;
        this.parent = parent;
        this.g = g;
        this.f = g + h;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.f, other.f);
    }
}
