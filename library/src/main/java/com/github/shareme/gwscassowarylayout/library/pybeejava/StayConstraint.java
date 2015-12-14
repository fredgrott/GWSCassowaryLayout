package com.github.shareme.gwscassowarylayout.library.pybeejava;

@SuppressWarnings("unused")
public class StayConstraint extends AbstractConstraint
{
    protected Variable  _variable;

    // cache the expresion
    private Expression _expression;

    public StayConstraint(Variable var, Strength strength, double weight)
    {
        super(strength, weight);
        _variable = var;
        _expression = new Expression(_variable, -1.0, _variable.getValue());
    }

    public StayConstraint(Variable var, Strength strength)
    {
        this(var, strength, 1.0);
    }

    public StayConstraint(Variable var)
    {
        this(var, Strength.REQUIRED, 1.0);
        _variable = var;
    }

    public Variable getVariable()
    {
        return _variable;
    }

    public Expression getExpression()
    {
        return _expression;
    }

    private void setVariable(Variable v)
    {
        _variable = v;
    }

    public boolean isStayConstraint()
    {
        return true;
    }

    public String toString()
    {
        return "stay " + super.toString();
    }

}
