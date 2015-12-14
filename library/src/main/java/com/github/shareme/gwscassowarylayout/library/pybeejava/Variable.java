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

import java.util.Hashtable;

@SuppressWarnings("unused")
public class Variable extends AbstractVariable
{
    private static Hashtable _ourVarMap;

    private double _value;

    private Object _attachedObject;

    private VariableObserver _observer;

    @SuppressWarnings("unchecked")
    public Variable(String name, double value)
    {
        super(name);
        _value = value;
        if (_ourVarMap != null)
        {
            _ourVarMap.put(name,this);
        }
    }

    @SuppressWarnings("unchecked")
    public Variable(String name)
    {
        super(name);
        _value = 0.0;
        if (_ourVarMap != null)
        {
            _ourVarMap.put(name,this);
        }
    }

    public Variable(double value)
    {
        _value = value;
    }

    public Variable()
    {
        _value = 0.0;
    }


    public Variable(long number, String prefix, double value)
    {
        super(number,prefix);
        _value = value;
    }

    public Variable(long number, String prefix)
    {
        super(number,prefix);
        _value = 0.0;
    }

    public boolean isDummy()
    {
        return false;
    }

    public boolean isExternal()
    {
        return true;
    }

    public boolean isPivotable()
    {
        return false;
    }

    public boolean isRestricted()
    {
        return false;
    }

    public String toString()
    {
        return "[" + getName() + ":" + _value + "]";
    }

    // change the value held -- should *not* use this if the variable is
    // in a solver -- instead use addEditVar() and suggestValue() interface
    public final double getValue()
    {
        return _value;
    }

    public final void setValue(double value)
    {
        _value = value;
    }

    // permit overriding in subclasses in case something needs to be
    // done when the value is changed by the solver
    // may be called when the value hasn't actually changed -- just
    // means the solver is setting the external variable
    public void changeValue(double value)
    {
        _value = value;
        if (_observer != null) {
            _observer.onVariableChanged(this);
        }
    }

    public void setAttachedObject(Object o)
    {
        _attachedObject = o;
    }

    public Object getAttachedObject()
    {
        return _attachedObject;
    }

    public static void setVarMap(Hashtable map)
    {
        _ourVarMap = map;
    }

    public static Hashtable getVarMap()
    {
        return _ourVarMap;
    }

    public Expression times(double val)
    {
        return new Expression(this, val);
    }

    public Expression times(Expression var)
            throws NonlinearExpression
    {
        return var.times(new Expression(this));
    }

    public Expression plus(double val)
    {
        return new Expression(this).plus(new Expression(val));
    }

    public VariableObserver getObserver() {
        return _observer;
    }

    public void setObserver(VariableObserver variableObserver) {
        this._observer = variableObserver;
    }
}
