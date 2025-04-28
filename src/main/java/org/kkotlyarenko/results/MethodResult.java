package org.kkotlyarenko.results;

public class MethodResult {
    private final double root;
    private final double functionValueAtRoot;
    private final int iterations;
    private final boolean success;
    private final String message;

    private MethodResult(double root, double functionValueAtRoot, int iterations, String message, boolean success) {
        this.root = root;
        this.functionValueAtRoot = functionValueAtRoot;
        this.iterations = iterations;
        this.message = message;
        this.success = success;
    }


    public static MethodResult success(double root, double functionValueAtRoot, int iterations) {
        return new MethodResult(root, functionValueAtRoot, iterations, "Решение найдено успешно.", true);
    }

    public static MethodResult failure(String message) {
        return new MethodResult(Double.NaN, Double.NaN, -1, message, false);
    }

    public static MethodResult failure(String message, int iterations) {
        return new MethodResult(Double.NaN, Double.NaN, iterations, message, false);
    }

    public static MethodResult failure(String message, int iterations, double lastApprox, double lastFuncVal) {
        return new MethodResult(lastApprox, lastFuncVal, iterations, message, false);
    }


    public double getRoot() {
        return root;
    }

    public double getFunctionValueAtRoot() {
        return functionValueAtRoot;
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
            return String.format("Корень: %.10f\nf(корень): %.2e\nИтераций: %d",
                    root, functionValueAtRoot, iterations);
        } else {
            String iterInfo = iterations >= 0 ? String.format(" (Итераций: %d)", iterations) : "";
            String approxInfo = "";
            if (!Double.isNaN(root)) {
                approxInfo = String.format("\nПоследнее приближение: %.10f\nf(приближение): %.2e", root, functionValueAtRoot);
            }
            return "Ошибка: " + message + iterInfo + approxInfo;
        }
    }
}
