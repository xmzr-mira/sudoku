import java.util.*;
import javax.swing.JFrame;

public class SudokuGenerator {
    private static final int SIZE = 9;
    private static final int MAX_ATTEMPTS = 10; // 最大重试次数
    private int[][] puzzle;           // 题目
    private int[][] fullSolution;      // 完整答案
    private JFrame parent;             // 父窗口，用于难度选择

    public SudokuGenerator(JFrame parent) {
        this.puzzle = new int[SIZE][SIZE];
        this.parent = parent;
    }

    // 生成数独谜题并确保只有唯一解
    public int[][] generateSudokuPuzzle() {
        // 获取难度（以确定隐藏数字个数）
        int diff = SudokuDifficultySelection.showDialog(parent);
        if (diff == 0) diff = 27; // 默认 Easy，防止在玩家未选择难度的情况下直接关闭难度选择窗口导致卡死

        int attempts = 0;
        boolean success = false;
        while (attempts < MAX_ATTEMPTS && !success) {
            // 每次尝试前重置puzzle
            puzzle = new int[SIZE][SIZE];

            // 生成完整终盘
            fillDiagonal();
            if (!fillRemaining(0, 3)) {
                attempts++;
                continue;
            }
            fullSolution = deepCopy(puzzle); // 保存完整答案

            // 尝试隐藏指定数量的数字
            int hidden = hideNumbers(diff);
            if (hidden >= diff) {
                success = true;
            } else {
                attempts++;
            }
        }

        if (!success) {
            System.err.println("Warning: Could not generate puzzle with desired difficulty after " + MAX_ATTEMPTS + " attempts. Using last generated puzzle.");
        }
        return puzzle;
    }

    // 返回完整解
    public int[][] getFullSolution() {
        return fullSolution;
    }

    // 生成完整终盘
    private void fillDiagonal() {
        Random random = new Random();
        for (int i = 0; i < SIZE; i += 3) {
            fillSubGrid(i, i, random);
        }
    }

    private void fillSubGrid(int row, int col, Random random) {
        int[] nums = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffleArray(nums, random);
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                puzzle[row + i][col + j] = nums[index++];
            }
        }
    }

    private void shuffleArray(int[] array, Random random) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private boolean isValid(int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (puzzle[row][i] == num || puzzle[i][col] == num) {
                return false;
            }
        }
        int subGridStartRow = row - row % 3;
        int subGridStartCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (puzzle[subGridStartRow + i][subGridStartCol + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean fillRemaining(int row, int col) {
        if (col == SIZE) {
            col = 0;
            row++;
            if (row == SIZE) {
                return true;
            }
        }
        if (puzzle[row][col] != 0) {
            return fillRemaining(row, col + 1);
        }
        Random random = new Random();
        int[] nums = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffleArray(nums, random);
        for (int i = 0; i < SIZE; i++) {
            if (isValid(row, col, nums[i])) {
                puzzle[row][col] = nums[i];
                if (fillRemaining(row, col + 1)) {
                    return true;
                }
                puzzle[row][col] = 0;
            }
        }
        return false;
    }

    // 唯一解隐藏数字逻辑
    private int hideNumbers(int numToHide) {
        // 随机打乱所有位置
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions, new Random());

        int hidden = 0;
        while (hidden < numToHide && !positions.isEmpty()) {
            int[] pos = positions.remove(positions.size() - 1);
            int row = pos[0];
            int col = pos[1];
            int original = puzzle[row][col];
            puzzle[row][col] = 0; // 尝试隐藏

            if (isUnique(puzzle)) {
                hidden++; // 隐藏成功
            } else {
                puzzle[row][col] = original; // 恢复
            }
        }
        return hidden;
    }

    // 判断当前数独是否有唯一解
    private boolean isUnique(int[][] board) {
        return countSolutions(board, 2) == 1;
    }

    // 计数解，最多数到 limit
    private int countSolutions(int[][] board, int limit) {
        int[][] copy = deepCopy(board);
        return countSolutionsRecursive(copy, limit);
    }

    private int countSolutionsRecursive(int[][] board, int limit) {
        // 寻找第一个空格
        int row = -1, col = -1;
        boolean found = false;
        for (int i = 0; i < SIZE && !found; i++) {
            for (int j = 0; j < SIZE && !found; j++) {
                if (board[i][j] == 0) {
                    row = i;
                    col = j;
                    found = true;
                }
            }
        }
        if (!found) {
            return 1; // 找到一个完整解
        }

        int count = 0;
        for (int num = 1; num <= 9; num++) {
            if (isValidMove(board, row, col, num)) {
                board[row][col] = num;
                count += countSolutionsRecursive(board, limit);
                if (count >= limit) {
                    return count; // 达到限制，提前返回
                }
                board[row][col] = 0;
            }
        }
        return count;
    }

    private boolean isValidMove(int[][] board, int row, int col, int num) {
        // 检查行
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num) return false;
        }
        // 检查列
        for (int i = 0; i < SIZE; i++) {
            if (board[i][col] == num) return false;
        }
        // 检查宫
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == num) return false;
            }
        }
        return true;
    }

    // 工具方法
    private int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}