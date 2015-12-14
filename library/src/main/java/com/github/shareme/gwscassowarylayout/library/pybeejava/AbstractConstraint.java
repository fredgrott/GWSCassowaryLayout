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

import java.lang.*;

@SuppressWarnings("unused")
public abstract class AbstractConstraint
{
    private Strength _strength;
    private double _weight;

    private Object _attachedObject;

    /**
     *
     * @param strength the strength
     * @param weight the weight
     */
    public AbstractConstraint(Strength strength, double weight)
    {
        _strength = strength;
        _weight = weight;
    }

    /**
     *
     * @param strength the strength
     */
    public AbstractConstraint(Strength strength)
    {
        _strength = strength;
        _weight = 1.0;
    }

    public AbstractConstraint()
    {
        _strength = Strength.REQUIRED;
        _weight = 1.0;
    }

    public abstract Expression getExpression();

    /**
     *
     * @return false
     */
    public boolean isEditConstraint()
    {
        return false;
    }

    /**
     *
     * @return false
     */
    public boolean isInequality()
    {
        return false;
    }

    /**
     *
     * @return Strength.REQUIRED boolean
     */
    public boolean isRequired()
    {
        return _strength == Strength.REQUIRED;
    }

    /**
     *
     * @return false
     */
    public boolean isStayConstraint()
    {
        return false;
    }

    /**
     *
     * @return _strength
     */
    public Strength getStrength()
    {
        return _strength;
    }

    /**
     *
     * @return _weight
     */
    public double getWeight()
    {
        return _weight;
    }

    public String toString()
    {
        return _strength + " {" + getWeight() + "} (" + getExpression();
    }

    public void setAttachedObject(Object o)
    {
        _attachedObject = o;
    }

    public Object getAttachedObject()
    {
        return _attachedObject;
    }

    private void setStrength(Strength strength)
    {
        _strength = strength;
    }

    private void setWeight(double weight)
    {
        _weight = weight;
    }

}
