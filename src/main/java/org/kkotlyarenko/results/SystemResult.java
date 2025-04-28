package org.kkotlyarenko.results;

import java.util.Arrays;

public class SystemResult {
    private final double[] solution;
    private final double[] residuals;
    private final int iterations;
    private final boolean success;
    private final String message;

    public SystemResult(double[] solution, double[] residuals, int iterations, String message, boolean success) {
        this.solution = solution;
        this.residuals = residuals;
        this.iterations = iterations;
        this.message = message;
        this.success = success;
    }

    public static SystemResult success(double[] solution, double[] residuals, int iterations) {
        return new SystemResult(solution, residuals, iterations, "Solution found successfully.", true);
    }

    public static SystemResult failure(String message) {
        return new SystemResult(null, null, -1, message, false);
    }

    public static SystemResult failure(String message, int iterations, double[] lastApprox, double[] lastResiduals) {
        return new SystemResult(lastApprox, lastResiduals, iterations, message, false);
    }


    public double[] getSolution() {
        return solution;
    }

    public double[] getResiduals() {
        return residuals;
    }

    public int getIterations() {
        return iterations;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("Решение: x = %.8f, y = %.8f\nПогрешности (f1, f2): [%.2e, %.2e]\nИтераций: %d",
                    solution[0], solution[1], residuals[0], residuals[1], iterations);
        } else {
            String iterInfo = iterations >= 0 ? String.format(" (Итераций: %d)", iterations) : "";
            String approxInfo = "";
            if (solution != null) {
                approxInfo = String.format("\nПоследнее приближение: x=%.8f, y=%.8f", solution[0], solution[1]);
            }
            String residualInfo = "";
            if (residuals != null) {
                residualInfo = String.format("\nПогрешности (f1, f2): [%.2e, %.2e]", residuals[0], residuals[1]);
            }
            return "Ошибка: " + message + iterInfo + approxInfo + residualInfo;
        }
    }
}
