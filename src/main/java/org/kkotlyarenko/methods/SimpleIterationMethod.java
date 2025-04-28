package org.kkotlyarenko.methods;

import org.kkotlyarenko.results.MethodResult;
import java.util.function.DoubleUnaryOperator;

public class SimpleIterationMethod implements EquationSolver {

    private static final int MAX_ITERATIONS = 50000;
    private final DoubleUnaryOperator phi;
    private final DoubleUnaryOperator dphi;

    public SimpleIterationMethod(DoubleUnaryOperator phi, DoubleUnaryOperator dphi) {
        this.phi = phi;
        this.dphi = dphi;
    }

    @Override
    public MethodResult solve(DoubleUnaryOperator f, double a, double b, double eps) {

        double x0 = a + (b - a) / 2.0;
        double dphiX0 = dphi.applyAsDouble(x0);

        System.out.printf("SI Start: a=%.4f, b=%.4f, x0=%.4f, phi'(x0)=%.4f\n", a, b, x0, dphiX0);

        if (Double.isNaN(dphiX0) || Double.isInfinite(dphiX0)) {
            return MethodResult.failure("Производная phi'(x) не определена или бесконечна в начальной точке x0=" + x0);
        }

        double x = x0;
        int iterations = 0;
        double nextX = x;
        double error = Double.MAX_VALUE;

        while (iterations < MAX_ITERATIONS) {
            iterations++;
            double prevX = x;
            nextX = phi.applyAsDouble(x);

            double currentDphi = Double.NaN;
            try {
                currentDphi = dphi.applyAsDouble(x);
            } catch (Exception ignore) {}
            System.out.printf("SI Iter %d: x=%.10f, nextX=%.10f, diff=%.3e, phi'(x)=%.4f\n",
                    iterations, x, nextX, Math.abs(nextX - x), currentDphi);


            if (Double.isNaN(nextX) || Double.isInfinite(nextX)) {
                System.err.println("SI Error: NaN/Infinity detected.");
                return MethodResult.failure("Получено нечисловое значение phi(x) на итерации " + iterations, iterations, prevX, f.applyAsDouble(prevX));
            }

            if (nextX < a || nextX > b) {
                System.err.printf("SI Warning: Iteration %d exited interval [%.4f, %.4f]. nextX = %.10f\n", iterations, a, b, nextX);
            }


            error = Math.abs(nextX - x);

            if (error < eps) {
                double fValue = f.applyAsDouble(nextX);
                if (Math.abs(fValue) < eps * 10 || error < eps * 0.1) {
                    System.out.printf("SI Converged: Iter=%d, Root=%.10f, f(Root)=%.3e, Error=%.3e\n", iterations, nextX, fValue, error);
                    return MethodResult.success(nextX, fValue, iterations);
                } else {
                    System.err.printf("SI Warning: |x_k - x_{k-1}| < eps (%.2e), but |f(x_k)| is still large (%.2e) at iter %d\n", error, Math.abs(fValue), iterations);
                }
            }

            x = nextX;
        }

        double finalFValue = f.applyAsDouble(nextX);
        System.err.println("SI Error: Max iterations exceeded.");
        return MethodResult.failure("Превышено максимальное количество итераций ("+ MAX_ITERATIONS +"). Последняя оценка ошибки: " + error, iterations, nextX, finalFValue);
    }
}