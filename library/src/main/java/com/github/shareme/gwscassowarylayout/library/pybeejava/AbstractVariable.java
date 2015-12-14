package com.github.shareme.gwscassowarylayout.library.pybeejava;

import java.lang.*;

@SuppressWarnings("unused")
public abstract class AbstractVariable
{
    private static int iVariableNumber;

    private String _name;

    public AbstractVariable(String name)
    {
        _name = name;
        iVariableNumber++;
    }

    public AbstractVariable()
    {
        _name = "v" + iVariableNumber;
        iVariableNumber++;
    }

    public AbstractVariable(long varnumber, String prefix)
    {
        //hash_code = iVariableNumber;
        _name = prefix + varnumber;
        iVariableNumber++;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public boolean isDummy()
    {
        return false;
    }

    public abstract boolean isExternal();

    public abstract boolean isPivotable();

    public abstract boolean isRestricted();

    public abstract String toString();

    public static int numCreated()
    {
        return iVariableNumber;
    }

}
