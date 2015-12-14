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

import android.annotation.SuppressLint;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

@SuppressWarnings("unused")
public class SimplexSolver extends Tableau
{
    //// BEGIN PRIVATE INSTANCE FIELDS

    // the arrays of positive and negative error vars for the stay constraints
    // (need both positive and negative since they have only non-negative values)
    private Vector<SlackVariable> _stayMinusErrorVars;
    private Vector<SlackVariable> _stayPlusErrorVars;

    // give error variables for a non required constraint,
    // maps to SlackVariable-s
    private Hashtable<AbstractConstraint, Set<AbstractVariable>> _errorVars;

    // Return a lookup table giving the marker variable for each
    // constraint (used when deleting a constraint).
    private Hashtable<AbstractConstraint, AbstractVariable> _markerVars;

    private ObjectiveVariable _objective;

    // Map edit variables to EditInfo-s.
    // EditInfo instances contain all the information for an
    // edit constraint (the edit plus/minus vars, the index [for old-style
    // resolve(Vector...) interface], and the previous value.
    // (EditInfo replaces the parallel vectors from the Smalltalk impl.)
    private Hashtable<Variable, EditInfo> _editVarMap; // map Variable to a EditInfo

    private long _slackCounter;
    private long _artificialCounter;
    private long _dummyCounter;

    private Vector<Double> _resolve_pair;

    private double _epsilon;

    private boolean _fOptimizeAutomatically;
    private boolean _fNeedsSolving;

    private Stack<Integer> _stkCedcns;


    // Ctr initializes the fields, and creates the objective row
    public SimplexSolver()
    {
        _stayMinusErrorVars = new Vector<>();
        _stayPlusErrorVars = new Vector<>();
        _errorVars = new Hashtable<>();
        _markerVars = new Hashtable<>();

        _resolve_pair = new Vector<>(2);
        _resolve_pair.addElement(0.0);
        _resolve_pair.addElement(0.0);

        _objective = new ObjectiveVariable("Z");

        _editVarMap = new Hashtable<>();

        _slackCounter = 0;
        _artificialCounter = 0;
        _dummyCounter = 0;
        _epsilon = 1e-8;

        _fOptimizeAutomatically = true;
        _fNeedsSolving = false;

        Expression e = new Expression();
        _rows.put(_objective, e);
        _stkCedcns = new Stack<>();
        _stkCedcns.push(0);
    }

    // Convenience function for creating a linear inequality constraint
    public final SimplexSolver addLowerBound(AbstractVariable v, double lower)
            throws RequiredFailure, InternalError
    {
        Constraint cn = new Constraint(v, Constraint.Operator.GEQ, new Expression(lower));
        return addConstraint(cn);
    }

    // Convenience function for creating a linear inequality constraint
    public final SimplexSolver addUpperBound(AbstractVariable v, double upper)
            throws RequiredFailure, InternalError
    {
        Constraint cn = new Constraint(v, Constraint.Operator.LEQ, new Expression(upper));
        return addConstraint(cn);
    }

    // Convenience function for creating a pair of linear inequality constraint
    public final SimplexSolver addBounds(AbstractVariable v, double lower, double upper)
    throws RequiredFailure, InternalError
    {
        addLowerBound(v, lower);
        addUpperBound(v, upper);
        return this;
    }

    // Add constraint "cn" to the solver
    public final SimplexSolver addConstraint(AbstractConstraint cn)
            throws RequiredFailure, InternalError
    {
        Vector eplus_eminus = new Vector<>(2);
        Double prevEConstant = 0.0;
        Expression expr = newExpression(cn, eplus_eminus, prevEConstant);
        boolean fAddedOkDirectly = false;

        fAddedOkDirectly = tryAddingDirectly(expr);
        if (!fAddedOkDirectly)
        {
            // could not add directly
            addWithArtificialVariable(expr);
        }

        _fNeedsSolving = true;

        if (cn.isEditConstraint())
        {
            int i = _editVarMap.size();
            EditConstraint cnEdit = (EditConstraint) cn;
            AbstractVariable clvEplus = (AbstractVariable) eplus_eminus.elementAt(0);
            AbstractVariable clvEminus = (AbstractVariable) eplus_eminus.elementAt(1);
            _editVarMap.put(
                cnEdit.getVariable(),
                new EditInfo(cnEdit, clvEplus, clvEminus, prevEConstant, i)
            );
        }

        if (_fOptimizeAutomatically)
        {
            optimize(_objective);
            setExternalVariables();
        }

        return this;
    }

