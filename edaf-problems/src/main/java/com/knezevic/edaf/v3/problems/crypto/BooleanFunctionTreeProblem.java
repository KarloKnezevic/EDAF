package com.knezevic.edaf.v3.problems.crypto;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.List;
import java.util.Map;

/**
 * Boolean-function optimization where genotype is interpreted as tokenized boolean expression tree.
 */
public final class BooleanFunctionTreeProblem extends AbstractBooleanFunctionProblem<VariableLengthVector<Integer>> {

    private final int maxDepth;

    public BooleanFunctionTreeProblem(int n,
                                      List<String> criteria,
                                      Map<String, Double> criterionWeights,
                                      int maxDepth) {
        super(n, criteria, criterionWeights);
        this.maxDepth = Math.max(2, maxDepth);
    }

    @Override
    public String name() {
        return "boolean-function-tree";
    }

    @Override
    public Fitness evaluate(VariableLengthVector<Integer> genotype) {
        BooleanExpression expr = BooleanExpression.parse(genotype.values(), n, maxDepth);
        int[] truthTable = new int[truthTableSize];

        for (int mask = 0; mask < truthTableSize; mask++) {
            truthTable[mask] = expr.eval(mask) ? 1 : 0;
        }
        return evaluateScalarFitness(truthTable);
    }

    @Override
    public List<String> violations(VariableLengthVector<Integer> genotype) {
        if (genotype == null || genotype.size() == 0) {
            return List.of("Variable-length genotype must contain at least one token");
        }
        return List.of();
    }

    private interface BooleanExpression {
        boolean eval(int inputMask);

        static BooleanExpression parse(List<Integer> tokens, int variables, int maxDepth) {
            TokenReader reader = new TokenReader(tokens);
            return parse(reader, variables, maxDepth, 0);
        }

        private static BooleanExpression parse(TokenReader reader, int variables, int maxDepth, int depth) {
            if (depth >= maxDepth || !reader.hasNext()) {
                return inputMask -> false;
            }

            int token = reader.next();
            int normalized = Math.floorMod(token, variables + 9);

            if (normalized < variables) {
                int var = normalized;
                return inputMask -> ((inputMask >> var) & 1) == 1;
            }

            int op = normalized - variables;
            return switch (op) {
                case 0 -> inputMask -> false;
                case 1 -> inputMask -> true;
                case 2 -> {
                    BooleanExpression child = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> !child.eval(inputMask);
                }
                case 3 -> {
                    BooleanExpression left = parse(reader, variables, maxDepth, depth + 1);
                    BooleanExpression right = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> left.eval(inputMask) && right.eval(inputMask);
                }
                case 4 -> {
                    BooleanExpression left = parse(reader, variables, maxDepth, depth + 1);
                    BooleanExpression right = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> left.eval(inputMask) || right.eval(inputMask);
                }
                case 5 -> {
                    BooleanExpression left = parse(reader, variables, maxDepth, depth + 1);
                    BooleanExpression right = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> left.eval(inputMask) ^ right.eval(inputMask);
                }
                case 6 -> {
                    BooleanExpression left = parse(reader, variables, maxDepth, depth + 1);
                    BooleanExpression right = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> !(left.eval(inputMask) && right.eval(inputMask));
                }
                case 7 -> {
                    BooleanExpression left = parse(reader, variables, maxDepth, depth + 1);
                    BooleanExpression right = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> !(left.eval(inputMask) || right.eval(inputMask));
                }
                case 8 -> {
                    BooleanExpression left = parse(reader, variables, maxDepth, depth + 1);
                    BooleanExpression right = parse(reader, variables, maxDepth, depth + 1);
                    yield inputMask -> left.eval(inputMask) == right.eval(inputMask);
                }
                default -> inputMask -> false;
            };
        }
    }

    private static final class TokenReader {
        private final List<Integer> tokens;
        private int index;

        private TokenReader(List<Integer> tokens) {
            this.tokens = tokens;
            this.index = 0;
        }

        private boolean hasNext() {
            return index < tokens.size();
        }

        private int next() {
            if (!hasNext()) {
                return 0;
            }
            return tokens.get(index++);
        }
    }
}
