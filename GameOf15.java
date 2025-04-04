import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Move {
    int row, col, digit, score;
    Move(int row, int col, int digit, int score) {
        this.row = row;
        this.col = col;
        this.digit = digit;
        this.score = score;
    }
}

/**
 * GameOf15ZeroForBoth.java
 *
 * A Game of 15 variant where:
 * - Empty cells are stored as -1 (displayed as "0").
 * - If human picks ODD, allowed digits are {1,3,5,7,9} plus 0 (if unused).
 * - If human picks EVEN, allowed digits are {2,4,6,8} plus 0 (if unused).
 * - The AI automatically gets the opposite set (also +0 if unused).
 * - Each digit can only be used once total, including 0.
 *
 * Full-depth minimax with alpha-beta pruning is used for the AI moves.
 */
public class GameOf15ZeroForBoth {

    private int[][] board = new int[3][3];     // 3x3 board; -1 = empty
    private boolean[] used = new boolean[10];  // tracks digits 0..9 (false = not used)
    private Scanner sc = new Scanner(System.in);
    private Random rand = new Random();

    private int moveCount = 0;  // how many moves have been made (0..9)
    private int counter = 0;    // minimax computation count

    // Selections
    private boolean humanIsOdd;   // if true => user can pick odd digits + 0
    private boolean humanFirst;   // if true => user goes first

    public static void main(String[] args) {
        GameOf15ZeroForBoth game = new GameOf15ZeroForBoth();
        game.startGame();
    }

    public void startGame() {
        System.out.println("\n--- Game of 15 with 0 for Both Sides ---");
        
        // Order selection
        System.out.print("Who goes first? (Enter 'human' or 'ai'): ");
        String order = sc.nextLine().trim().toLowerCase();
        humanFirst = order.equals("human");

        // Parity selection
        System.out.print("Choose your digit set (Enter 'odd' or 'even'): ");
        String parity = sc.nextLine().trim().toLowerCase();
        if (parity.equals("odd")) {
            humanIsOdd = true;
        } else {
            humanIsOdd = false;
        }

        // Show info
        if (humanIsOdd) {
            System.out.println("You are ODD => {1,3,5,7,9} + 0 (if not used).");
        } else {
            System.out.println("You are EVEN => {2,4,6,8} + 0 (if not used).");
        }
        System.out.println(humanFirst ? "You go first." : "AI goes first.");
        System.out.println("-------------------------------------------");

        while (true) {
            resetBoard();
            playMatch();
            System.out.print("Enter 1 to play again, 0 to exit: ");
            int choice = sc.nextInt();
            sc.nextLine();
            if (choice != 1) {
                System.out.println("Thanks for playing!");
                break;
            }
        }
        sc.close();
    }