    // Same as addConstraint, except returns false if the constraint
    // resulted in an unsolvable system (instead of throwing an exception)
    public final boolean addConstraintNoException(AbstractConstraint cn)
            throws InternalError
    {
        try
        {
            addConstraint(cn);
            return true;
        }
        catch (RequiredFailure e)
        {
            return false;
        }
    }

    // Add an edit constraint for "v" with given strength
    public final SimplexSolver addEditVar(Variable v, Strength strength)
            throws InternalError
    {
        try
        {
            EditConstraint cnEdit = new EditConstraint(v, strength);
            return addConstraint(cnEdit);
        }
        catch (RequiredFailure e)
        {
            // should not get this
            throw new InternalError("Required failure when adding an edit variable");
        }
    }

    // default to strength = strong
    public final SimplexSolver addEditVar(Variable v)
            throws InternalError
    {
        return addEditVar(v, Strength.STRONG);
    }

    // Remove the edit constraint previously added for variable v
    public final SimplexSolver removeEditVar(Variable v)
            throws InternalError, ConstraintNotFound
    {
        EditInfo cei = _editVarMap.get(v);
        AbstractConstraint cn = cei.getConstraint();
        removeConstraint(cn);
        return this;
    }

    // beginEdit() should be called before sending
    // resolve() messages, after adding the appropriate edit variables
    @SuppressLint("Assert")
    public final SimplexSolver beginEdit()
            throws InternalError
    {
        assert _editVarMap.size() > 0;
        // may later want to do more in here
        _infeasibleRows.clear();
        resetStayConstants();
        _stkCedcns.addElement(_editVarMap.size());
        return this;
    }

    // endEdit should be called after editing has finished
    // for now, it just removes all edit variables
    @SuppressLint("Assert")
    public final SimplexSolver endEdit()
            throws InternalError
    {
        assert _editVarMap.size() > 0;
        resolve();
        _stkCedcns.pop();
        int n = (Integer) _stkCedcns.peek();
        removeEditVarsTo(n);
        // may later want to do more in here
        return this;
    }

    // removeAllEditVars() just eliminates all the edit constraints
    // that were added
    public final SimplexSolver removeAllEditVars()
            throws InternalError
    {
        return removeEditVarsTo(0);
    }

    // remove the last added edit vars to leave only n edit vars left
    @SuppressLint("Assert")
    public final SimplexSolver removeEditVarsTo(int n)
            throws InternalError
    {
        try
        {
            // Need to use an enumeration here to avoid concurrent modifications
            Enumeration<Variable> e = _editVarMap.keys();
            while (e.hasMoreElements())
            {
                Variable v = e.nextElement();
                EditInfo cei = _editVarMap.get(v);
                if (cei.getIndex() >= n)
                {
                    removeEditVar(v);
                }
            }
            assert _editVarMap.size() == n;

            return this;
        }
        catch (ConstraintNotFound e)
        {
            // should not get this
            throw new InternalError("Constraint not found in removeEditVarsTo");
        }
    }

    // Add a stay of the given strength (default to weak) of v to the tableau
    public final SimplexSolver addStay(Variable v, Strength strength, double weight)
            throws RequiredFailure, InternalError
    {
        StayConstraint cn = new StayConstraint(v, strength, weight);
        return addConstraint(cn);
    }

    // default to weight == 1.0
    public final SimplexSolver addStay(Variable v, Strength strength)
            throws RequiredFailure, InternalError
    {
        addStay(v, strength, 1.0);
        return this;
    }

    // default to strength = weak
    public final SimplexSolver addStay(Variable v)
            throws RequiredFailure, InternalError
    {
        addStay(v, Strength.WEAK, 1.0);
        return this;
    }


