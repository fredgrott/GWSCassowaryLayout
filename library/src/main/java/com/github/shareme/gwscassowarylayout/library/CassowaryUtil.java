/*
 * Copyright (C) 2014 Agens AS
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shareme.gwscassowarylayout.library;


import com.github.shareme.gwscassowarylayout.library.pybeejava.Constraint;
import com.github.shareme.gwscassowarylayout.library.pybeejava.ConstraintNotFound;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Expression;
import com.github.shareme.gwscassowarylayout.library.pybeejava.SimplexSolver;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Strength;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Variable;

/**
 * CassowaryUtil
 * Created by alex on 08/10/2014.
 */
@SuppressWarnings("unused")
public class CassowaryUtil {

    public static Constraint createWeakEqualityConstraint() {
        return new Constraint(new Expression(null, -1.0, 0), Strength.WEAK);
    }

    public static Constraint createWeakInequalityConstraint(Variable variable, Constraint.Operator op, double value) {
        Expression expression = new Expression(value);
        return new Constraint(variable, op, expression, Strength.STRONG);
    }

    public static void updateConstraint(Constraint constraint, Variable variable, double value) {
        Expression expression = constraint.getExpression();
        expression.setConstant(value);
        expression.setVariable(variable, -1);
    }


    public static Constraint createOrUpdateLeqInequalityConstraint(Variable variable, Constraint constraint, double value, SimplexSolver solver) {
        if (constraint != null) {
            double currentValue = constraint.getExpression().getConstant();
            // This will not detect if the variable or strength has changed.
            if (currentValue != value) {
                try {
                    solver.removeConstraint(constraint);
                } catch (ConstraintNotFound constraintNotFound) {
                    constraintNotFound.printStackTrace();
                }
                constraint = null;
            }
        }

        if (constraint == null) {
            constraint = new Constraint(variable, Constraint.Operator.LEQ, value, Strength.STRONG);
            solver.addConstraint(constraint);
        }

        return constraint;
    }

    public static Constraint createOrUpdateLinearEquationConstraint(Variable variable, Constraint constraint, double value, SimplexSolver solver) {
        if (constraint != null) {
            double currentValue = constraint.getExpression().getConstant();
            // This will not detect if the variable, strength or operation has changed
            if (currentValue != value) {
                try {
                    solver.removeConstraint(constraint);
                } catch (ConstraintNotFound constraintNotFound) {
                    constraintNotFound.printStackTrace();
                }
                constraint = null;
            }
        }

        if (constraint == null) {
            constraint = new Constraint(variable, Constraint.Operator.EQ, value, Strength.STRONG);
            solver.addConstraint(constraint);
        }

        return constraint;
    }
}
