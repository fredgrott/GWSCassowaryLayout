package com.github.shareme.gwscassowarylayout.library;


import com.github.shareme.gwscassowarylayout.library.pybeejava.Constraint;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Expression;
import com.github.shareme.gwscassowarylayout.library.pybeejava.SimplexSolver;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Strength;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Variable;

/**
 * Created by alex on 02/11/14.
 */
public class ChildNode extends Node {

    public ChildNode(SimplexSolver solver) {
        super(solver);
    }

    @Override
    protected void createImplicitConstraints(String variableName, Variable variable) {

        if (RIGHT.equals(variableName)) {
            solver.addConstraint(new Constraint(variable, Constraint.Operator.EQ, new Expression(getLeft()).plus(getWidth()), Strength.REQUIRED));
        } else if (BOTTOM.equals(variableName)) {
            solver.addConstraint(new Constraint(variable, Constraint.Operator.EQ, new Expression(getTop()).plus(getHeight()), Strength.REQUIRED));
        } else if (CENTERX.equals(variableName)) {
            solver.addConstraint(new Constraint(variable, Constraint.Operator.EQ, new Expression(getWidth()).divide(2).plus(getLeft()), Strength.REQUIRED));
        } else if (CENTERY.equals(variableName)) {
            solver.addConstraint(new Constraint(variable, Constraint.Operator.EQ, new Expression(getHeight()).divide(2).plus(getTop()), Strength.REQUIRED));
        }

    }

}