    // Remove the constraint cn from the tableau
    // Also remove any error variable associated with cn
    public final SimplexSolver removeConstraint(AbstractConstraint cn)
            throws ConstraintNotFound, InternalError
    {
        _fNeedsSolving = true;

        resetStayConstants();

        Expression zRow = rowExpression(_objective);

        Set<AbstractVariable> eVars = _errorVars.get(cn);
        if (eVars != null) {
            for (AbstractVariable clv: eVars)
            {
                final Expression expr = rowExpression(clv);
                if (expr == null)
                {
                    zRow.addVariable(clv, -cn.getWeight() * cn.getStrength().getValue(),
                    _objective, this);
                }
                else
                { // the error variable was in the basis
                    zRow.addExpression(expr, -cn.getWeight() * cn.getStrength().getValue(),
                    _objective, this);
                }
            }
        }

        AbstractVariable marker = (AbstractVariable) _markerVars.remove(cn);
        if (marker == null)
        {
            throw new ConstraintNotFound();
        }

        if (rowExpression(marker) == null)
        {
            // not in the basis, so need to do some work
            Set<AbstractVariable> col = _columns.get(marker);

            AbstractVariable exitVar = null;
            double minRatio = 0.0;
            for (AbstractVariable v: col)
            {
                if (v.isRestricted() ) {
                    final Expression expr = rowExpression(v);
                    double coeff = expr.coefficientFor(marker);
                    if (coeff < 0.0)
                    {
                        double r = -expr.getConstant() / coeff;
                        if (exitVar == null || r < minRatio)
                        {
                            minRatio = r;
                            exitVar = v;
                        }
                    }
                }
            }
            if (exitVar == null)
            {
                for (AbstractVariable v: col)
                {
                    if (v.isRestricted())
                    {
                        final Expression expr = rowExpression(v);
                        double coeff = expr.coefficientFor(marker);
                        double r = expr.getConstant() / coeff;
                        if (exitVar == null || r < minRatio)
                        {
                            minRatio = r;
                            exitVar = v;
                        }
                    }
                }
            }

            if (exitVar == null)
            {
                // exitVar is still null
                if (col.size() == 0)
                {
                    removeColumn(marker);
                }
                else
                {
                    exitVar = col.iterator().next();
                }
            }

            if (exitVar != null)
            {
                pivot(marker, exitVar);
            }
        }

        if (rowExpression(marker) != null )
        {
            Expression expr = removeRow(marker);
            expr = null;
        }

        if (eVars != null)
        {
            for (AbstractVariable v: eVars)
            {
                // FIXGJBNOW != or equals?
                if (v != marker)
                {
                    removeColumn(v);
                    v = null;
                }
            }
        }

        if (cn.isStayConstraint())
        {
            if (eVars != null) {
                for (int i = 0; i < _stayPlusErrorVars.size(); i++)
                {
                    eVars.remove(_stayPlusErrorVars.elementAt(i));
                    eVars.remove(_stayMinusErrorVars.elementAt(i));
                }
            }
        }
        else if (cn.isEditConstraint())
        {
            // assert eVars != null;
            EditConstraint cnEdit = (EditConstraint) cn;
            Variable clv = cnEdit.getVariable();
            EditInfo cei = _editVarMap.get(clv);
            AbstractVariable clvEditMinus = cei.getClvEditMinus();
            // Variable clvEditPlus = cei.ClvEditPlus();
            // the clvEditPlus is a marker variable that is removed elsewhere
            removeColumn(clvEditMinus);
            _editVarMap.remove(clv);
        }

        // FIXGJB do the remove at top
        if (eVars != null)
        {
            _errorVars.remove(eVars);
        }
        marker = null;

        if (_fOptimizeAutomatically)
        {
            optimize(_objective);
            setExternalVariables();
        }

        return this;
    }

    // Re-initialize this solver from the original constraints, thus
    // getting rid of any accumulated numerical problems.  (Actually, we
    // haven't definitely observed any such problems yet)
    public final void reset()
            throws InternalError
    {
        throw new InternalError("reset not implemented");
    }

