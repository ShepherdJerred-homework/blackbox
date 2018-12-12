// blackbox
// Jerred SHepherd

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class blackbox {
    private static final int BOARD_ROWS = 8;
    private static final int BOARD_COLUMNS = 8;
    private static final int BOARD_PADDING = 1;
    private static final int BOARD_ROWS_WITH_PADDING = BOARD_ROWS + BOARD_PADDING * 2;
    private static final int BOARD_COLUMNS_WITH_PADDING = BOARD_COLUMNS + BOARD_PADDING * 2;

    public static void main(String[] args) throws FileNotFoundException {
        List<Input> inputs = getInputs();
        List<Output> outputs = solveInputs(inputs);
        printOutputs(outputs);
    }

    private static void printOutputs(List<Output> outputs) throws FileNotFoundException {
        File file = new File("blackbox.out");
        PrintWriter printWriter = new PrintWriter(file);
        outputs.forEach(output -> {
            printWriter.print(output.toFormattedOutput());
            System.out.print(output.toFormattedOutput());
        });
        printWriter.close();
    }

    private static Output solveInput(Input input) {
//        System.out.println(input);
//        System.out.println(input.board.toOutputString());

        List<Board.ShootResult> results = new ArrayList<>();

        input.tests.forEach(test -> {
            System.out.println("NEXT TEST CASE: " + test + "\n");
            Board board = new Board(input.board);
            Board.ShootResult result = board.shootRay(test.rayStart, test.rayDirection, test.rayStart, 0);
            System.out.println("RESULT: " + result + "\n\n");
            results.add(result);
        });

        return new Output(results);
    }

    private static List<Output> solveInputs(List<Input> inputs) {
        List<Output> outputs = new ArrayList<>();
        inputs.forEach(input -> {
            Output output = solveInput(input);
            outputs.add(output);
        });
        return outputs;
    }

    private static List<Input> getInputs() throws FileNotFoundException {
        List<Input> inputs = new ArrayList<>();

        File inputFile = new File("blackbox.in");
        Scanner scanner = new Scanner(inputFile);

        while (scanner.hasNext()) {
            Board board = new Board();

            String s = null;
            for (int row = 0; row < BOARD_ROWS; row++) {
                s = scanner.next();

                if (s.equals("X")) {
                    break;
                }

                for (int col = 0; col < BOARD_COLUMNS; col++) {
                    Coordinate coordinate = new Coordinate(row + 1, col + 1);
                    Board.Cell cell = Board.Cell.fromChar(s.charAt(col));
                    board.setCell(coordinate, cell);
                }
            }

            if (s.equals("X")) {
                break;
            }

            int numberOfTests = scanner.nextInt();
            List<Test> tests = new ArrayList<>(numberOfTests);
            for (int i = 0; i < numberOfTests; i++) {
                String ray = scanner.next();
                Coordinate coordinate = Coordinate.fromInputString(ray);
                Board.Direction direction = Board.Direction.fromChar(ray.charAt(0));
                Test test = new Test(coordinate, direction);
                tests.add(test);
            }

            Input input = new Input(board, tests);
            inputs.add(input);
        }

        return inputs;
    }

    static class Coordinate {
        final int row;
        final int column;

        Coordinate(int row, int column) {
            this.row = row;
            this.column = column;
        }

        Coordinate up() {
            return new Coordinate(row - 1, column);
        }

        Coordinate down() {
            return new Coordinate(row + 1, column);
        }

        Coordinate left() {
            return new Coordinate(row, column - 1);
        }

        Coordinate right() {
            return new Coordinate(row, column + 1);
        }

        static Coordinate fromInputString(String s) {
            char direction = s.charAt(0);
            int num = Character.getNumericValue(s.charAt(1));

            Coordinate coordinate;
            switch (direction) {
                case 'T':
                    coordinate = new Coordinate(0, num);
                    break;
                case 'B':
                    coordinate = new Coordinate(BOARD_ROWS_WITH_PADDING - 1, num);
                    break;
                case 'L':
                    coordinate = new Coordinate(num, 0);
                    break;
                case 'R':
                    coordinate = new Coordinate(num, BOARD_COLUMNS_WITH_PADDING - 1);
                    break;
                default:
                    throw new IllegalArgumentException(s + " is not a valid Coordinate string");
            }

            return coordinate;
        }

        public String toOutputString() {
            // TODO this might not work
            if (row == 0) {
                return String.format("T%s", column);
            } else if (row == BOARD_ROWS_WITH_PADDING - 1) {
                return String.format("B%s", column);
            } else if (column == 0) {
                return String.format("L%s", row);
            } else if (column == BOARD_COLUMNS_WITH_PADDING - 1) {
                return String.format("R%s", row);
            } else {
                throw new IllegalStateException("Cannot output a coordinate that isn't at the edge of the grid");
            }
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "row=" + row +
                    ", column=" + column +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return row == that.row &&
                    column == that.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }

    static class Board {
        private final Cell[][] cells;

        Board() {
            this.cells = new Cell[BOARD_ROWS_WITH_PADDING][BOARD_COLUMNS_WITH_PADDING];
            for (int row = 0; row < BOARD_ROWS_WITH_PADDING; row++) {
                for (int col = 0; col < BOARD_COLUMNS_WITH_PADDING; col++) {
                    Coordinate coordinate = new Coordinate(row, col);
                    setCell(coordinate, Cell.PADDING);
                }
            }
        }

        Board(Board board) {
            this.cells = new Cell[BOARD_ROWS_WITH_PADDING][BOARD_COLUMNS_WITH_PADDING];
            for (int row = 0; row < BOARD_ROWS_WITH_PADDING; row++) {
                for (int col = 0; col < BOARD_COLUMNS_WITH_PADDING; col++) {
                    Coordinate coordinate = new Coordinate(row, col);
                    setCell(coordinate, board.getCell(coordinate));
                }
            }
        }

        Coordinate getNextCoordinate(Coordinate coordinate, Direction direction) {
            Coordinate nextCoordinate;
            switch (direction) {
                case UP:
                    nextCoordinate = coordinate.up();
                    break;
                case DOWN:
                    nextCoordinate = coordinate.down();
                    break;
                case LEFT:
                    nextCoordinate = coordinate.left();
                    break;
                case RIGHT:
                    nextCoordinate = coordinate.right();
                    break;
                default:
                    throw new IllegalStateException("Unknown direction");
            }
            return nextCoordinate;
        }

        Cell getNextCell(Coordinate coordinate, Direction direction) {
            return getCell(getNextCoordinate(coordinate, direction));
        }

        boolean isReflection(Coordinate coordinate, Direction direction) {
            Coordinate nextCoordinate = getNextCoordinate(coordinate, direction);
            Coordinate up;
            Coordinate down;
            Coordinate left;
            Coordinate right;

            switch (direction) {
                case UP:
                case DOWN:
                    left = nextCoordinate.left();
                    right = nextCoordinate.right();
                    if (!isValidCoordinate(left) || !isValidCoordinate(right)) return false;
                    Cell leftCell = getCell(left);
                    Cell rightCell = getCell(right);
                    return leftCell == Cell.ATOM && rightCell == Cell.ATOM;
                case LEFT:
                case RIGHT:
                    up = nextCoordinate.up();
                    down = nextCoordinate.down();
                    if (!isValidCoordinate(up) || !isValidCoordinate(down)) return false;
                    Cell upCell = getCell(up);
                    Cell downCell = getCell(down);
                    return upCell == Cell.ATOM && downCell == Cell.ATOM;
                default:
                    throw new IllegalStateException("Unknown direction");
            }
        }

        Direction isDeflection(Coordinate coordinate, Direction direction) {
            Coordinate nextCoordinate = getNextCoordinate(coordinate, direction);
            Coordinate up;
            Coordinate down;
            Coordinate left;
            Coordinate right;

            switch (direction) {
                case UP:
                case DOWN:
                    left = nextCoordinate.left();
                    right = nextCoordinate.right();
                    if (!isValidCoordinate(left) || !isValidCoordinate(right)) return null;
                    Cell leftCell = getCell(left);
                    Cell rightCell = getCell(right);
                    if (leftCell == Cell.ATOM) {
                        return Direction.RIGHT;
                    } else if (rightCell == Cell.ATOM) {
                        return Direction.LEFT;
                    } else {
                        return null;
                    }
                case LEFT:
                case RIGHT:
                    up = nextCoordinate.up();
                    down = nextCoordinate.down();
                    if (!isValidCoordinate(up) || !isValidCoordinate(down)) return null;
                    Cell upCell = getCell(up);
                    Cell downCell = getCell(down);
                    if (upCell == Cell.ATOM) {
                        return Direction.DOWN;
                    } else if (downCell == Cell.ATOM) {
                        return Direction.UP;
                    } else {
                        return null;
                    }
                default:
                    throw new IllegalStateException("Unknown direction");
            }
        }

        boolean isHit(Coordinate coordinate, Direction direction) {
            Coordinate nextCoordinate = getNextCoordinate(coordinate, direction);
            return getCell(nextCoordinate) == Cell.ATOM;
        }

        ShootResult.ResultType checkRay(Coordinate coordinate, Direction direction, Coordinate initial, int iteration) {
            if (isInGrid(coordinate)) {
                if (isHit(coordinate, direction)) {
                    return ShootResult.ResultType.HIT;
                } else {
                    return null;
                }
            } else {
                System.out.println("Not in grid");
                if (iteration == 0) {
                    if (getNextCell(coordinate, direction) == Cell.ATOM) {
                        return ShootResult.ResultType.HIT ;
                    }
                    if (isDeflection(coordinate, direction) != null) {
                        return ShootResult.ResultType.REFLECTION;
                    }
                    return null;
                } else {
                    if (coordinate.equals(initial)) {
                        return ShootResult.ResultType.REFLECTION;
                    } else {
                        return ShootResult.ResultType.EXIT;
                    }
                }
            }
        }

        ShootResult shootRay(Coordinate coordinate, Direction direction, Coordinate initial, int iteration) {
            ShootResult.ResultType result = checkRay(coordinate, direction, initial, iteration);

            if (getCell(coordinate) != Cell.EMPTY && getCell(coordinate) != Cell.PADDING) {
                throw new IllegalStateException("Trying to shoot into a non-empty cell " + coordinate + getCell(coordinate));
            } else {
                setCell(coordinate, Cell.RAY);
                System.out.println("ITERATION " + iteration);
                System.out.println(coordinate + " " + direction);
                System.out.println(this.toPrettyString());
            }

            if (result == null) {
                // Move
                Coordinate nextCoordinate;
                Direction nextDirection;

                if (isReflection(coordinate, direction)) {
                    System.out.println("Reflection");
                    nextCoordinate = coordinate;
                    nextDirection = Direction.opposite(direction);
                } else {
                    Direction deflection = isDeflection(coordinate, direction);
                    if (deflection != null) {
                        System.out.println("Deflection " + deflection);
                        nextCoordinate = coordinate;
                        nextDirection = deflection;
                    } else {
                        nextCoordinate = getNextCoordinate(coordinate, direction);
                        nextDirection = direction;
                    }
                }

                setCell(coordinate, Cell.EMPTY);
                return shootRay(nextCoordinate, nextDirection, initial, iteration += 1);
            } else {
                if (result == ShootResult.ResultType.EXIT) {
                    return new ShootResult(coordinate);
                } else {
                    return new ShootResult(result);
                }
            }
        }

        private boolean isInGrid(Coordinate coordinate) {
            return isValidCoordinate(coordinate) &&
                    coordinate.row > 0 &&
                    coordinate.row <= BOARD_ROWS_WITH_PADDING - BOARD_PADDING * 2 &&
                    coordinate.column > 0 &&
                    coordinate.column <= BOARD_COLUMNS_WITH_PADDING - BOARD_PADDING * 2;
        }

        private boolean isValidCoordinate(Coordinate coordinate) {
            return coordinate.row >= 0 &&
                    coordinate.row <= BOARD_ROWS_WITH_PADDING &&
                    coordinate.column >= 0 &&
                    coordinate.column <= BOARD_COLUMNS_WITH_PADDING;
        }

        private void setCell(Coordinate coordinate, Cell cell) {
            if (isValidCoordinate(coordinate)) {
                cells[coordinate.row][coordinate.column] = cell;
            } else {
                throw new IllegalArgumentException(coordinate.toString());
            }
        }

        private Cell getCell(Coordinate coordinate) {
            if (isValidCoordinate(coordinate)) {
                return cells[coordinate.row][coordinate.column];
            } else {
                throw new IllegalArgumentException(coordinate.toString());
            }
        }

        String toPrettyString() {
            StringBuilder sb = new StringBuilder();
            sb.append("   01234567 \n");
            for (int row = 0; row < BOARD_ROWS_WITH_PADDING; row++) {
                if (row != BOARD_ROWS_WITH_PADDING - 1 && row != 0) sb.append(row - 1).append(" ");
                else sb.append("  ");
                for (int col = 0; col < BOARD_COLUMNS_WITH_PADDING; col++) {
                    Coordinate coordinate = new Coordinate(row, col);
                    sb.append(getCell(coordinate).toPrettyString());
                }
                sb.append('\n');
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "Board{" +
                    "cells=" + Arrays.deepToString(cells) +
                    '}';
        }

        static class ShootResult {
            final ResultType resultType;
            final Coordinate exitPoint;

            enum ResultType {
                HIT, REFLECTION, EXIT;
            }

            public ShootResult(ResultType resultType) {
                this.resultType = resultType;
                this.exitPoint = null;

                if (this.resultType == ResultType.EXIT) {
                    throw new IllegalArgumentException("Cannot have an exit result without an exit point");
                }
            }

            public ShootResult(Coordinate exitPoint) {
                this.resultType = ResultType.EXIT;
                this.exitPoint = exitPoint;
            }

            public String toOutputString() {
                switch (resultType) {
                    case HIT:
                        return "H";
                    case REFLECTION:
                        return "R";
                    case EXIT:
                        return exitPoint.toOutputString();
                    default:
                        throw new IllegalStateException();
                }
            }

            @Override
            public String toString() {
                return "ShootResult{" +
                        "resultType=" + resultType +
                        ", exitPoint=" + exitPoint +
                        '}';
            }
        }

        enum Direction {
            UP, DOWN, LEFT, RIGHT;

            static Direction fromChar(char c) {
                switch (c) {
                    case 'T':
                        return DOWN;
                    case 'B':
                        return UP;
                    case 'L':
                        return RIGHT;
                    case 'R':
                        return LEFT;
                    default:
                        throw new IllegalArgumentException(c + " is not a valid Direction char");
                }
            }

            static Direction opposite(Direction direction) {
                switch (direction) {
                    case UP:
                        return DOWN;
                    case DOWN:
                        return UP;
                    case LEFT:
                        return RIGHT;
                    case RIGHT:
                        return LEFT;
                    default:
                        throw new IllegalArgumentException("Unknown direction");
                }
            }
        }

        enum Cell {
            EMPTY, ATOM, RAY, PADDING;

            static Cell fromChar(char c) {
                switch (c) {
                    case '.':
                        return EMPTY;
                    case '@':
                        return ATOM;
                    default:
                        throw new IllegalArgumentException(c + " is not a valid cell character");
                }
            }

            String toPrettyString() {
                switch (this) {
                    case EMPTY:
                        return " ";
                    case ATOM:
                        return "@";
                    case RAY:
                        return "-";
                    case PADDING:
                        return "█";
                    default:
                        return "?";
                }
            }
        }
    }

    static class Output {
        final List<Board.ShootResult> results;

        Output(List<Board.ShootResult> results) {
            this.results = results;
        }

        String toFormattedOutput() {
            StringBuilder sb = new StringBuilder();
            results.forEach(result -> sb.append(result.toOutputString()).append('\n'));
            return sb.toString();
        }

        @Override
        public String toString() {
            return "Output{" +
                    "results=" + results +
                    '}';
        }
    }

    static class Test {
        final Coordinate rayStart;
        final Board.Direction rayDirection;

        Test(Coordinate rayStart, Board.Direction rayDirection) {
            this.rayStart = rayStart;
            this.rayDirection = rayDirection;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "rayStart=" + rayStart +
                    ", rayDirection=" + rayDirection +
                    '}';
        }
    }

    static class Input {
        final Board board;
        final List<Test> tests;

        Input(Board board, List<Test> tests) {
            this.board = board;
            this.tests = tests;
        }

        @Override
        public String toString() {
            return "Input{" +
                    "board=" + board +
                    ", tests=" + tests +
                    '}';
        }
    }
}
