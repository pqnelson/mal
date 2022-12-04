package com.github.pqnelson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.PushbackReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.pqnelson.ReaderMacro;

/**
 * A read-table driven Lisp reader.
 *
 * <p>This is an attempt to cleanup the Scanner/Reader mess.</p>
 */
public class ReadTable {
    private Map<Integer, ReaderMacro<ReadTable>> table;
    private final PushbackReader input;
    private int line = 1;
    private boolean finished = false;

    private final ReaderMacro<ReadTable> newlineReader = (_stream, _reader) -> {
        line++;
        return null;
    };

    public ReadTable(final String snippet) {
        this(new StringReader((null == snippet ? "" : snippet)));
    }

    public ReadTable(final InputStream stream) {
        this(new InputStreamReader(stream));
    }

    public ReadTable(final Reader reader) {
        this.table = new HashMap<Integer, ReaderMacro<ReadTable>>();
        this.input = new PushbackReader(new BufferedReader(reader));

        this.table.put((int) '(', delimitedReaderFactory(")"));
        this.table.put((int) ')', singleCharReader(right_paren));
        this.table.put((int) '\n', newlineReader);
    }

    private ReaderMacro<ReadTable> delimitedReaderFactory(final String delimiter) {
        final ReaderMacro<ReadTable> result = (_stream, _reader) -> {
            ArrayList<Object> coll = new ArrayList<Object>();
            Object token;
            while (!(token = _reader.read()).equals(delimiter)) {
                coll.add(token);
            }
            return coll;
        };
        return result;
    }

    private ReaderMacro<ReadTable> singleCharReader(final int c) {
        StringBuffer buf = new StringBuffer();
        buf.appendCodePoint(c);
        final String token = buf.toString();
        final ReaderMacro<ReadTable> result = (_stream, _reader) -> token;
        return result;
    }

    private int next() {
        try {
            return input.read();
        } catch (IOException e) {
            return -1;
        }
    }

    private void unread(int c) {
        try {
            input.unread(c);
        } catch (IOException e) {
        }
    }

    public boolean isAtEnd() {
        if (!finished) {
            int c = next();
            if (-1 == c) {
                finished = true;
            } else {
                unread(c);
            }
        }
        return finished;
    }

    public Object read() {
        Object result = null;
        while(true) {
            if (isAtEnd()) {
                return result;
            }
            int c = next();
            if (table.containsKey(c)) {
                if ((result = table.get(c).read(this.input, this)) != null) {
                    return result;
                }
            } else if (Character.isWhitespace(c)) {
                // skip whitespace
            } else {
                return readToken(c);
            }
        }
    }

    public Object readToken(final int cp) {
        StringBuffer buf = new StringBuffer();
        buf.appendCodePoint(cp);
        while(!isAtEnd()) {
            int c = next();
            if (this.table.containsKey(c)) {
                unread(c);
                return buf.toString();
            } else if (Character.isWhitespace(c)) {
                return buf.toString();
            } else {
                buf.appendCodePoint(c);
            }
        }
        return buf.toString();
    }
}