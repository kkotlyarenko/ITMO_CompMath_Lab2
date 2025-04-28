package org.kkotlyarenko;

import java.util.function.DoubleUnaryOperator;

public class FunctionSet {

    private static final FunctionDefinition[] definitions = {
            new FunctionDefinition(
                    "f(x) = x^3 - 8",
                    x -> Math.pow(x, 3) - 8.0,
                    x -> 3.0 * x * x
            ),
            new FunctionDefinition(
                    "f(x) = exp(x) - 5",
                    x -> Math.exp(x) - 5.0,
                    Math::exp
            ),
            new FunctionDefinition(
                    "f(x) = 2*x - 3",
                    x -> 2.0 * x - 3.0,
                    x -> 2.0
            ),
            new FunctionDefinition(
                    "f(x) = sin(x) - 0.5",
                    x -> Math.sin(x) - 0.5,
                    Math::cos
            ),
            new FunctionDefinition(
                    "f(x) = 3*x^2 - 1",
                    x -> 3.0 * x * x - 1.0,
                    x -> 6.0 * x
            )
    };

    public static String[] descriptions = new String[definitions.length];
    public static DoubleUnaryOperator[] functions = new DoubleUnaryOperator[definitions.length];
    public static DoubleUnaryOperator[] derivatives = new DoubleUnaryOperator[definitions.length];

    static {
        for (int i = 0; i < definitions.length; i++) {
            descriptions[i] = definitions[i].description;
            functions[i] = definitions[i].function;
            derivatives[i] = definitions[i].derivative;
        }
    }

    private record FunctionDefinition(String description, DoubleUnaryOperator function,
                                      DoubleUnaryOperator derivative) {
    }
}
