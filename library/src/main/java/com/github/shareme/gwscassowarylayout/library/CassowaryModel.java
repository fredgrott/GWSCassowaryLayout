package com.github.shareme.gwscassowarylayout.library;

import android.content.Context;

import com.github.shareme.gwscassowarylayout.library.pybeejava.Constraint;
import com.github.shareme.gwscassowarylayout.library.pybeejava.ConstraintNotFound;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Expression;
import com.github.shareme.gwscassowarylayout.library.pybeejava.SimplexSolver;
import com.github.shareme.gwscassowarylayout.library.pybeejava.Variable;

import java.util.HashMap;

import timber.log.Timber;


/**
 * Created by alex on 01/11/14.
 */
public class CassowaryModel {

    private Context context;

    private static final String LOG_TAG = "CassowaryModel";

    public CassowaryModel(Context context) {
        this.context = context;
        setupCassowary();
    }

    private HashMap<String, ChildNode> nodes = new HashMap<String, ChildNode>();

    private SimplexSolver solver = new SimplexSolver();

    private ContainerNode containerNode = new ContainerNode(solver);

    private ConstraintParser.CassowaryVariableResolver cassowaryVariableResolver = new ConstraintParser.CassowaryVariableResolver() {
        @Override
        public Variable resolveVariable(String variableName) {
            return CassowaryModel.this.resolveVariable(variableName);
        }

        @Override
        public Expression resolveConstant(String constantName) {

            Expression expression = null;
            Double value;

            try {
                value = Double.parseDouble(constantName);
            } catch (NumberFormatException e) {
                value = DimensionParser.getDimension(constantName, getContext());

            }

            if (value != null) {
                expression = new Expression(value);
            }
            return expression;
        }
    };


    private Variable resolveVariable(String variableName) {
        Variable variable = null;

        String[] stringArray = variableName.split("\\.");

        if (stringArray.length > 1) {
            String nodeName = stringArray[0];
            String propertyName = stringArray[1];

            if (nodeName != null) {
                if ("container".equals(nodeName) || "parent".equals(nodeName)) {
                    variable = containerNode.getVariable(propertyName);
                } else {
                    Node node = getNodeByName(nodeName);
                    if (node != null) {
                        variable = node.getVariable(propertyName);
                    }

                }
            }

        }
        if (variable == null) {
            throw new RuntimeException("unknown variable " + variableName);
        }
        return variable;
    }

    public ChildNode getNodeByName(String name) {
        ChildNode node = nodes.get(name);
        if (node == null) {
            node = new ChildNode(solver);
            nodes.put(name, node);
        }
        return node;
    }

    private Context getContext() {
        return context;
    }

    private void setupCassowary() {
        Timber.d(LOG_TAG, "setupCassowary");
        solver.setAutosolve(false);
    }

    public void addConstraint(Constraint constraint) {
        solver.addConstraint(constraint);
    }

    public Constraint addConstraint(String constraintString) {
        Timber.d(LOG_TAG, "adding constraint " + constraintString);
        Constraint constraint = ConstraintParser.parseConstraint(constraintString, cassowaryVariableResolver);
        addConstraint(constraint);
        return constraint;
    }

    public void removeConstraint(Constraint constraint) {
        try {
            solver.removeConstraint(constraint);
        } catch (ConstraintNotFound constraintNotFound) {
            constraintNotFound.printStackTrace();
        }
    }

    public void addConstraints(CharSequence[] constraints) {
        for (CharSequence constraint : constraints) {
            try {
                addConstraint(constraint.toString());
            } catch (RuntimeException e) {
                Timber.e(LOG_TAG, "could not add constraint " + constraint.toString(), e);
            }

        }
    }

    public void addConstraints(int id) {
        String[] constraints = context.getResources().getStringArray(id);
        addConstraints(constraints);
    }

    public Node getContainerNode() {
        return containerNode;
    }

    public void solve() {
        long timeBeforeSolve = System.nanoTime();

        solver.solve();

        Timber.d(LOG_TAG, "solve took " + TimerUtil.since(timeBeforeSolve));
    }
}
