package com.github.pqnelson.js;

class JsString extends Expr implements PrimitiveValue {
    private String contents;
    private boolean isDoubleQuoted;

    public JsString(String value) {
        this.contents = value;
        this.isDoubleQuoted = true;
    }
    public JsString(String value, boolean isDoubleQuoted) {
        this.contents = value;
        this.isDoubleQuoted = isDoubleQuoted;
    }
    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitString(this);
    }

    // TODO: escape double quoted contents
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(this.isDoubleQuoted ? "\"" : "'");
        buf.append(this.contents);
        buf.append(this.isDoubleQuoted ? "\"" : "'");
        return buf.toString();
    }

    @Override
    public boolean isFalsy() {
        return this.contents.isEmpty();
    }
}