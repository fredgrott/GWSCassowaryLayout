package com.github.shareme.gwscassowarylayout.library.pybeejava;


public enum Strength
{
    REQUIRED (1001001000),
    STRONG (1000000),
    MEDIUM (1000),
    WEAK (1);

    private int _value;

    Strength(int value) {
        this._value = value;
    }

    public int getValue() {
        return _value;
    }

    public String toString(Strength strength) {
        switch (strength) {
            case REQUIRED:
                return "Required";
            case STRONG:
                return "Strong";
            case MEDIUM:
                return "Medium";
            case WEAK:
                return "Weak";
            default:
                return "Unknown (" + _value + ")";
        }
    }
}
