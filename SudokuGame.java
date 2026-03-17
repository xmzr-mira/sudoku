import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class SudokuGame extends JFrame {
    private JTextField[][] cells;
    private int[][] solution;   // 完整答案
    private int[][] puzzle;      // 题目
    private boolean gameCompleted = false;

    // 边框样式：可编辑用深绿色，不可编辑用深灰色
    private final Border EDITABLE_BORDER = BorderFactory.createLineBorder(new Color(0, 128, 0), 2); 
    private final Border NON_EDITABLE_BORDER = BorderFactory.createLineBorder(Color.DARK_GRAY, 1); 

    public SudokuGame() {
        startNewGame();
    }

    private JPanel getjPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton solveButton = new JButton("显示答案");
        solveButton.addActionListener(e -> setSolution());
        JButton hintButton = new JButton("提示");
        hintButton.addActionListener(e -> showHint());

        buttonPanel.add(solveButton);
        buttonPanel.add(hintButton);
        return buttonPanel;
    }

    private void showHint() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].getText().equals("")) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        if (emptyCells.isEmpty()) {
            JOptionPane.showMessageDialog(this, "已经没有空格需要提示了！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int[] pos = emptyCells.get((int) (Math.random() * emptyCells.size()));
        int i = pos[0], j = pos[1];
        cells[i][j].setText(Integer.toString(solution[i][j]));
        cells[i][j].setEditable(false);
        // 提示后变为不可编辑
        applyNonEditableStyle(i, j); 
        checkCompletion();
    }

    private void startNewGame() {
        setTitle("Sudoku Game");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sudokuPanel = new JPanel(new GridLayout(9, 9));
        cells = new JTextField[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField(1);
                cells[i][j].setFont(new Font("serif", Font.BOLD, 40));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                int ii = i;
                int jj = j;
                cells[i][j].addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (!Character.isDigit(c) || c == '0') {
                            e.consume();
                        }
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {}

                    @Override
                    public void keyReleased(KeyEvent e) {
                        String text = cells[ii][jj].getText();
                        if (text.isEmpty()) {
                            // 清空后恢复宫格背景，并应用可编辑样式
                            printCells(ii, jj);
                            applyEditableStyle(ii, jj);
                        } else {
                            int num = Integer.parseInt(text);
                            boolean conflict = false;

                            // 检查行
                            for (int k = 0; k < 9; k++) {
                                if (k == jj) continue;
                                String other = cells[ii][k].getText();
                                if (!other.isEmpty() && Integer.parseInt(other) == num) {
                                    conflict = true;
                                    break;
                                }
                            }
                            // 检查列
                            if (!conflict) {
                                for (int k = 0; k < 9; k++) {
                                    if (k == ii) continue;
                                    String other = cells[k][jj].getText();
                                    if (!other.isEmpty() && Integer.parseInt(other) == num) {
                                        conflict = true;
                                        break;
                                    }
                                }
                            }
                            // 检查宫
                            if (!conflict) {
                                int startRow = ii - ii % 3;
                                int startCol = jj - jj % 3;
                                for (int r = startRow; r < startRow + 3; r++) {
                                    for (int c = startCol; c < startCol + 3; c++) {
                                        if (r == ii && c == jj) continue;
                                        String other = cells[r][c].getText();
                                        if (!other.isEmpty() && Integer.parseInt(other) == num) {
                                            conflict = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (conflict) {
                                cells[ii][jj].setBackground(Color.red);
                                // 保持可编辑样式（深绿色边框）
                                applyEditableStyle(ii, jj);
                            } else {
                                printCells(ii, jj); // 恢复宫格背景
                                applyEditableStyle(ii, jj);
                            }
                        }
                        checkCompletion();
                    }
                });
                printCells(i, j);
                sudokuPanel.add(cells[i][j]);
            }
        }

        add(sudokuPanel, BorderLayout.CENTER);
        add(getjPanel(), BorderLayout.SOUTH);

        generatePuzzle();
        setPuzzle();

        setVisible(true);
    }

    // 应用可编辑样式
    private void applyEditableStyle(int row, int col) {
        cells[row][col].setBorder(EDITABLE_BORDER);
    }

    // 应用不可编辑样式
    private void applyNonEditableStyle(int row, int col) {
        cells[row][col].setBorder(NON_EDITABLE_BORDER);
    }

    private void checkCompletion() {
        if (gameCompleted) return;

        boolean allCorrect = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                if (text.isEmpty()) {
                    allCorrect = false;
                    break;
                }
                try {
                    int val = Integer.parseInt(text);
                    if (val != solution[i][j]) {
                        allCorrect = false;
                        break;
                    }
                } catch (NumberFormatException ex) {
                    allCorrect = false;
                    break;
                }
            }
            if (!allCorrect) break;
        }
        if (allCorrect) {
            gameCompleted = true;
            JOptionPane.showMessageDialog(this, "恭喜你挑战成功！！！", "挑战成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void printCells(int i, int j) {
        // 根据宫格位置设置背景色，便于辨识宫
        switch (i / 3) {
            case 0:
            case 2:
                switch (j / 3) {
                    case 0:
                    case 2:
                        cells[i][j].setBackground(Color.white);
                        break;
                    case 1:
                        cells[i][j].setBackground(Color.gray);
                        break;
                }
                break;
            case 1:
                switch (j / 3) {
                    case 0:
                    case 2:
                        cells[i][j].setBackground(Color.gray);
                        break;
                    case 1:
                        cells[i][j].setBackground(Color.white);
                        break;
                }
                break;
        }
    }

    private void generatePuzzle() {
        SudokuGenerator generator = new SudokuGenerator(this);
        puzzle = generator.generateSudokuPuzzle();
        solution = generator.getFullSolution();
    }

    private void setPuzzle() {
        gameCompleted = false;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] != 0) {
                    cells[i][j].setText(Integer.toString(puzzle[i][j]));
                    cells[i][j].setEditable(false);
                    applyNonEditableStyle(i, j);
                } else {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    applyEditableStyle(i, j);
                }
                printCells(i, j); // 设置灰白背景色
            }
        }
    }

    private void setSolution() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText(Integer.toString(solution[i][j]));
                cells[i][j].setEditable(false);
                printCells(i, j);
                applyNonEditableStyle(i, j);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGame());
    }
}