    // Clear the board, set all to -1, reset used digits and move count.
    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = -1;  // empty
            }
        }
        for (int d = 0; d <= 9; d++) {
            used[d] = false;
        }
        moveCount = 0;
    }

    private void playMatch() {
        // If AI goes first, do so now
        if (!humanFirst) {
            aiMove();
            moveCount++;
            printBoard();
        }
        // Main loop
        while (true) {
            if (evaluate(board)) {
                if (didHumanWin()) {
                    System.out.println("Congratulations, you win!");
                } else {
                    System.out.println("AI wins! Better luck next time.");
                }
                break;
            }
            if (moveCount == 9) {
                System.out.println("It's a tie!");
                break;
            }
            if (isHumanTurn()) {
                humanMove();
            } else {
                aiMove();
            }
            moveCount++;
            printBoard();
        }
    }

    // Decide if it's the human's turn based on moveCount and order selection.
    private boolean isHumanTurn() {
        int turn = moveCount + 1;  // turn numbers start at 1
        if (humanFirst) {
            return (turn % 2 != 0);
        } else {
            return (turn % 2 == 0);
        }
    }

    // Human move: row col digit
    private void humanMove() {
        System.out.println("Your move. (Allowed digits: " + 
            (humanIsOdd ? "{1,3,5,7,9}+0" : "{2,4,6,8}+0") + " if unused)");
        while (true) {
            System.out.print("Enter row(0-2), col(0-2), digit: ");
            String line = sc.nextLine().trim();
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Please enter exactly three integers.");
                continue;
            }
            int r, c, d;
            try {
                r = Integer.parseInt(parts[0]);
                c = Integer.parseInt(parts[1]);
                d = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Try again.");
                continue;
            }
            if (!isValidMove(r, c, d, humanIsOdd)) {
                continue;
            }
            board[r][c] = d;
            used[d] = true;
            break;
        }
    }

    // AI move: uses full-depth minimax with alpha-beta pruning
    private void aiMove() {
        boolean aiIsOdd = !humanIsOdd;
        System.out.println("AI's move (" + (aiIsOdd ? "ODD+0" : "EVEN+0") + "):");
        counter = 0;
        long startTime = System.currentTimeMillis();
        int depth = moveCount;
        Move best;
        if (aiIsOdd) {
            best = getBestOddMove(depth);
        } else {
            best = getBestEvenMove(depth);
        }
        long endTime = System.currentTimeMillis();
        board[best.row][best.col] = best.digit;
        used[best.digit] = true;
        System.out.println("AI placed " + best.digit + " at (" + best.row + "," + best.col + ")");
        System.out.println("Minimax computations: " + counter + ", Time: " + (endTime - startTime) + " ms");
    }

    // Check if the move is valid for the current parity (isOdd).
    // isOdd => {1,3,5,7,9} plus 0 if not used
    // !isOdd => {2,4,6,8} plus 0 if not used
    private boolean isValidMove(int r, int c, int d, boolean isOdd) {
        if (r < 0 || r > 2 || c < 0 || c > 2) {
            System.out.println("Row/Col must be 0..2.");
            return false;
        }
        if (board[r][c] != -1) {
            System.out.println("That cell is already occupied!");
            return false;
        }
        if (d < 0 || d > 9) {
            System.out.println("Digit must be between 0 and 9.");
            return false;
        }
        // Check if digit is in the correct set
        if (isOdd) {
            // odd digits => 1,3,5,7,9 plus 0 if not used
            if (d != 0 && (d < 1 || d > 9 || d % 2 == 0)) {
                System.out.println("You are ODD. Allowed digits: {1,3,5,7,9} plus 0 (if unused).");
                return false;
            }
        } else {
            // even digits => 2,4,6,8 plus 0 if not used
            if (d != 0 && (d < 2 || d > 8 || d % 2 != 0)) {
                System.out.println("You are EVEN. Allowed digits: {2,4,6,8} plus 0 (if unused).");
                return false;
            }
        }
        // Check usage
        if (used[d]) {
            System.out.println("That digit is already used!");
            return false;
        }
        return true;
    }

    // Evaluate if there's a line summing to 15 with no empty cell
    private boolean evaluate(int[][] state) {
        // rows
        for (int i = 0; i < 3; i++) {
            if (state[i][0] != -1 && state[i][1] != -1 && state[i][2] != -1) {
                int sum = state[i][0] + state[i][1] + state[i][2];
                if (sum == 15) return true;
            }
        }
        // columns
        for (int j = 0; j < 3; j++) {
            if (state[0][j] != -1 && state[1][j] != -1 && state[2][j] != -1) {
                int sum = state[0][j] + state[1][j] + state[2][j];
                if (sum == 15) return true;
            }
        }
        // diagonals
        if (state[0][0] != -1 && state[1][1] != -1 && state[2][2] != -1) {
            int d1 = state[0][0] + state[1][1] + state[2][2];
            if (d1 == 15) return true;
        }
        if (state[0][2] != -1 && state[1][1] != -1 && state[2][0] != -1) {
            int d2 = state[0][2] + state[1][1] + state[2][0];
            if (d2 == 15) return true;
        }
        return false;
    }

    // If the last winning move was made by the human, they have more digits than AI
    private boolean didHumanWin() {
        int oddCount = 0, evenCount = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int v = board[i][j];
                if (v != -1) {
                    if (v % 2 != 0) oddCount++;
                    else evenCount++;
                }
            }
        }
        return humanIsOdd ? (oddCount > evenCount) : (evenCount > oddCount);
    }

    // Print the board to console, showing 0 if cell is -1
    private void printBoard() {
        System.out.println("Current board:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int v = board[i][j];
                System.out.print((v == -1 ? 0 : v) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // --------------------------
    // Minimax Logic
    // --------------------------

    // Get best Odd move (AI is Odd)
    private Move getBestOddMove(int depth) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == -1) {
                    // Odd set = {1,3,5,7,9} + 0 if available
                    for (int d = 1; d < 10; d += 2) {
                        if (!used[d]) {
                            board[r][c] = d;
                            used[d] = true;
                            int score = minimax(board, depth + 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                            board[r][c] = -1;
                            used[d] = false;
                            counter++;
                            moves.add(new Move(r, c, d, score));
                        }
                    }
                    // Also consider 0 if it's unused
                    if (!used[0]) {
                        board[r][c] = 0;
                        used[0] = true;
                        int score = minimax(board, depth + 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                        board[r][c] = -1;
                        used[0] = false;
                        counter++;
                        moves.add(new Move(r, c, 0, score));
                    }
                }
            }
        }
        int maxScore = Integer.MIN_VALUE;
        for (Move m : moves) {
            maxScore = Math.max(maxScore, m.score);
        }
        List<Move> bestMoves = new ArrayList<>();
        for (Move m : moves) {
            if (m.score == maxScore) bestMoves.add(m);
        }
        return bestMoves.get(rand.nextInt(bestMoves.size()));
    }

    // Get best Even move (AI is Even)
    private Move getBestEvenMove(int depth) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == -1) {
                    // Even set = {2,4,6,8} + 0 if available
                    for (int d = 2; d < 10; d += 2) {
                        if (!used[d]) {
                            board[r][c] = d;
                            used[d] = true;
                            int score = minimax(board, depth + 1, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
                            board[r][c] = -1;
                            used[d] = false;
                            counter++;
                            moves.add(new Move(r, c, d, score));
                        }
                    }
                    if (!used[0]) {
                        board[r][c] = 0;
                        used[0] = true;
                        int score = minimax(board, depth + 1, true, Integer.MIN_VALUE, Integer.MAX_VALUE);
                        board[r][c] = -1;
                        used[0] = false;
                        counter++;
                        moves.add(new Move(r, c, 0, score));
                    }
                }
            }
        }
        int minScore = Integer.MAX_VALUE;
        for (Move m : moves) {
            minScore = Math.min(minScore, m.score);
        }
        List<Move> bestMoves = new ArrayList<>();
        for (Move m : moves) {
            if (m.score == minScore) bestMoves.add(m);
        }
        return bestMoves.get(rand.nextInt(bestMoves.size()));
    }

    /**
     * Full-depth minimax with alpha-beta pruning.
     * isMax = true => maximizing side's turn; false => minimizing side.
     *
     * Terminal evaluation: if there's a line = 15, we see if odd or even placed the last digit
     * by counting # of odd vs even digits on the board.
     */
    private int minimax(int[][] state, int depth, boolean isMax, int alpha, int beta) {
        // Check terminal
        if (evaluate(state)) {
            int oddCount = 0, evenCount = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int v = state[i][j];
                    if (v != -1) {
                        if (v % 2 != 0) oddCount++;
                        else evenCount++;
                    }
                }
            }
            // If oddCount > evenCount => odd side just won
            if (oddCount > evenCount) {
                return humanIsOdd ? (100 - depth) : (-100 + depth);
            } else {
                return humanIsOdd ? (-100 + depth) : (100 - depth);
            }
        }
        if (depth == 9) {
            return 0;  // tie
        }

        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            outerLoop:
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (state[r][c] == -1) {
                        if (humanIsOdd) {
                            // If human is odd => AI is even => maximizing moves = even digits + 0
                            for (int d = 2; d < 10; d += 2) {
                                if (!used[d]) {
                                    state[r][c] = d;
                                    used[d] = true;
                                    int eval = minimax(state, depth + 1, false, alpha, beta);
                                    state[r][c] = -1;
                                    used[d] = false;
                                    maxEval = Math.max(maxEval, eval);
                                    alpha = Math.max(alpha, eval);
                                    if (beta <= alpha) break outerLoop;
                                }
                            }
                            // also consider 0 if not used
                            if (!used[0]) {
                                state[r][c] = 0;
                                used[0] = true;
                                int eval = minimax(state, depth + 1, false, alpha, beta);
                                state[r][c] = -1;
                                used[0] = false;
                                maxEval = Math.max(maxEval, eval);
                                alpha = Math.max(alpha, eval);
                                if (beta <= alpha) break outerLoop;
                            }
                        } else {
                            // If human is even => AI is odd => maximizing moves = odd digits + 0
                            for (int d = 1; d < 10; d += 2) {
                                if (!used[d]) {
                                    state[r][c] = d;
                                    used[d] = true;
                                    int eval = minimax(state, depth + 1, false, alpha, beta);
                                    state[r][c] = -1;
                                    used[d] = false;
                                    maxEval = Math.max(maxEval, eval);
                                    alpha = Math.max(alpha, eval);
                                    if (beta <= alpha) break outerLoop;
                                }
                            }
                            // consider 0
                            if (!used[0]) {
                                state[r][c] = 0;
                                used[0] = true;
                                int eval = minimax(state, depth + 1, false, alpha, beta);
                                state[r][c] = -1;
                                used[0] = false;
                                maxEval = Math.max(maxEval, eval);
                                alpha = Math.max(alpha, eval);
                                if (beta <= alpha) break outerLoop;
                            }
                        }
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            outerLoop:
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (state[r][c] == -1) {
                        if (humanIsOdd) {
                            // If human is odd => AI is even => minimizing moves = odd digits + 0
                            for (int d = 1; d < 10; d += 2) {
                                if (!used[d]) {
                                    state[r][c] = d;
                                    used[d] = true;
                                    int eval = minimax(state, depth + 1, true, alpha, beta);
                                    state[r][c] = -1;
                                    used[d] = false;
                                    minEval = Math.min(minEval, eval);
                                    beta = Math.min(beta, eval);
                                    if (beta <= alpha) break outerLoop;
                                }
                            }
                            // consider 0
                            if (!used[0]) {
                                state[r][c] = 0;
                                used[0] = true;
                                int eval = minimax(state, depth + 1, true, alpha, beta);
                                state[r][c] = -1;
                                used[0] = false;
                                minEval = Math.min(minEval, eval);
                                beta = Math.min(beta, eval);
                                if (beta <= alpha) break outerLoop;
                            }
                        } else {
                            // If human is even => AI is odd => minimizing moves = even digits + 0
                            for (int d = 2; d < 10; d += 2) {
                                if (!used[d]) {
                                    state[r][c] = d;
                                    used[d] = true;
                                    int eval = minimax(state, depth + 1, true, alpha, beta);
                                    state[r][c] = -1;
                                    used[d] = false;
                                    minEval = Math.min(minEval, eval);
                                    beta = Math.min(beta, eval);
                                    if (beta <= alpha) break outerLoop;
                                }
                            }
                            // consider 0
                            if (!used[0]) {
                                state[r][c] = 0;
                                used[0] = true;
                                int eval = minimax(state, depth + 1, true, alpha, beta);
                                state[r][c] = -1;
                                used[0] = false;
                                minEval = Math.min(minEval, eval);
                                beta = Math.min(beta, eval);
                                if (beta <= alpha) break outerLoop;
                            }
                        }
                    }
                }
            }
            return minEval;
        }
    }
}