    // Re-solve the current collection of constraints for new values for
    // the constants of the edit variables.
    // DEPRECATED:  use suggestValue(...) then resolve()
    // If you must use this, be sure to not use it if you
    // remove an edit variable (or edit constraint) from the middle
    // of a list of edits and then try to resolve with this function
    // (you'll get the wrong answer, because the indices will be wrong
    // in the EditInfo objects)
    public final void resolve(Vector<Double> newEditConstants)
            throws InternalError
    {
        for (Variable v: _editVarMap.keySet())
        {
            EditInfo cei = _editVarMap.get(v);
            int i = cei.getIndex();
            try
            {
                if (i < newEditConstants.size())
                {
                    suggestValue(v, newEditConstants.get(i).doubleValue());
                }
            }
            catch (CassowaryError err)
            {
                throw new InternalError("Error during resolve");
            }
        }
        resolve();
    }

    // Convenience function for resolve-s of two variables
    public final void resolve(double x, double y)
            throws InternalError
    {
        _resolve_pair.set(0, x);
        _resolve_pair.set(1, y);
        resolve(_resolve_pair);
    }

    // Re-solve the cuurent collection of constraints, given the new
    // values for the edit variables that have already been
    // suggested (see suggestValue() method)
    public final void resolve()
            throws InternalError
    {
        dualOptimize();
        setExternalVariables();
        _infeasibleRows.clear();
        resetStayConstants();
    }

    // Suggest a new value for an edit variable
    // the variable needs to be added as an edit variable
    // and beginEdit() needs to be called before this is called.
    // The tableau will not be solved completely until
    // after resolve() has been called
    public final SimplexSolver suggestValue(Variable v, double x)
            throws CassowaryError
    {
        EditInfo cei = _editVarMap.get(v);
        if (cei == null)
        {
            System.err.println("suggestValue for variable " + v + ", but var is not an edit variable\n");
            throw new CassowaryError();
        }
        int i = cei.getIndex();
        AbstractVariable clvEditPlus = cei.getClvEditPlus();
        AbstractVariable clvEditMinus = cei.getClvEditMinus();
        double delta = x - cei.getPrevEditConstant();
        cei.setPrevEditConstant(x);
        deltaEditConstant(delta, clvEditPlus, clvEditMinus);
        return this;
    }

    // Control whether optimization and setting of external variables
    // is done automatically or not.  By default it is done
    // automatically and solve() never needs to be explicitly
    // called by client code; if setAutosolve is put to false,
    // then solve() needs to be invoked explicitly before using
    // variables' values
    // (Turning off autosolve while adding lots and lots of
    // constraints [ala the addDel test in ClTests] saved
    // about 20% in runtime, from 68sec to 54sec for 900 constraints,
    // with 126 failed adds)
    public final SimplexSolver setAutosolve(boolean f)
    {
        _fOptimizeAutomatically = f;
        return this;
    }

    // Tell whether we are autosolving
    public final boolean FIsAutosolving()
    {
        return _fOptimizeAutomatically;
    }

    // If autosolving has been turned off, client code needs
    // to explicitly call solve() before accessing variables
    // values
    public final SimplexSolver solve()
            throws InternalError
    {
        if (_fNeedsSolving)
        {
            optimize(_objective);
            setExternalVariables();
        }
        return this;
    }

    public SimplexSolver setEditedValue(Variable v, double n)
            throws InternalError
    {
        if (!FContainsVariable(v)) {
            v.changeValue(n);
            return this;
        }

        if (!Util.approx(n, v.getValue())) {
            addEditVar(v);
            beginEdit();
            try {
                suggestValue(v, n);
            }
            catch (CassowaryError e)
            {
                // just added it above, so we shouldn't get an error
                throw new InternalError("Error in setEditedValue");
            }
            endEdit();
        }
        return this;
    }

    public final boolean FContainsVariable(Variable v)
            throws InternalError
    {
        return columnsHasKey(v) || (rowExpression(v) != null);
    }

    public SimplexSolver addVar(Variable v)
            throws InternalError
    {
        if (!FContainsVariable(v))
        {
            try
            {
                addStay(v);
            }
            catch (RequiredFailure e)
            {
                // cannot have a required failure, since we add w/ weak
                throw new InternalError("Error in addVar -- required failure is impossible");
            }
        }
        return this;
    }

    // Originally from Michael Noth <noth@cs>
    public final String getInternalInfo()
    {
        String retstr = super.getInternalInfo() + "\nSolver info:\n" +
                "Stay Error Variables: " +
                (_stayPlusErrorVars.size() + _stayMinusErrorVars.size()) +
                " (" + _stayPlusErrorVars.size() + " +, " +
                _stayMinusErrorVars.size() + " -)\n" +
                "Edit Variables: " + _editVarMap.size() +
                "\n";
        return retstr;
    }

