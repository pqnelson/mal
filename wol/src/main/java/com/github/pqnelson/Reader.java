package com.github.pqnelson;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ListIterator;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.annotations.VisibleForTesting;
import static com.github.pqnelson.TokenType.*;

/**
 * Lisp reader, assembling an abstract syntax tree.
 *
 * As per tradition, we refer to the parser as the "reader".
 */
class Reader {
    private final List<Token> tokens;
    private Token currentToken = null;
    private ListIterator<Token> tokenIterator;

    // PRECONDITION: tokens is a non-empty list
    Reader(List<Token> tokens) {
        assert (!tokens.isEmpty());
        this.tokens = tokens;
        this.tokenIterator = this.tokens.listIterator();
    }

    public static final Expr readString(String str) {
        Scanner scanner = new Scanner(str);
        Reader reader = new Reader(scanner.scanTokens());
        Expr expr = reader.readForm();
        return expr;
    }

    // Populate {@code currentToken} if needed, and return it.
    Token peek() {
        if (null == currentToken && tokenIterator.hasNext()) {
            currentToken = tokenIterator.next();
        }
        return currentToken;
    }

    public boolean isAtEnd() {
        return EOF == peek().type;
    }

    Token next() {
        if (isAtEnd()) return peek();

        Token result = null;
        if (null != currentToken) {
            result = currentToken;
            currentToken = null;
        } else if (null == currentToken && tokenIterator.hasNext()) {
            result = tokenIterator.next();
        }
        // If we're at the end, we should remember it.
        if (EOF == result.type) {
            currentToken = result;
        }
        return result;
    }

    Expr readForm() {
        Token token = this.peek();
        if (isAtEnd()) return null;

        switch(token.type) {
        case QUOTE:
        case BACKTICK:
        case UNQUOTE:
        case SPLICE:
        case WITH_META: {
            next();
            Seq result = new Seq();
            result.conj(new Symbol(token));
            result.conj(readForm());
            return result;
        }
        // lists
        case RIGHT_PAREN: throw new InputMismatchException("Unexpected ')'");
        case LEFT_PAREN: return this.readList();

        // vector
        case RIGHT_BRACKET: throw new InputMismatchException("Unexpected ']'");
        case LEFT_BRACKET:
            next(); return this.readVector(token);

        // hash maps
        case RIGHT_BRACE: throw new InputMismatchException("Unexpected '}'");
        case LEFT_BRACE:
            next(); return this.readMap(token);

        default:
            return readAtom();
        }
    }

    private String expectedDelimiter(TokenType rightDelimiter) {
        switch(rightDelimiter) {
        case RIGHT_PAREN: return ")";
        case RIGHT_BRACE: return "}";
        case RIGHT_BRACKET: return "]";
        default: return rightDelimiter.toString();
        }
    }

    ArrayList<Expr> gatherContents(Token start, TokenType leftDelimiter, TokenType rightDelimiter) {
        assert (leftDelimiter == start.type) : "left delimiter didn't match start token";
        ArrayList<Expr> ast = new ArrayList<>();
        Token token = peek();
        while (rightDelimiter != token.type) {
            if (EOF == token.type) {
                String expected = expectedDelimiter(rightDelimiter);
                throw new InputMismatchException("expected '"+expected+"' found '"+token.lexeme+"'");
            }
            ast.add(readForm());
            token = peek();
        }
        assert (!isAtEnd()) : "Somehow I'm at the end, skipping the right delimiter";
        assert (rightDelimiter == peek().type) : "Right delimiter mismatch?";
        next(); // consume the tasty right delimiter, yum yum
        return ast;
    }

    Seq readSeq(Token start) {
        ArrayList<Expr> ast = gatherContents(start, LEFT_PAREN, RIGHT_PAREN);
        return new Seq(ast);
    }


    Expr readVector(Token token) {
        return new Vector(gatherContents(token, LEFT_BRACKET, RIGHT_BRACKET));
    }

    Map readMap(Token token) {
        ArrayList<Expr> contents = gatherContents(token, LEFT_BRACE, RIGHT_BRACE);
        if (contents.size() % 2 != 0) {
            throw new InputMismatchException("HashMap bindings must be even");
        }
        Map m = new Map();
        for (int i = 0; i < contents.size(); i += 2) {
            m.assoc(contents.get(i), contents.get(i+1));
        }
        return m;
    }

    Expr readList() {
        Token token = next();
        assert (LEFT_PAREN == token.type) : ("readList expected a left parentheses but found "+token.toString());
        return readSeq(token);
    }

    Expr number(Token token, Object value) {
        if (Long.class.isInstance(value)) {
            return new Int(token, (long)value);
        } else if (BigInteger.class.isInstance(value)) {
            return new BigInt(token, (BigInteger)value);
        } else if (Double.class.isInstance(value)) {
            return new Float(token, (double)value);
        } else {
            throw new Error("Unexpected numeric value "+value.toString()+" "+value.getClass().toString());
        }
    }

    Expr readAtom() {
        final Token token = this.next();
        switch(token.type) {
        case KEYWORD:
            return new Keyword(token);
        case NUMBER:
            return number(token, token.literal);
        case STRING:
            return new Str(token);
        case NIL:
        case TRUE:
        case FALSE:
            return new Literal(token);
        default:
            return new Symbol(token);
        }
    }
}