package com.github.shareme.gwscassowarylayout.library.pybeejava;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

@SuppressWarnings("unused")
class Tableau
{
    // _columns is a mapping from variables which occur in expressions to the
    // set of basic variables whose expressions contain them
    // i.e., it's a mapping from variables in expressions (a column) to the
    // set of rows that contain them
    protected Hashtable<AbstractVariable, Set<AbstractVariable>> _columns;

    // _rows maps basic variables to the expressions for that row in the tableau
    protected Hashtable<AbstractVariable, Expression> _rows;

    // the collection of basic variables that have infeasible rows
    // (used when reoptimizing)
    protected Set<AbstractVariable> _infeasibleRows;

    // the set of rows where the basic variable is external
    // this was added to the Java/C++ versions to reduce time in setExternalVariables()
    protected Set<Variable> _externalRows;

    // the set of external variables which are parametric
    // this was added to the Java/C++ versions to reduce time in setExternalVariables()
    protected Set<Variable> _externalParametricVars;

    // ctr is protected, since this only supports an ADT for
    // the SimplexSolver class
    protected Tableau()
    {
        _columns = new Hashtable<>();
        _rows = new Hashtable<>();
        _infeasibleRows = new HashSet<>();
        _externalRows = new HashSet<>();
        _externalParametricVars = new HashSet<>();
    }

    // Variable v has been removed from an expression.  If the
    // expression is in a tableau the corresponding basic variable is
    // subject (or if subject is nil then it's in the objective function).
    // Update the column cross-indices.
    public final void noteRemovedVariable(AbstractVariable v, AbstractVariable subject)
    {
        if (subject != null)
        {
            _columns.get(v).remove(subject);
        }
    }

    // v has been added to the linear expression for subject
    // update column cross indices
    public final void noteAddedVariable(AbstractVariable v, AbstractVariable subject)
    {
        if (subject != null)
        {
            insertColVar(v, subject);
        }
    }

    // Originally from Michael Noth <noth@cs>
    public String getInternalInfo() {
        String retstr = "Tableau Information:\n" + "Rows: " + _rows.size() +
                " (= " + (_rows.size() - 1) + " constraints)" +
                "\nColumns: " + _columns.size() +
                "\nInfeasible Rows: " + _infeasibleRows.size() +
                "\nExternal basic variables: " + _externalRows.size() +
                "\nExternal parametric variables: " +
                _externalParametricVars.size() +
                "\n";

        return retstr;
    }

    public String toString()
    {
        StringBuilder bstr = new StringBuilder("Tableau:\n");
        for (AbstractVariable clv: _rows.keySet()) {
            Expression expr = _rows.get(clv);
            bstr.append(clv.toString());
            bstr.append(" <==> ");
            bstr.append(expr.toString());
            bstr.append("\n");
        }

        bstr.append("\nColumns:\n");
        bstr.append(_columns.toString());

        bstr.append("\nInfeasible rows: ");
        bstr.append(_infeasibleRows.toString());

        bstr.append("External basic variables: ");
        bstr.append(_externalRows.toString());

        bstr.append("External parametric variables: ");
        bstr.append(_externalParametricVars.toString());

        return bstr.toString();
    }

    // Convenience function to insert a variable into
    // the set of rows stored at _columns[param_var],
    // creating a new set if needed
    @SuppressWarnings("unchecked")
    private void insertColVar(AbstractVariable param_var, AbstractVariable rowvar)
    {
        Set rowset = _columns.get(param_var);
        if (rowset == null)
        {
            rowset = new HashSet<>();
            _columns.put(param_var, rowset);
        }
        rowset.add(rowvar);
    }

    // Add v=expr to the tableau, update column cross indices
    // v becomes a basic variable
    // expr is now owned by Tableau class,
    // and Tableauis responsible for deleting it
    // (also, expr better be allocated on the heap!)
    protected final void addRow(AbstractVariable var, Expression expr)
    {
        // for each variable in expr, add var to the set of rows which
        // have that variable in their expression
        _rows.put(var, expr);

        for (AbstractVariable clv: expr.getTerms().keySet())
        {
            insertColVar(clv, var);
            if (clv.isExternal())
            {
                _externalParametricVars.add((Variable) clv);
            }
        }
        if (var.isExternal())
        {
            _externalRows.add((Variable) var);
        }
    }

    // Remove v from the tableau -- remove the column cross indices for v
    // and remove v from every expression in rows in which v occurs
    @SuppressWarnings("SuspiciousMethodCalls")
    protected final void removeColumn(AbstractVariable var)
    {
        // remove the rows with the variables in varset
        Set<AbstractVariable> rows = _columns.remove(var);

        if (rows != null) {
            for (AbstractVariable clv: rows)
            {
                Expression expr = _rows.get(clv);
                expr.getTerms().remove(var);
            }
        }

        if (var.isExternal())
        {
            _externalRows.remove(var);
            _externalParametricVars.remove(var);
        }
    }

    // Remove the basic variable v from the tableau row v=expr
    // Then update column cross indices
    @SuppressWarnings("SuspiciousMethodCalls")
    protected final Expression removeRow(AbstractVariable var)
           throws InternalError
    {
        Expression expr = _rows.get(var);
        assert expr != null;

        // For each variable in this expression, update
        // the column mapping and remove the variable from the list
        // of rows it is known to be in
        for (AbstractVariable clv: expr.getTerms().keySet()) {
            Set varset = (Set) _columns.get(clv);
            if (varset != null)
            {
                varset.remove(var);
            }
        }

        _infeasibleRows.remove(var);

        if (var.isExternal())
        {
            _externalRows.remove(var);
        }
        _rows.remove(var);
        return expr;
    }

    // Replace all occurrences of oldVar with expr, and update column cross indices
    // oldVar should now be a basic variable
    protected final void substituteOut(AbstractVariable oldVar, Expression expr)
    {
        for (AbstractVariable v: _columns.get(oldVar))
        {
            Expression row = _rows.get(v);
            row.substituteOut(oldVar, expr, v, this);
            if (v.isRestricted() && row.getConstant() < 0.0)
            {
                _infeasibleRows.add(v);
            }
        }

        if (oldVar.isExternal())
        {
            _externalRows.add((Variable) oldVar);
            _externalParametricVars.remove(oldVar);
        }
        _columns.remove(oldVar);
    }

    protected final Hashtable<AbstractVariable, Set<AbstractVariable>> columns()
    {
        return _columns;
    }

    protected final Hashtable<AbstractVariable, Expression> rows()
    {
        return _rows;
    }

    // return true iff the variable subject is in the columns keys
    protected final boolean columnsHasKey(AbstractVariable subject)
    {
        return _columns.containsKey(subject);
    }

    protected final Expression rowExpression(AbstractVariable v)
    {
        return _rows.get(v);
    }
}
