package com.github.pqnelson;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ListIterator;

import com.github.pqnelson.Expr;
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

    boolean isAtEnd() {
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
            next();
            return new Expr.Pair(token, readForm());
        // lists
        case RIGHT_PAREN: throw new InputMismatchException("Unexpected ')'");
        case LEFT_PAREN: return this.readList();

        // vector
        case RIGHT_BRACKET: throw new InputMismatchException("Unexpected ']'");
        case LEFT_BRACKET:
            next(); return this.readVector(token);

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

    Expr.Seq readSeq(Token start) {
        ArrayList<Expr> ast = gatherContents(start, LEFT_PAREN, RIGHT_PAREN);
        return new Expr.Seq(ast);
    }


    Expr readVector(Token token) {
        return new Expr.Vector(gatherContents(token, LEFT_BRACKET, RIGHT_BRACKET));
    }

    Expr readList() {
        Token token = next();
        assert (LEFT_PAREN == token.type) : ("readList expected a left parentheses but found "+token.toString());
        Token rator = peek();
        return readSeq(token);
    }

    Expr readAtom() {
        final Token token = this.next();
        switch(token.type) {
        case KEYWORD:
            return new Expr.Keyword(token);
        case NUMBER:
        case STRING:
            return new Expr.Literal(token);
        case NIL:
            return new Expr.Literal(token, null);
        case TRUE:
            return new Expr.Literal(token, true);
        case FALSE:
            return new Expr.Literal(token, false);
        default:
            return new Expr.Symbol(token);
        }
    }
}