    public final String getDebugInfo()
    {
        String bstr = toString() + getInternalInfo() +
                "\n";
        return bstr;
    }

    public final String toString()
    {
        String bstr = super.toString() + "\n_stayPlusErrorVars: " +
                _stayPlusErrorVars +
                "\n_stayMinusErrorVars: " +
                _stayMinusErrorVars +
                "\n";

        return bstr;
    }

    public Hashtable<AbstractConstraint, AbstractVariable> getConstraintMap()
    {
        return _markerVars;
    }

  //// END PUBLIC INTERFACE

  // Add the constraint expr=0 to the inequality tableau using an
  // artificial variable.  To do this, create an artificial variable
  // av and add av=expr to the inequality tableau, then make av be 0.
  // (Raise an exception if we can't attain av=0.)
  protected final void addWithArtificialVariable(Expression expr)
       throws RequiredFailure, InternalError
    {
        SlackVariable av = new SlackVariable(++_artificialCounter,"a");
        ObjectiveVariable az = new ObjectiveVariable("az");
        Expression azRow = (Expression) expr.clone();

        addRow(az, azRow);
        addRow(av, expr);

        optimize(az);

        Expression azTableauRow = rowExpression(az);

        if (!Util.approx(azTableauRow.getConstant(), 0.0))
        {
            removeRow(az);
            removeColumn(av);
            throw new RequiredFailure();
        }

        // See if av is a basic variable
        final Expression e = rowExpression(av);

        if (e != null ) {
            // find another variable in this row and pivot,
            // so that av becomes parametric
            if (e.isConstant()) {
                // if there isn"t another variable in the row
                // then the tableau contains the equation av=0 --
                // just delete av"s row
                removeRow(av);
                removeRow(az);
                return;
            }
            AbstractVariable entryVar = e.anyPivotableVariable();
            pivot(entryVar, av);
        }
        assert rowExpression(av) == null;
        removeColumn(av);
        removeRow(az);
    }

    // We are trying to add the constraint expr=0 to the appropriate
    // tableau.  Try to add expr directly to the tableax without
    // creating an artificial variable.  Return true if successful and
    // false if not.
    protected final boolean tryAddingDirectly(Expression expr)
            throws RequiredFailure
    {
        final AbstractVariable subject = chooseSubject(expr);
        if (subject == null)
        {
            return false;
        }
        expr.newSubject(subject);
        if (columnsHasKey(subject))
        {
            substituteOut(subject, expr);
        }
        addRow(subject, expr);
        return true; // successfully added directly
    }

    // We are trying to add the constraint expr=0 to the tableaux.  Try
    // to choose a subject (a variable to become basic) from among the
    // current variables in expr.  If expr contains any unrestricted
    // variables, then we must choose an unrestricted variable as the
    // subject.  Also, if the subject is new to the solver we won't have
    // to do any substitutions, so we prefer new variables to ones that
    // are currently noted as parametric.  If expr contains only
    // restricted variables, if there is a restricted variable with a
    // negative coefficient that is new to the solver we can make that
    // the subject.  Otherwise we can't find a subject, so return nil.
    // (In this last case we have to add an artificial variable and use
    // that variable as the subject -- this is done outside this method
    // though.)
    //
    // Note: in checking for variables that are new to the solver, we
    // ignore whether a variable occurs in the objective function, since
    // new slack variables are added to the objective function by
    // 'newExpression:', which is called before this method.
    protected final AbstractVariable chooseSubject(Expression expr)
            throws RequiredFailure
    {
        AbstractVariable subject = null; // the current best subject, if any

        boolean foundUnrestricted = false;
        boolean foundNewRestricted = false;

        final Hashtable<AbstractVariable, Double> terms = expr.getTerms();

        for (AbstractVariable v: terms.keySet())
        {
            final double c = ((Double) terms.get(v)).doubleValue();

            if (foundUnrestricted)
            {
                if (!v.isRestricted())
                {
                    if (!columnsHasKey(v))
                    {
                        return v;
                    }
                }
            }
            else
            {
                // we haven't found an restricted variable yet
                if (v.isRestricted())
                {
                    if (!foundNewRestricted && !v.isDummy() && c < 0.0)
                    {
                        final Set<AbstractVariable> col = _columns.get(v);
                        if (col == null || (col.size() == 1 && columnsHasKey(_objective)))
                        {
                            subject = v;
                            foundNewRestricted = true;
                        }
                    }
                }
                else
                {
                    subject = v;
                    foundUnrestricted = true;
                }
            }
        }

        if (subject != null)
        {
            return subject;
        }

        double coeff = 0.0;

        for (AbstractVariable v: terms.keySet())
        {
            final double c = terms.get(v);
            if (!v.isDummy())
            {
                return null; // nope, no luck
            }
            if (!columnsHasKey(v))
            {
                subject = v;
                coeff = c;
            }
        }

        if (!Util.approx(expr.getConstant(), 0.0))
        {
            throw new RequiredFailure();
        }
        if (coeff > 0.0)
        {
            expr.multiplyMe(-1);
        }

        return subject;
    }

