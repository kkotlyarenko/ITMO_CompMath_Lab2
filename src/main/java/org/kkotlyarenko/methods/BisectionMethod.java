package org.kkotlyarenko.methods;

import org.kkotlyarenko.results.MethodResult;
import java.util.function.DoubleUnaryOperator;

public class BisectionMethod implements EquationSolver {

    private static final int MAX_ITERATIONS = 10000;

    @Override
    public MethodResult solve(DoubleUnaryOperator f, double a, double b, double eps) {
        double fa = f.applyAsDouble(a);
        double fb = f.applyAsDouble(b);

        if (Double.isNaN(fa) || Double.isNaN(fb) || Double.isInfinite(fa) || Double.isInfinite(fb)) {
            return MethodResult.failure("Значение функции не определено или бесконечно на границах интервала.");
        }

        if (Math.signum(fa) * Math.signum(fb) >= 0) {
            if (Math.abs(fa) < eps) return MethodResult.success(a, fa, 0);
            if (Math.abs(fb) < eps) return MethodResult.success(b, fb, 0);
            return MethodResult.failure("Значения функции на концах интервала одного знака или равны нулю. Метод не гарантирует корень внутри интервала.");
        }

        double c = a;
        int iterations = 0;
        double errorEstimate = Math.abs(b - a);

        while (errorEstimate > eps) {
            if (iterations >= MAX_ITERATIONS) {
                double lastRoot = (a + b) / 2.0;
                double lastFVal = f.applyAsDouble(lastRoot);
                return MethodResult.failure("Превышено максимальное количество итераций (" + MAX_ITERATIONS + ").", iterations, lastRoot, lastFVal);
            }

            c = a + (b - a) / 2.0;
            double fc = f.applyAsDouble(c);
            iterations++;

            if (Double.isNaN(fc) || Double.isInfinite(fc)) {
                return MethodResult.failure("Значение функции не определено или бесконечно в точке c = " + c, iterations);
            }

            if (fc == 0.0 || Math.abs(b - a) / 2.0 < eps) {
                errorEstimate = Math.abs(b - a) / 2.0;
                break;
            }

            if (Math.signum(fa) * Math.signum(fc) < 0) {
                b = c;
                fb = fc;
            } else {
                a = c;
                fa = fc;
            }
            errorEstimate = Math.abs(b - a);
        }

        double finalRoot = (a + b) / 2.0;
        double finalFValue = f.applyAsDouble(finalRoot);
        return MethodResult.success(finalRoot, finalFValue, iterations);
    }
}
