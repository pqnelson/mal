package com.github.pqnelson.expr;

public class Char extends Literal {
    Char(char c) {
        super(c);
    }
    Char(Character c) {
        super(c);
    }
    
    @Override
    public Character value() {
        return (Character) super.value();
    }
    
    public char charValue() {
        return this.value().charValue();
    }
    
    @Override
    public String toString() {
        return Character.toString(this.value());
    }
    
    @Override
    public String type() {
        return "Char";
    }
}
