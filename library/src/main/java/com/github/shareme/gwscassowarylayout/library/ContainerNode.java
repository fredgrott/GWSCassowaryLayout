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
import com.github.shareme.gwscassowarylayout.library.pybeejava.Expression;
import com.github.shareme.gwscassowarylayout.library.pybeejava.SimplexSolver;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Strength;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Variable;

/**
 * ContainerNode
 * Created by alex on 10/10/2014.
 */
public class ContainerNode extends Node {

    public ContainerNode(SimplexSolver solver) {
        super(solver);
    }

    @Override
    protected void createImplicitConstraints(String variableName, Variable variable) {

        if (CENTERX.equals(variableName)) {
            solver.addConstraint(new Constraint(variable, Constraint.Operator.EQ, new Expression(getWidth()).divide(2), Strength.REQUIRED));
        } else if (CENTERY.equals(variableName)) {
            solver.addConstraint(new Constraint(variable, Constraint.Operator.EQ, new Expression(getHeight()).divide(2), Strength.REQUIRED));
        }
    }

}
