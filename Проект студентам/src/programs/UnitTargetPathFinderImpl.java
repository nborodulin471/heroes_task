package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.EdgeDistance;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Реализация интерфейса UnitTargetPathFinder.
 *
 * @author Бородулин Никита Петрович.
 */
public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    /**
     * Метод определяет кратчайший маршрут между атакующим и атакуемым юнитом и возвращает его в виде списка объектов
     * содержащих координаты каждой точки данного кратчайшего пути.
     *
     * @param attackUnit       юнит, который атакует.
     * @param targetUnit       юнит, который подвергается атаке.
     * @param existingUnitList список всех существующих юнитов.
     *
     * @return Cписок объектов Edge, т.е. координат клеток пути от атакующего юнита до атакуемого юнита включительно.
     *         Если маршрут не найден — возвращает пустой список.
     */
    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        var distances = initializeDistances();
        var visited = new boolean[WIDTH][HEIGHT];
        var predecessors = new Edge[WIDTH][HEIGHT];
        var blockedCells = getBlockedCells(existingUnitList, attackUnit, targetUnit);

        var priorityQueue = new PriorityQueue<>(Comparator.comparingInt(EdgeDistance::getDistance));
        addStartingPointToQueue(priorityQueue, attackUnit, distances);

        while (!priorityQueue.isEmpty()) {
            var current = priorityQueue.poll();
            if (visited[current.getX()][current.getY()]) {
                continue;
            }
            visited[current.getX()][current.getY()] = true;

            if (targetReached(current, targetUnit)) {
                break;
            }

            updateNeighbors(current, blockedCells, distances, predecessors, priorityQueue);
        }

        return buildPath(predecessors, targetUnit);
    }

    /**
     * Инициализация массива расстояний.
     */
    private int[][] initializeDistances() {
        var distances = new int[WIDTH][HEIGHT];
        for (int[] row : distances) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        return distances;
    }

    /**
     * Получение заблокированных клеток
     */
    private Set<String> getBlockedCells(List<Unit> existingUnitList, Unit attackUnit, Unit targetUnit) {
        var blockedCells = new HashSet<String>();
        for (Unit unit : existingUnitList) {
            if (unit.isAlive() && unit != attackUnit && unit != targetUnit) {
                blockedCells.add(unit.getxCoordinate() + "," + unit.getyCoordinate());
            }
        }
        return blockedCells;
    }

    /**
     * Добавление начальной точки в очередь
     */
    private void addStartingPointToQueue(Queue<EdgeDistance> priorityQueue, Unit attackUnit, int[][] distances) {
        var x = attackUnit.getxCoordinate();
        var y = attackUnit.getyCoordinate();
        distances[x][y] = 0;
        priorityQueue.offer(new EdgeDistance(x, y, 0));
    }

    /**
     * Проверка достижения цели.
     */
    private boolean targetReached(EdgeDistance current, Unit targetUnit) {
        return current.getX() == targetUnit.getxCoordinate() && current.getY() == targetUnit.getyCoordinate();
    }

    /**
     * Обновление соседних клеток.
     */
    private void updateNeighbors(EdgeDistance current, Set<String> blockedCells,
                                 int[][] distances, Edge[][] predecessors, Queue<EdgeDistance> priorityQueue) {
        for (int[] direction : DIRECTIONS) {

            var neighborX = current.getX() + direction[0];
            var neighborY = current.getY() + direction[1];

            if (isValid(neighborX, neighborY, blockedCells)) {

                var newDistance = distances[current.getX()][current.getY()] + 1;
                if (newDistance < distances[neighborX][neighborY]) {
                    distances[neighborX][neighborY] = newDistance;
                    predecessors[neighborX][neighborY] = new Edge(current.getX(), current.getY());
                    priorityQueue.offer(new EdgeDistance(neighborX, neighborY, newDistance));
                }
            }
        }
    }

    /**
     * Проверка валидности клетки.
     */
    private boolean isValid(int x, int y, Set<String> blockedCells) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && !blockedCells.contains(x + "," + y);
    }

    /**
     * Собирает путь.
     */
    private List<Edge> buildPath(Edge[][] cameFrom, Unit targetUnit) {
        var path = new ArrayDeque<Edge>();
        var x = targetUnit.getxCoordinate();
        var y = targetUnit.getyCoordinate();

        while (true) {
            var predecessor = cameFrom[x][y];
            if (predecessor == null) {
                break;
            }
            path.push(new Edge(x, y));
            x = predecessor.getX();
            y = predecessor.getY();
        }

        if (!path.isEmpty()) {
            path.pop();
        }

        return new ArrayList<>(path);
    }
}


