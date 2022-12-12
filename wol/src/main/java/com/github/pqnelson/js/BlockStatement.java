package com.github.pqnelson.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BlockStatement extends Statement implements Iterable<Statement> {
    private List<Statement> statements;

    public BlockStatement() {
        this(new ArrayList<>());
    }
    public BlockStatement(Statement... contents) {
        this(Arrays.asList(contents));
    }
    public BlockStatement(List<Statement> contents) {
        this.statements = new ArrayList<>(contents);
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitBlock(this);
    }

    public void append(Statement s) {
        this.statements.add(s);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("{\n");
        for (Statement s : this.statements) {
            buf.append(s.toString());
        }
        buf.append("\n}");
        return buf.toString();
    }

    @Override
    public Iterator<Statement> iterator() {
        return this.statements.iterator();
    }
}
