package org.kkotlyarenko.methods;

import org.kkotlyarenko.results.MethodResult;
import java.util.function.DoubleUnaryOperator;

public class SecantMethod implements EquationSolver {

    private static final int MAX_ITERATIONS = 1000;

    @Override
    public MethodResult solve(DoubleUnaryOperator f, double x0, double x1, double eps) {
        double fx0 = f.applyAsDouble(x0);
        double fx1 = f.applyAsDouble(x1);
        int iterations = 0;

        if (Double.isNaN(fx0) || Double.isNaN(fx1) || Double.isInfinite(fx0) || Double.isInfinite(fx1)) {
            return MethodResult.failure("Значение функции не определено или бесконечно в начальных точках.");
        }

        if (Math.abs(fx0) < eps) {
            return MethodResult.success(x0, fx0, iterations);
        }
        if (Math.abs(fx1) < eps) {
            return MethodResult.success(x1, fx1, iterations);
        }


        double x2 = x1;

        while (iterations < MAX_ITERATIONS) {
            iterations++;

            double denominator = fx1 - fx0;
            if (Math.abs(denominator) < 1e-15) {
                if (Math.abs(fx1) < eps) {
                    return MethodResult.success(x1, fx1, iterations);
                } else {
                    return MethodResult.failure("Делитель близок к нулю (f(x1) - f(x0) ~ 0), метод не может продолжаться.", iterations);
                }
            }

            x2 = x1 - fx1 * (x1 - x0) / denominator;
            double fx2 = f.applyAsDouble(x2);

            if (Double.isNaN(x2) || Double.isInfinite(x2) || Double.isNaN(fx2) || Double.isInfinite(fx2)) {
                return MethodResult.failure("Получено нечисловое значение для x или f(x) на итерации " + iterations, iterations);
            }

            if (Math.abs(x2 - x1) < eps || Math.abs(fx2) < eps) {
                return MethodResult.success(x2, fx2, iterations);
            }

            x0 = x1;
            fx0 = fx1;
            x1 = x2;
            fx1 = fx2;
        }

        return MethodResult.failure("Превышено максимальное количество итераций.", iterations);
    }
}
