package com.github.shareme.gwscassowarylayout.library.pybeejava;


public class ConstraintNotFound extends CassowaryError
{
    public String description()
    {
        return "Tried to remove a constraint never added to the tableu.";
    }
}
