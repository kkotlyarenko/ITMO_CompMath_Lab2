package org.kkotlyarenko;

import org.kkotlyarenko.methods.*;
import org.kkotlyarenko.results.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.function.DoubleUnaryOperator;


public class MainGUI extends JFrame {
    private final JComboBox<String> taskChoice;
    private final JComboBox<String> methodChoice;
    private final JComboBox<String> functionChoice;
    private final JComboBox<String> systemChoice;
    private final JTextField aField;
    private final JTextField bField;
    private final JTextField epsField;
    private final JTextArea resultArea;
    private final GraphPanel graphPanel;
    private final JButton solveBtn;
    private final JButton loadBtn;
    private final JButton saveBtn;

    private static final String FILE_MARKER_EQUATION = "EQUATION_PARAMS";
    private static final String FILE_MARKER_SYSTEM = "SYSTEM_PARAMS";

    public MainGUI() {
        setTitle("Численные методы решения уравнений и систем");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setResizable(false);
        setLocationRelativeTo(null);

        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 14));
        UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Monospaced", Font.PLAIN, 14));

        JPanel inputPanel = new JPanel();
        inputPanel.setMinimumSize(new Dimension(250, 600));
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        inputPanel.setBackground(Color.WHITE);

        taskChoice = new JComboBox<>(new String[]{"Нелинейное уравнение", "Система нелинейных уравнений"});
        methodChoice = new JComboBox<>(new String[]{"Половинного деления", "Секущих", "Простой итерации"});
        functionChoice = new JComboBox<>(FunctionSet.descriptions);
        systemChoice = new JComboBox<>(SystemFunctionSet.descriptions);
        aField = new JTextField("0");
        bField = new JTextField("1");
        epsField = new JTextField("0.0001");
        solveBtn = new JButton("Решить");
        loadBtn = new JButton("Загрузить из файла");
        saveBtn = new JButton("Сохранить в файл");
        resultArea = new JTextArea(8, 25);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(false);
        resultArea.setBackground(new Color(245, 245, 245));
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Результат"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        inputPanel.add(labeled("1. Выберите задачу:", taskChoice));
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(labeled("2. Выберите метод:", methodChoice));
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(labeled("3. Выберите уравнение:", functionChoice));
        inputPanel.add(Box.createVerticalStrut(0));
        inputPanel.add(labeled("3. Выберите систему:", systemChoice));
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(labeled("4. Левая граница / x0:", aField));
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(labeled("5. Правая граница / y0 (или x1 для секущих):", bField));
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(labeled("6. Точность ε:", epsField));
        inputPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setOpaque(false);
        buttonPanel.add(solveBtn);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(buttonPanel);
        inputPanel.add(Box.createVerticalStrut(10));

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filePanel.setOpaque(false);
        filePanel.setBackground(Color.WHITE);
        filePanel.add(loadBtn);
        filePanel.add(saveBtn);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(filePanel);

        inputPanel.add(Box.createVerticalStrut(15));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputPanel.add(scrollPane);

        inputPanel.add(Box.createVerticalGlue());

        graphPanel = new GraphPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, graphPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);

        add(splitPane);

        taskChoice.addActionListener(e -> onTaskChanged());
        solveBtn.addActionListener(this::onSolveClicked);
        loadBtn.addActionListener(this::onLoadFromFileClicked);
        saveBtn.addActionListener(this::onSaveToFileClicked);

        onTaskChanged();

        setVisible(true);
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        panel.setOpaque(false);
        JLabel jLabel = new JLabel(label);
        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        if (field instanceof JTextField || field instanceof JComboBox) {
            jLabel.setLabelFor(field);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void onTaskChanged() {
        if (taskChoice == null) return;

        boolean isEquation = taskChoice.getSelectedIndex() == 0;

        if(methodChoice!=null) methodChoice.setVisible(isEquation);
        if(functionChoice!=null) functionChoice.setVisible(isEquation);
        if(systemChoice!=null) systemChoice.setVisible(!isEquation);

        setVisibleParent(functionChoice, isEquation);
        setVisibleParent(systemChoice, !isEquation);

        updateFieldLabels(isEquation);

        if (resultArea != null) resultArea.setText("");
        if (graphPanel != null) {
            graphPanel.clear();
        }
    }

    private void setVisibleParent(Component child, boolean visible) {
        if (child != null) {
            Container parent = child.getParent();
            if (parent != null) {
                parent.setVisible(visible);
                Container grandParent = parent.getParent();
                if (grandParent != null) {
                    grandParent.revalidate();
                    grandParent.repaint();
                }
            }
        }
    }

    private void updateFieldLabels(boolean isEquationTask) {
        try {
            if (aField == null || aField.getParent() == null || bField == null || bField.getParent() == null) return;

            Container aParent = aField.getParent();
            if (aParent instanceof JPanel && aParent.getLayout() instanceof BorderLayout) {
                Component aComp = ((BorderLayout) aParent.getLayout()).getLayoutComponent(BorderLayout.NORTH);
                if (aComp instanceof JLabel aLabel) {
                    Container bParent = bField.getParent();
                    if (bParent instanceof JPanel && bParent.getLayout() instanceof BorderLayout) {
                        Component bComp = ((BorderLayout) bParent.getLayout()).getLayoutComponent(BorderLayout.NORTH);
                        if (bComp instanceof JLabel bLabel) {
                            if (isEquationTask) {
                                aLabel.setText("4. Левая граница / x0:");
                                bLabel.setText("5. Правая граница / x1 (для секущих):");
                            } else {
                                aLabel.setText("4. Начальное приближение x0:");
                                bLabel.setText("5. Начальное приближение y0:");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error updating labels in onTaskChanged: " + ex.getMessage());
        }
    }

    private void onSolveClicked(ActionEvent e) {
        solveBtn.setEnabled(false);
        loadBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        resultArea.setText("Вычисление...");
        if (graphPanel != null) graphPanel.clear();

        SolverWorker worker = new SolverWorker();
        worker.execute();
    }

    private class SolverWorker extends SwingWorker<Object, Void> {
        @Override
        protected Object doInBackground() {
            int task = taskChoice.getSelectedIndex();
            double a, b, eps;

            try {
                a = Double.parseDouble(aField.getText().replace(',', '.'));
                b = Double.parseDouble(bField.getText().replace(',', '.'));
                eps = Double.parseDouble(epsField.getText().replace(',', '.'));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Ошибка ввода: проверьте числовые поля (используйте '.' как разделитель).");
            }

            if (eps <= 0) {
                throw new IllegalArgumentException("Точность ε должна быть положительным числом.");
            }


            if (task == 0) {
                if (methodChoice.getSelectedIndex() != 1 && a >= b) {
                    throw new IllegalArgumentException("Левая граница 'a' должна быть строго меньше правой 'b'.");
                }

                int methodIndex = methodChoice.getSelectedIndex();
                int fIndex = functionChoice.getSelectedIndex();
                DoubleUnaryOperator f = FunctionSet.functions[fIndex];
                DoubleUnaryOperator df = FunctionSet.derivatives[fIndex];

                switch (methodIndex) {
                    case 0:
                        double fa = f.applyAsDouble(a);
                        double fb = f.applyAsDouble(b);
                        if (Double.isNaN(fa) || Double.isNaN(fb) || Double.isInfinite(fa) || Double.isInfinite(fb)) {
                            throw new ArithmeticException("Значение функции не определено или бесконечно на границах интервала.");
                        }
                        if (Math.signum(fa) * Math.signum(fb) >= 0) {
                            throw new IllegalArgumentException("Значения функции на концах интервала одного знака. Метод половинного деления неприменим.");
                        }
                        return new BisectionMethod().solve(f, a, b, eps);
                    case 1:
                        return new SecantMethod().solve(f, a, b, eps);
                    case 2:
                        double midPoint = (a + b) / 2.0;
                        double derivativeAtMid;
                        try {
                            derivativeAtMid = df.applyAsDouble(midPoint);
                        } catch (Exception ex) {
                            throw new ArithmeticException("Не удалось вычислить производную в середине интервала (" + midPoint +"): " + ex.getMessage());
                        }

                        if (Double.isNaN(derivativeAtMid) || Double.isInfinite(derivativeAtMid)) {
                            throw new ArithmeticException("Производная не определена или бесконечна в середине интервала (" + midPoint +").");
                        }
                        if (Math.abs(derivativeAtMid) < 1e-12) {
                            throw new ArithmeticException("Производная близка к нулю в середине интервала (" + midPoint +"), невозможно подобрать lambda.");
                        }
                        double lambda = -1.0 / derivativeAtMid;

                        final double MAX_LAMBDA = 1e6;
                        if (Math.abs(lambda) > MAX_LAMBDA) {
                            System.err.println("Warning: Lambda is very large (|lambda| = " + Math.abs(lambda) + "), potential instability.");
                            throw new ArithmeticException("Вычисленное значение lambda (" + lambda + ") слишком велико. Возможно, производная близка к нулю.");
                        }


                        DoubleUnaryOperator phi = x -> x + lambda * f.applyAsDouble(x);
                        DoubleUnaryOperator dphi = x -> 1 + lambda * df.applyAsDouble(x);

                        SimpleIterationMethod siSolver = new SimpleIterationMethod(phi, dphi);
                        return siSolver.solve(f, a, b, eps);
                    default:
                        throw new IllegalStateException("Неизвестный метод");
                }
            } else {
                int systemIndex = systemChoice.getSelectedIndex();
                SystemFunctionSet.SystemDefinition sysDef = SystemFunctionSet.systems[systemIndex];

                NewtonSystemSolver solver = new NewtonSystemSolver(
                        sysDef.f1(), sysDef.f2(),
                        sysDef.dF1dx(), sysDef.dF1dy(),
                        sysDef.dF2dx(), sysDef.dF2dy()
                );
                return solver.solve(a, b, eps);
            }
        }

        @Override
        protected void done() {
            try {
                Object result = get();

                if (result instanceof MethodResult mr) {
                    resultArea.setText(mr.toString());
                    if (mr.isSuccess()) {
                        int fIndex = functionChoice.getSelectedIndex();
                        if (fIndex < 0 || fIndex >= FunctionSet.functions.length) {
                            System.err.println("Invalid function index in done(): " + fIndex);
                            return;
                        }
                        DoubleUnaryOperator f = FunctionSet.functions[fIndex];
                        double left, right;
                        try {
                            left = Double.parseDouble(aField.getText().replace(',', '.'));
                            right = Double.parseDouble(bField.getText().replace(',', '.'));
                        } catch (NumberFormatException nfe) {
                            System.err.println("Не удалось прочитать границы для графика уравнения.");
                            left = mr.getRoot() - 1.0;
                            right = mr.getRoot() + 1.0;
                        }
                        double graphMin = Math.min(left, right) - 1.0;
                        double graphMax = Math.max(left, right) + 1.0;
                        if (graphMax <= graphMin) graphMax = graphMin + 2.0;

                        graphPanel.setFunction(f, graphMin, graphMax, mr.getRoot());
                    }
                } else if (result instanceof SystemResult sr) {
                    resultArea.setText(sr.toString());
                    if (systemChoice == null || SystemFunctionSet.systems == null) {
                        System.err.println("System choice or definitions are null in done().");
                        return;
                    }
                    int systemIndex = systemChoice.getSelectedIndex();
                    if (systemIndex < 0 || systemIndex >= SystemFunctionSet.systems.length) {
                        System.err.println("Invalid system index in done(): " + systemIndex);
                        return;
                    }
                    SystemFunctionSet.SystemDefinition sysDef = SystemFunctionSet.systems[systemIndex];
                    double x0 = 0, y0 = 0, range = 5.0;
                    try {
                        x0 = Double.parseDouble(aField.getText().replace(',', '.'));
                        y0 = Double.parseDouble(bField.getText().replace(',', '.'));
                    } catch (NumberFormatException nfe) {
                        System.err.println("Не удалось прочитать начальное приближение для центрирования графика.");
                    }

                    double xmin = x0 - range;
                    double xmax = x0 + range;
                    double ymin = y0 - range;
                    double ymax = y0 + range;

                    if (sr.isSuccess() && sr.getSolution() != null) {
                        double solX = sr.getSolution()[0];
                        double solY = sr.getSolution()[1];
                        if (Double.isFinite(solX) && Double.isFinite(solY)) {
                            if (solX < xmin) xmin = solX - range/2;
                            if (solX > xmax) xmax = solX + range/2;
                            if (solY < ymin) ymin = solY - range/2;
                            if (solY > ymax) ymax = solY + range/2;
                            graphPanel.setSystemFunctions(sysDef.f1(), sysDef.f2(), xmin, xmax, ymin, ymax, sr.getSolution());
                        } else {
                            graphPanel.setSystemFunctions(sysDef.f1(), sysDef.f2(), xmin, xmax, ymin, ymax, null);
                        }

                    } else if (sr.getSolution() != null) {
                        graphPanel.setSystemFunctions(sysDef.f1(), sysDef.f2(), xmin, xmax, ymin, ymax, sr.getSolution());
                    } else {
                        graphPanel.setSystemFunctions(sysDef.f1(), sysDef.f2(), xmin, xmax, ymin, ymax, null);
                    }
                } else {
                    resultArea.setText("Ошибка: Неожиданный тип результата.");
                }

            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                resultArea.setText("Ошибка выполнения: " + cause.getMessage());
                System.err.println("Stack trace:");
                cause.printStackTrace(System.err);
            } finally {
                solveBtn.setEnabled(true);
                loadBtn.setEnabled(true);
                saveBtn.setEnabled(true);
            }
        }
    }

    private void onLoadFromFileClicked(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Загрузить параметры из файла");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad))) {
                String line;
                String marker = null;
                Integer taskIndex = null, itemIndex = null, methodIndex = null;
                String aVal = null, bVal = null, epsVal = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    if (FILE_MARKER_EQUATION.equals(line)) {
                        marker = FILE_MARKER_EQUATION;
                        taskIndex = 0;
                    } else if (FILE_MARKER_SYSTEM.equals(line)) {
                        marker = FILE_MARKER_SYSTEM;
                        taskIndex = 1;
                    } else if (line.startsWith("---")) {
                        continue;
                    } else if (marker != null) {
                        if (itemIndex == null) {
                            itemIndex = Integer.parseInt(line);
                        } else if (taskIndex == 0 && methodIndex == null) {
                            methodIndex = Integer.parseInt(line);
                        } else if (aVal == null) {
                            aVal = line;
                        } else if (bVal == null) {
                            bVal = line;
                        } else if (epsVal == null) {
                            epsVal = line;
                            break;
                        }
                    }
                }

                if (taskIndex == null || itemIndex == null || aVal == null || bVal == null || epsVal == null || (taskIndex == 0 && methodIndex == null) ) {
                    throw new IOException("Не удалось прочитать все необходимые параметры из файла.");
                }

                taskChoice.setSelectedIndex(taskIndex);
                if (taskIndex == 0) {
                    if(itemIndex >= 0 && itemIndex < functionChoice.getItemCount()) {
                        functionChoice.setSelectedIndex(itemIndex);
                    } else {
                        System.err.println("Загружен неверный индекс функции: " + itemIndex);
                        functionChoice.setSelectedIndex(0);
                    }
                    if(methodIndex >= 0 && methodIndex < methodChoice.getItemCount()) {
                        methodChoice.setSelectedIndex(methodIndex);
                    } else {
                        System.err.println("Загружен неверный индекс метода: " + methodIndex);
                        methodChoice.setSelectedIndex(0);
                    }
                } else {
                    if(itemIndex >= 0 && itemIndex < systemChoice.getItemCount()) {
                        systemChoice.setSelectedIndex(itemIndex);
                    } else {
                        System.err.println("Загружен неверный индекс системы: " + itemIndex);
                        systemChoice.setSelectedIndex(0);
                    }
                }
                aField.setText(aVal);
                bField.setText(bVal);
                epsField.setText(epsVal);

                onTaskChanged();
                resultArea.setText("Параметры загружены из " + fileToLoad.getName());
                if (graphPanel != null) graphPanel.clear();


            } catch (IOException | NumberFormatException | IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(this,
                        "Не удалось загрузить файл: " + ex.getMessage() + "\nУбедитесь, что файл имеет правильный формат.",
                        "Ошибка загрузки", JOptionPane.ERROR_MESSAGE);
                resultArea.setText("Ошибка загрузки файла.");
            }
        }
    }

    private void onSaveToFileClicked(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Сохранить результат в файл");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".txt");
            }

            if (fileToSave.exists()) {
                int response = JOptionPane.showConfirmDialog(this,
                        "Файл " + fileToSave.getName() + " уже существует.\nПерезаписать?",
                        "Подтверждение перезаписи",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }


            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                int task = taskChoice.getSelectedIndex();
                writer.println(task == 0 ? FILE_MARKER_EQUATION : FILE_MARKER_SYSTEM);

                if (task == 0) {
                    writer.println(functionChoice.getSelectedIndex());
                    writer.println(methodChoice.getSelectedIndex());
                } else {
                    writer.println(systemChoice.getSelectedIndex());
                }
                writer.println(aField.getText());
                writer.println(bField.getText());
                writer.println(epsField.getText());

                writer.println();
                writer.println("--- Исходные данные ---");
                writer.println("Задача: " + taskChoice.getSelectedItem());

                if (task == 0) {
                    writer.println("Уравнение: " + functionChoice.getSelectedItem());
                    writer.println("Метод: " + methodChoice.getSelectedItem());
                    writer.println("Параметры:");
                    writer.println("  a/x0 = " + aField.getText());
                    writer.println("  b/x1 = " + bField.getText());
                    writer.println("  eps  = " + epsField.getText());
                } else {
                    writer.println("Система: " + systemChoice.getSelectedItem());
                    writer.println("Метод: Метод Ньютона");
                    writer.println("Параметры:");
                    writer.println("  x0 = " + aField.getText());
                    writer.println("  y0 = " + bField.getText());
                    writer.println("  eps = " + epsField.getText());
                }

                writer.println("\n--- Результат ---");
                String[] resultLines = resultArea.getText().split("\\n");
                for (String line : resultLines) {
                    if (!line.contains("Результат сохранен в файл:")) {
                        writer.println(line);
                    }
                }

                resultArea.append("\n\nРезультат сохранен в файл: " + fileToSave.getName());

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Не удалось сохранить файл: " + ex.getMessage(),
                        "Ошибка сохранения", JOptionPane.ERROR_MESSAGE);
                resultArea.append("\n\nОшибка сохранения файла.");
            }
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Не удалось установить Look and Feel 'Nimbus', используется по умолчанию.");
        }
        SwingUtilities.invokeLater(MainGUI::new);
    }

    static class SystemFunctionSet {
        record SystemDefinition( String description, NewtonSystemSolver.Function2Var f1, NewtonSystemSolver.Function2Var f2, NewtonSystemSolver.Function2Var dF1dx, NewtonSystemSolver.Function2Var dF1dy, NewtonSystemSolver.Function2Var dF2dx, NewtonSystemSolver.Function2Var dF2dy) {}
        static String[] descriptions = {
                "sin(x)+2y=2; x+cos(y−1)=0.7",
                "x²+y²=4; y=x²-2",
                "e^(x-y)+x*y=1; x²+y²=4"
        };
        static SystemDefinition[] systems = {
                new SystemDefinition(
                        descriptions[0],
                        (x, y) -> Math.sin(x) + 2 * y - 2,
                        (x, y) -> x + Math.cos(y - 1) - 0.7,
                        (x, y) -> Math.cos(x),
                        (x, y) -> 2.0,
                        (x, y) -> 1.0,
                        (x, y) -> -Math.sin(y - 1)
                ),
                new SystemDefinition(
                        descriptions[1],
                        (x, y) -> x * x + y * y - 4,
                        (x, y) -> y - x*x + 2,
                        (x, y) -> 2 * x,
                        (x, y) -> 2 * y,
                        (x, y) -> -2 * x,
                        (x, y) -> 1.0
                ),
                new SystemDefinition(
                        descriptions[2],
                        (x, y) -> Math.exp(x - y) + x * y - 1,
                        (x, y) -> x * x + y * y - 4,
                        (x, y) -> Math.exp(x - y) + y,
                        (x, y) -> -Math.exp(x - y) + x,
                        (x, y) -> 2 * x,
                        (x, y) -> 2 * y
                )
        };
    }

}
