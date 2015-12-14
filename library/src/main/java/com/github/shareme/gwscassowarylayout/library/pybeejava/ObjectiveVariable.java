package com.github.shareme.gwscassowarylayout.library.pybeejava;

@SuppressWarnings("unused")
class ObjectiveVariable extends AbstractVariable
{
    public ObjectiveVariable(String name)
    {
        super(name);
    }

    public ObjectiveVariable(long number, String prefix)
    {
        super(number,prefix);
    }

    public String toString()
    {
        return "[" + getName() + ":obj]";
    }

    public boolean isExternal()
    {
        return false;
    }

    public boolean isPivotable()
    {
        return false;
    }

    public boolean isRestricted()
    {
        return false;
    }
}