    // Each of the non-required edits will be represented by an equation
    // of the form
    //    v = c + eplus - eminus
    // where v is the variable with the edit, c is the previous edit
    // value, and eplus and eminus are slack variables that hold the
    // error in satisfying the edit constraint.  We are about to change
    // something, and we want to fix the constants in the equations
    // representing the edit constraints.  If one of eplus and eminus is
    // basic, the other must occur only in the expression for that basic
    // error variable.  (They can't both be basic.)  Fix the constant in
    // this expression.  Otherwise they are both nonbasic.  Find all of
    // the expressions in which they occur, and fix the constants in
    // those.  See the UIST paper for details.
    // (This comment was for resetEditConstants(), but that is now
    // gone since it was part of the screwey vector-based interface
    // to resolveing. --02/16/99 gjb)
    protected final void deltaEditConstant(double delta, AbstractVariable plusErrorVar, AbstractVariable minusErrorVar)
    {
        Expression exprPlus = rowExpression(plusErrorVar);
        if (exprPlus != null )
        {
            exprPlus.incrementConstant(delta);

            if (exprPlus.getConstant() < 0.0)
            {
                _infeasibleRows.add(plusErrorVar);
            }
            return;
        }

        Expression exprMinus = rowExpression(minusErrorVar);
        if (exprMinus != null)
        {
            exprMinus.incrementConstant(-delta);
            if (exprMinus.getConstant() < 0.0)
            {
                _infeasibleRows.add(minusErrorVar);
            }
            return;
        }

        for (AbstractVariable basicVar: _columns.get(minusErrorVar))
        {
            Expression expr = rowExpression(basicVar);
            //assert(expr != null, "expr != null" );
            final double c = expr.coefficientFor(minusErrorVar);
            expr.incrementConstant(c * delta);
            if (basicVar.isRestricted() && expr.getConstant() < 0.0)
            {
                _infeasibleRows.add(basicVar);
            }
        }
    }

    // We have set new values for the constants in the edit constraints.
    // Re-optimize using the dual simplex algorithm.
    protected final void dualOptimize()
            throws InternalError
    {
        final Expression zRow = rowExpression(_objective);

        while (_infeasibleRows.size() > 0)
        {
            Iterator<AbstractVariable> elements = _infeasibleRows.iterator();
            AbstractVariable exitVar = elements.next();
            _infeasibleRows.remove(exitVar);

            AbstractVariable entryVar = null;
            Expression expr = rowExpression(exitVar);
            if (expr != null )
            {
                if (expr.getConstant() < 0.0)
                {
                    double ratio = Double.MAX_VALUE;
                    double r;
                    Hashtable<AbstractVariable, Double> terms = expr.getTerms();
                    for (AbstractVariable v: terms.keySet())
                    {
                        double c = terms.get(v).doubleValue();
                        if (c > 0.0 && v.isPivotable())
                        {
                            double zc = zRow.coefficientFor(v);
                            r = zc / c; // FIXGJB r:= zc/c or zero, as SymbolicWeight-s
                            if (r < ratio)
                            {
                                entryVar = v;
                                ratio = r;
                            }
                        }
                    }
                    if (ratio == Double.MAX_VALUE)
                    {
                        throw new InternalError("ratio == nil (MAX_VALUE) in dualOptimize");
                    }
                    pivot(entryVar, exitVar);
                }
            }
        }
    }

