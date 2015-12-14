/*
 * Cassowary-Java
 *
 * Copyright (C) 1998-2000 Greg J. Badros
 * Copyright (C) 2014 Russell Keith-Magee.
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *  3. Neither the name of Cassowary nor the names of its contributors may
 *     be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ===========================================================================
 *
 * This port of the Cassowary algorithm was derived from the original Java
 * implmentation, Copyright (C) 1998-2000 Greg J. Badros. That implementation is
 * distributed under the terms of the LGPL; however, dispensation has been granted
 * to release this derivative work under the BSD license; see:
 *
 * https://groups.google.com/d/msg/overconstrained/rqoXuonGGkc/qwHxV6tKkuQJ
 *
 */
package com.github.shareme.gwscassowarylayout.library.pybeejava;

@SuppressWarnings("unused")
public class Constraint extends AbstractConstraint
{
    public enum Operator {
        LEQ (-1),
        EQ (0),
        GEQ (-1);

        private int _value;

        Operator(int value) {
            this._value = value;
        }

        public int getValue() {
            return _value;
        }

        public static Operator fromValue(int value) {
            switch (value) {
                case -1: return LEQ;
                case 0: return EQ;
                case 1: return GEQ;
                default: throw new RuntimeException("Unknown value " + value);
            }
        }
    }

    protected Expression _expression;
    protected boolean _isInequality;

    public Constraint(Expression cle, Strength strength, double weight)
    {
        super(strength, weight);
        _expression = cle;
    }

    public Constraint(Expression cle, Strength strength)
    {
        super(strength, 1.0);
        _expression = cle;
    }

    public Constraint(Expression cle)
    {
        super(Strength.REQUIRED, 1.0);
        _expression = cle;
    }

    public Constraint(Variable clv1, Operator op_enum, Variable clv2, Strength strength, double weight)
            throws InternalError
    {
        super(strength, weight);
        _expression = new Expression(clv2);

        if (op_enum == Operator.GEQ)
        {
            _expression.multiplyMe(-1.0);
            _expression.addVariable(clv1);
            _isInequality = true;
        }
        else if (op_enum == Operator.EQ) {
            _expression.addVariable(clv1,-1.0);
        }
        else if (op_enum == Operator.LEQ)
        {
            _expression.addVariable(clv1,-1.0);
            _isInequality = true;
        }
        else
        {
            // the operator was invalid
            throw new InternalError("Invalid operator in Constraint constructor");
        }
    }

    public Constraint(Variable clv1, Operator op_enum, Variable clv2, Strength strength)
            throws InternalError
    {
        this(clv1, op_enum, clv2, strength, 1.0);
    }

    public Constraint(Variable clv1, Operator op_enum, Variable clv2)
            throws InternalError
    {
        this(clv1, op_enum, clv2, Strength.REQUIRED, 1.0);
    }


    public Constraint(Variable clv, Operator op_enum, double val, Strength strength, double weight)
            throws InternalError
    {
        super(strength, weight);
        _expression = new Expression(val);

        if (op_enum == Operator.GEQ)
        {
            _expression.multiplyMe(-1.0);
            _expression.addVariable(clv);
            _isInequality = true;
        }
        else if (op_enum == Operator.EQ)
        {
            _expression.addVariable(clv, -1.0);
        }
        else if (op_enum == Operator.LEQ)
        {
            _expression.addVariable(clv, -1.0);
            _isInequality = true;
        }
        else
        {
            // the operator was invalid
            throw new InternalError("Invalid operator in Constraint constructor");
        }
    }

    public Constraint(Variable clv, Operator op_enum, double val, Strength strength)
            throws InternalError
    {
        this(clv, op_enum, val, strength, 1.0);
    }

    public Constraint(Variable clv, Operator op_enum, double val)
            throws InternalError
    {
        this(clv, op_enum, val, Strength.REQUIRED, 1.0);
    }

    public Constraint(Expression cle1, Operator op_enum, Expression cle2, Strength strength, double weight)
            throws InternalError
    {
        super(strength, weight);
        _expression = cle2.clone();

        if (op_enum == Operator.GEQ)
        {
            _expression.multiplyMe(-1.0);
            _expression.addExpression(cle1);
            _isInequality = true;
        }
        else if (op_enum == Operator.EQ)
        {
            _expression.addExpression(cle1, -1.0);
        }
        else if (op_enum == Operator.LEQ)
        {
            _expression.addExpression(cle1, -1.0);
            _isInequality = true;
        }
        else
        {
            // the operator was invalid
            throw new InternalError("Invalid operator in Constraint constructor");
        }
    }

    public Constraint(Expression cle1, Operator op_enum, Expression cle2, Strength strength)
            throws InternalError
    {
        this(cle1, op_enum, cle2, strength, 1.0);
    }

    public Constraint(Expression cle1, Operator op_enum, Expression cle2)
            throws InternalError
    {
        this(cle1, op_enum, cle2, Strength.REQUIRED, 1.0);
    }

    public Constraint(AbstractVariable clv, Operator op_enum, Expression cle, Strength strength, double weight)
        throws InternalError
    {
        super(strength, weight);
        _expression = cle.clone();

        if (op_enum == Operator.GEQ)
        {
            _expression.multiplyMe(-1.0);
            _expression.addVariable(clv);
            _isInequality = true;
        }
        else if (op_enum == Operator.EQ)
        {
            _expression.addVariable(clv,-1.0);
        }
        else if (op_enum == Operator.LEQ)
        {
            _expression.addVariable(clv, -1.0);
            _isInequality = true;
        }
        else
        {
            // the operator was invalid
            throw new InternalError("Invalid operator in Constraint constructor");
        }
    }

    public Constraint(AbstractVariable clv, Operator op_enum, Expression cle, Strength strength)
            throws InternalError
    {
        this(clv, op_enum, cle, strength, 1.0);
    }

    public Constraint(AbstractVariable clv, Operator op_enum, Expression cle)
        throws InternalError
    {
        this(clv, op_enum, cle, Strength.REQUIRED, 1.0);
    }

    public Constraint(Expression cle, Operator op_enum, AbstractVariable clv, Strength strength, double weight)
            throws InternalError
    {
        super(strength, weight);
        _expression = cle.clone();

        if (op_enum == Operator.LEQ)
        {
            _expression.multiplyMe(-1.0);
            _expression.addVariable(clv);
            _isInequality = true;
        }
        else if (op_enum == Operator.EQ)
        {
            _expression.addVariable(clv, -1.0);
        }
        else if (op_enum == Operator.GEQ)
        {
            _expression.addVariable(clv, -1.0);
            _isInequality = true;
        }
        else
        {
            // the operator was invalid
            throw new InternalError("Invalid operator in Constraint constructor");
        }
    }

    public Constraint(Expression cle, Operator op_enum, AbstractVariable clv, Strength strength)
        throws InternalError
    {
        this(cle, op_enum, clv, strength, 1.0);
    }

    public Constraint(Expression cle, Operator op_enum, AbstractVariable clv)
            throws InternalError
    {
        this(cle, op_enum, clv, Strength.REQUIRED, 1.0);
    }

    public Expression getExpression()
    {
        return _expression;
    }

    protected void setExpression(Expression expr)
    {
        _expression = expr;
    }

    public final boolean isInequality()
    {
        return _isInequality;
    }

    public final String toString()
    {
        if (_isInequality)
        {
            return super.toString() + " >= 0)";
        }
        else
        {
            return super.toString() + " == 0)";
        }
    }
}
