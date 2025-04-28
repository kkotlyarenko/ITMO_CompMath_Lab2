package org.kkotlyarenko.methods;

import org.kkotlyarenko.results.MethodResult;
import java.util.function.DoubleUnaryOperator;

@FunctionalInterface
public interface EquationSolver {
    MethodResult solve(DoubleUnaryOperator f, double a, double b, double eps);
}