    // Make a new linear expression representing the constraint cn,
    // replacing any basic variables with their defining expressions.
    // Normalize if necessary so that the constant is non-negative.  If
    // the constraint is non-required give its error variables an
    // appropriate weight in the objective function.
    @SuppressWarnings("unchecked")
    protected final Expression newExpression(AbstractConstraint cn, Vector eplus_eminus, Double prevEConstant)
    {
        final Expression cnExpr = cn.getExpression();
        Expression expr = new Expression(cnExpr.getConstant());
        SlackVariable slackVar = new SlackVariable();
        DummyVariable dummyVar = new DummyVariable();
        SlackVariable eminus = new SlackVariable();
        SlackVariable eplus = new SlackVariable();
        final Hashtable<AbstractVariable, Double> cnTerms = cnExpr.getTerms();
        for (AbstractVariable v: cnTerms.keySet())
        {
            double c = cnTerms.get(v);
            final Expression e = rowExpression(v);
            if (e == null)
            {
                expr.addVariable(v, c);
            }
            else
            {
                expr.addExpression(e, c);
            }
        }

        if (cn.isInequality())
        {
            ++_slackCounter;
            slackVar = new SlackVariable(_slackCounter, "s");
            expr.setVariable(slackVar,-1);
            _markerVars.put(cn, slackVar);
            if (!cn.isRequired())
            {
                ++_slackCounter;
                eminus = new SlackVariable(_slackCounter, "em");
                expr.setVariable(eminus, 1.0);
                Expression zRow = rowExpression(_objective);
                double swCoeff = cn.getStrength().getValue() * cn.getWeight();
                zRow.setVariable(eminus, swCoeff);
                insertErrorVar(cn, eminus);
                noteAddedVariable(eminus, _objective);
            }
        }
        else
        {
            // cn is an equality
            if (cn.isRequired())
            {
                ++_dummyCounter;
                dummyVar = new DummyVariable(_dummyCounter, "d");
                eplus_eminus.addElement(dummyVar);
                eplus_eminus.addElement(dummyVar);
                expr.setVariable(dummyVar, 1.0);
                _markerVars.put(cn, dummyVar);
            }
            else
            {
                ++_slackCounter;
                eplus = new SlackVariable(_slackCounter, "ep");
                eminus = new SlackVariable(_slackCounter, "em");

                expr.setVariable(eplus, -1.0);
                expr.setVariable(eminus, 1.0);
                _markerVars.put(cn, eplus);
                Expression zRow = rowExpression(_objective);
                double swCoeff = cn.getStrength().getValue() * cn.getWeight();
                zRow.setVariable(eplus, swCoeff);
                noteAddedVariable(eplus, _objective);
                zRow.setVariable(eminus, swCoeff);
                noteAddedVariable(eminus, _objective);
                insertErrorVar(cn, eminus);
                insertErrorVar(cn, eplus);
                if (cn.isStayConstraint())
                {
                    _stayPlusErrorVars.addElement(eplus);
                    _stayMinusErrorVars.addElement(eminus);
                }
                else if (cn.isEditConstraint())
                {
                    eplus_eminus.addElement(eplus);
                    eplus_eminus.addElement(eminus);
                    // FIXME: In the original Java implementation, this was a
                    // setValue call; since prevEConstant is passed in by reference,
                    // this means the value should be reflected outside this method.
                    // Does this actually matter?
                    prevEConstant = cnExpr.getConstant();
                }
            }
        }

        if (expr.getConstant() < 0)
        {
           expr.multiplyMe(-1);
        }

        return expr;
    }

