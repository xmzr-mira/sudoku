import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SudokuDifficultySelection extends JDialog {
    private int numberToHide = 0; // 保存用户选择的难度对应隐藏数字个数

    /**
     * 构造函数，创建难度选择对话框
     * @param parent 父窗口（用于居中显示）
     */
    public SudokuDifficultySelection(JFrame parent) {
        super(parent, "Difficulty Selection", true); // 模态对话框
        setSize(250, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        JButton easy = new JButton("Easy");
        JButton normal = new JButton("Normal");
        JButton difficult = new JButton("Difficult");

        easy.addActionListener(new DifficultyActionListener("Easy"));
        normal.addActionListener(new DifficultyActionListener("Normal"));
        difficult.addActionListener(new DifficultyActionListener("Difficult"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(easy);
        buttonPanel.add(normal);
        buttonPanel.add(difficult);
        add(buttonPanel);
    }

    // 内部监听器类
    private class DifficultyActionListener implements ActionListener {
        private String difficulty;

        public DifficultyActionListener(String difficulty) {
            this.difficulty = difficulty;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (difficulty) {
                case "Easy":
                    numberToHide = 27;
                    break;
                case "Normal":
                    numberToHide = 36;
                    break;
                case "Difficult":
                    numberToHide = 45;
                    break;
            }
            dispose(); // 关闭对话框
        }
    }

    /**
     * 获取用户选择的隐藏数字个数
     * @return 隐藏数字个数
     */
    public int getNumberToHide() {
        return numberToHide;
    }

    /**
     * 静态便利方法：显示难度选择对话框，并返回用户选择的隐藏数字个数
     * @param parent 父窗口
     * @return 隐藏数字个数（若用户直接关闭对话框，返回0）
     */
    public static int showDialog(JFrame parent) {
        SudokuDifficultySelection dialog = new SudokuDifficultySelection(parent);
        dialog.setVisible(true); // 阻塞直到对话框关闭
        return dialog.getNumberToHide();
    }
}