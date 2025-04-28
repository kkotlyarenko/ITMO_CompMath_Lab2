package org.kkotlyarenko.methods;

import org.kkotlyarenko.results.SystemResult;

public class NewtonSystemSolver {

    @FunctionalInterface
    public interface Function2Var {
        double apply(double x, double y);
    }

    private static final int MAX_ITERATIONS = 500;
    private static final double JACOBIAN_ZERO_THRESHOLD = 1e-12;

    private final Function2Var f1;
    private final Function2Var f2;
    private final Function2Var dF1dx;
    private final Function2Var dF1dy;
    private final Function2Var dF2dx;
    private final Function2Var dF2dy;

    public NewtonSystemSolver(
            Function2Var f1, Function2Var f2,
            Function2Var dF1dx, Function2Var dF1dy,
            Function2Var dF2dx, Function2Var dF2dy
    ) {
        this.f1 = f1;
        this.f2 = f2;
        this.dF1dx = dF1dx;
        this.dF1dy = dF1dy;
        this.dF2dx = dF2dx;
        this.dF2dy = dF2dy;
    }

    public SystemResult solve(double x0, double y0, double eps) {
        double x = x0;
        double y = y0;
        int iterations = 0;
        double[] residuals = new double[2];
        double[] currentSolution = new double[]{x, y};

        while (iterations < MAX_ITERATIONS) {
            iterations++;

            double f1Val = f1.apply(x, y);
            double f2Val = f2.apply(x, y);
            residuals[0] = f1Val;
            residuals[1] = f2Val;
            currentSolution[0] = x;
            currentSolution[1] = y;

            if (Double.isNaN(f1Val) || Double.isNaN(f2Val) || Double.isInfinite(f1Val) || Double.isInfinite(f2Val)) {
                return SystemResult.failure("Значение функции не определено или бесконечно в точке (" + x + ", " + y + ")", iterations, currentSolution, residuals);
            }

            double df1dxVal = dF1dx.apply(x, y);
            double df1dyVal = dF1dy.apply(x, y);
            double df2dxVal = dF2dx.apply(x, y);
            double df2dyVal = dF2dy.apply(x, y);

            if (Double.isNaN(df1dxVal) || Double.isNaN(df1dyVal) || Double.isNaN(df2dxVal) || Double.isNaN(df2dyVal) ||
                    Double.isInfinite(df1dxVal) || Double.isInfinite(df1dyVal) || Double.isInfinite(df2dxVal) || Double.isInfinite(df2dyVal)) {
                return SystemResult.failure("Значение производной не определено или бесконечно в точке (" + x + ", " + y + ")", iterations, currentSolution, residuals);
            }

            double J = df1dxVal * df2dyVal - df1dyVal * df2dxVal;

            if (Math.abs(J) < JACOBIAN_ZERO_THRESHOLD) {
                boolean maybeSolution = Math.max(Math.abs(f1Val), Math.abs(f2Val)) < eps * 10;
                String message = "Якобиан близок к нулю (сингулярная матрица). ";
                message += maybeSolution ? "Возможно, найдено приближенное решение." : "Решение не может быть найдено.";
                return SystemResult.failure(message, iterations, currentSolution, residuals);
            }

            double dx = -(f1Val * df2dyVal - f2Val * df1dyVal) / J;
            double dy = -(df1dxVal * f2Val - df2dxVal * f1Val) / J;

            x += dx;
            y += dy;

            if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y)) {
                return SystemResult.failure("Получено нечисловое значение для x или y на итерации " + iterations, iterations, currentSolution, residuals);
            }


            double error = Math.max(Math.abs(dx), Math.abs(dy));
            double residualNorm = Math.max(Math.abs(f1.apply(x, y)), Math.abs(f2.apply(x, y)));

            if (error < eps && residualNorm < eps) {
                residuals[0] = f1.apply(x, y);
                residuals[1] = f2.apply(x, y);
                currentSolution[0] = x;
                currentSolution[1] = y;
                return SystemResult.success(currentSolution, residuals, iterations);
            }
        }

        residuals[0] = f1.apply(x, y);
        residuals[1] = f2.apply(x, y);
        currentSolution[0] = x;
        currentSolution[1] = y;
        return SystemResult.failure("Превышено максимальное количество итераций.", iterations, currentSolution, residuals);
    }
}