    // Minimize the value of the objective.  (The tableau should already
    // be feasible.)
    protected final void optimize(ObjectiveVariable zVar)
            throws InternalError
    {
        Expression zRow = rowExpression(zVar);
        assert zRow != null;
        AbstractVariable entryVar = null;
        AbstractVariable exitVar = null;
        while (true)
        {
            double objectiveCoeff = 0;
            Hashtable<AbstractVariable, Double> terms = zRow.getTerms();
            for (AbstractVariable v: terms.keySet()) {
                double c = ((Double) terms.get(v)).doubleValue();
                if (v.isPivotable() && c < objectiveCoeff)
                {
                    objectiveCoeff = c;
                    entryVar = v;
                }
            }
            if (objectiveCoeff >= -_epsilon || entryVar == null)
            {
                return;
            }

            double minRatio = Double.MAX_VALUE;
            Set<AbstractVariable> columnVars = _columns.get(entryVar);
            double r = 0.0;
            for (AbstractVariable v: columnVars) {
                if (v.isPivotable())
                {
                    final Expression expr = rowExpression(v);
                    double coeff = expr.coefficientFor(entryVar);
                    if (coeff < 0.0)
                    {
                        r = - expr.getConstant() / coeff;
                        if (r < minRatio)
                        {
                            minRatio = r;
                            exitVar = v;
                        }
                    }
                }
            }
            if (minRatio == Double.MAX_VALUE)
            {
                throw new InternalError("Objective function is unbounded in optimize");
            }
            pivot(entryVar, exitVar);
        }
    }

    // Do a pivot.  Move entryVar into the basis (i.e. make it a basic variable),
    // and move exitVar out of the basis (i.e., make it a parametric variable)
    protected final void pivot(AbstractVariable entryVar, AbstractVariable exitVar)
            throws InternalError
    {
        // the entryVar might be non-pivotable if we're doing a removeConstraint --
        // otherwise it should be a pivotable variable -- enforced at call sites,
        // hopefully

        Expression pexpr = removeRow(exitVar);

        pexpr.changeSubject(exitVar, entryVar);
        substituteOut(entryVar, pexpr);
        addRow(entryVar, pexpr);
    }

    // Each of the non-required stays will be represented by an equation
    // of the form
    //     v = c + eplus - eminus
    // where v is the variable with the stay, c is the previous value of
    // v, and eplus and eminus are slack variables that hold the error
    // in satisfying the stay constraint.  We are about to change
    // something, and we want to fix the constants in the equations
    // representing the stays.  If both eplus and eminus are nonbasic
    // they have value 0 in the current solution, meaning the previous
    // stay was exactly satisfied.  In this case nothing needs to be
    // changed.  Otherwise one of them is basic, and the other must
    // occur only in the expression for that basic error variable.
    // Reset the constant in this expression to 0.
    protected final void resetStayConstants()
    {
        for (int i = 0; i < _stayPlusErrorVars.size(); i++) {
            Expression expr = rowExpression(_stayPlusErrorVars.get(i) );
            if (expr == null)
            {
                expr = rowExpression(_stayMinusErrorVars.get(i));
            }
            if (expr != null)
            {
                expr.setConstant(0.0);
            }
        }
    }

    // Set the external variables known to this solver to their appropriate values.
    // Set each external basic variable to its value, and set each
    // external parametric variable to 0.  (It isn't clear that we will
    // ever have external parametric variables -- every external
    // variable should either have a stay on it, or have an equation
    // that defines it in terms of other external variables that do have
    // stays.  For the moment I'll put this in though.)  Variables that
    // are internal to the solver don't actually store values -- their
    // values are just implicit in the tableu -- so we don't need to set
    // them.
    protected final void setExternalVariables()
    {
        for (Variable v: _externalParametricVars)
        {
            if (rowExpression(v) != null)
            {
                System.err.println("Error: variable" + v + " in _externalParametricVars is basic");
                continue;
            }
            v.changeValue(0.0);
        }

        for (AbstractVariable av: _externalRows)
        {
            Variable v = (Variable) av;
            Expression expr = rowExpression(v);
            v.changeValue(expr.getConstant());
        }

        _fNeedsSolving = false;
    }

    // Protected convenience function to insert an error variable into
    // the _errorVars set, creating the mapping with put as necessary
    @SuppressWarnings("SuspiciousMethodCalls,unchecked")
    protected final void insertErrorVar(AbstractConstraint cn, AbstractVariable var)
    {
        Set cnset = (Set) _errorVars.get(var);
        if (cnset == null)
        {
            cnset = new HashSet<>();
            _errorVars.put(cn, cnset);
            cnset.add(var);
        }
    }
}
