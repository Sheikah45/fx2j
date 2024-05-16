// Generated from C:/Users/corey/FAFProjects/fx2j/fx2j-parser/src/main/antlr4/io/github/sheikah45/fx2j/parser/expression/BindExpression.g4 by ANTLR 4.13.1

package io.github.sheikah45.fx2j.parser.antlr;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class BindExpressionLexer extends Lexer {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T__22 = 23, String = 24,
            Decimal = 25, Fractional = 26, Identifier = 27, Whitespace = 28;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\u0004\u0000\u001c\u00b8\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002" +
            "\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002" +
            "\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002" +
            "\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002" +
            "\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e" +
            "\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011" +
            "\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014" +
            "\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017" +
            "\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a" +
            "\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d" +
            "\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002" +
            "\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005" +
            "\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001" +
            "\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001" +
            "\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001" +
            "\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001" +
            "\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001" +
            "\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001" +
            "\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001" +
            "\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001" +
            "\u0017\u0003\u0017~\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003" +
            "\u0017\u0083\b\u0017\u0001\u0017\u0003\u0017\u0086\b\u0017\u0001\u0018" +
            "\u0004\u0018\u0089\b\u0018\u000b\u0018\f\u0018\u008a\u0001\u0019\u0001" +
            "\u0019\u0001\u0019\u0003\u0019\u0090\b\u0019\u0001\u0019\u0003\u0019\u0093" +
            "\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0098\b\u0019" +
            "\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u009e\b\u0019" +
            "\u0001\u001a\u0001\u001a\u0005\u001a\u00a2\b\u001a\n\u001a\f\u001a\u00a5" +
            "\t\u001a\u0001\u001b\u0004\u001b\u00a8\b\u001b\u000b\u001b\f\u001b\u00a9" +
            "\u0001\u001c\u0001\u001c\u0003\u001c\u00ae\b\u001c\u0001\u001c\u0001\u001c" +
            "\u0001\u001d\u0004\u001d\u00b3\b\u001d\u000b\u001d\f\u001d\u00b4\u0001" +
            "\u001d\u0001\u001d\u0000\u0000\u001e\u0001\u0001\u0003\u0002\u0005\u0003" +
            "\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015" +
            "\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012" +
            "%\u0013\'\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a5\u001b7\u0000" +
            "9\u0000;\u001c\u0001\u0000\u0007\u0001\u000009\u0002\u0000AZaz\u0003\u0000" +
            "09AZaz\u0003\u0000\"\"\'\'\\\\\u0002\u0000EEee\u0002\u0000++--\u0003\u0000" +
            "\t\n\r\r  \u00c3\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001" +
            "\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001" +
            "\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000" +
            "\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000" +
            "\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000" +
            "\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000" +
            "\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000" +
            "\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000" +
            "\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000" +
            "%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001" +
            "\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001\u0000\u0000" +
            "\u0000\u0000/\u0001\u0000\u0000\u0000\u00001\u0001\u0000\u0000\u0000\u0000" +
            "3\u0001\u0000\u0000\u0000\u00005\u0001\u0000\u0000\u0000\u0000;\u0001" +
            "\u0000\u0000\u0000\u0001=\u0001\u0000\u0000\u0000\u0003?\u0001\u0000\u0000" +
            "\u0000\u0005A\u0001\u0000\u0000\u0000\u0007C\u0001\u0000\u0000\u0000\t" +
            "E\u0001\u0000\u0000\u0000\u000bG\u0001\u0000\u0000\u0000\rI\u0001\u0000" +
            "\u0000\u0000\u000fK\u0001\u0000\u0000\u0000\u0011M\u0001\u0000\u0000\u0000" +
            "\u0013O\u0001\u0000\u0000\u0000\u0015Q\u0001\u0000\u0000\u0000\u0017S" +
            "\u0001\u0000\u0000\u0000\u0019U\u0001\u0000\u0000\u0000\u001bW\u0001\u0000" +
            "\u0000\u0000\u001dZ\u0001\u0000\u0000\u0000\u001f\\\u0001\u0000\u0000" +
            "\u0000!_\u0001\u0000\u0000\u0000#b\u0001\u0000\u0000\u0000%e\u0001\u0000" +
            "\u0000\u0000\'h\u0001\u0000\u0000\u0000)k\u0001\u0000\u0000\u0000+p\u0001" +
            "\u0000\u0000\u0000-u\u0001\u0000\u0000\u0000/\u0085\u0001\u0000\u0000" +
            "\u00001\u0088\u0001\u0000\u0000\u00003\u009d\u0001\u0000\u0000\u00005" +
            "\u009f\u0001\u0000\u0000\u00007\u00a7\u0001\u0000\u0000\u00009\u00ab\u0001" +
            "\u0000\u0000\u0000;\u00b2\u0001\u0000\u0000\u0000=>\u0005.\u0000\u0000" +
            ">\u0002\u0001\u0000\u0000\u0000?@\u0005(\u0000\u0000@\u0004\u0001\u0000" +
            "\u0000\u0000AB\u0005,\u0000\u0000B\u0006\u0001\u0000\u0000\u0000CD\u0005" +
            ")\u0000\u0000D\b\u0001\u0000\u0000\u0000EF\u0005[\u0000\u0000F\n\u0001" +
            "\u0000\u0000\u0000GH\u0005]\u0000\u0000H\f\u0001\u0000\u0000\u0000IJ\u0005" +
            "!\u0000\u0000J\u000e\u0001\u0000\u0000\u0000KL\u0005-\u0000\u0000L\u0010" +
            "\u0001\u0000\u0000\u0000MN\u0005*\u0000\u0000N\u0012\u0001\u0000\u0000" +
            "\u0000OP\u0005/\u0000\u0000P\u0014\u0001\u0000\u0000\u0000QR\u0005%\u0000" +
            "\u0000R\u0016\u0001\u0000\u0000\u0000ST\u0005+\u0000\u0000T\u0018\u0001" +
            "\u0000\u0000\u0000UV\u0005>\u0000\u0000V\u001a\u0001\u0000\u0000\u0000" +
            "WX\u0005>\u0000\u0000XY\u0005=\u0000\u0000Y\u001c\u0001\u0000\u0000\u0000" +
            "Z[\u0005<\u0000\u0000[\u001e\u0001\u0000\u0000\u0000\\]\u0005<\u0000\u0000" +
            "]^\u0005=\u0000\u0000^ \u0001\u0000\u0000\u0000_`\u0005=\u0000\u0000`" +
            "a\u0005=\u0000\u0000a\"\u0001\u0000\u0000\u0000bc\u0005!\u0000\u0000c" +
            "d\u0005=\u0000\u0000d$\u0001\u0000\u0000\u0000ef\u0005&\u0000\u0000fg" +
            "\u0005&\u0000\u0000g&\u0001\u0000\u0000\u0000hi\u0005|\u0000\u0000ij\u0005" +
            "|\u0000\u0000j(\u0001\u0000\u0000\u0000kl\u0005n\u0000\u0000lm\u0005u" +
            "\u0000\u0000mn\u0005l\u0000\u0000no\u0005l\u0000\u0000o*\u0001\u0000\u0000" +
            "\u0000pq\u0005t\u0000\u0000qr\u0005r\u0000\u0000rs\u0005u\u0000\u0000" +
            "st\u0005e\u0000\u0000t,\u0001\u0000\u0000\u0000uv\u0005f\u0000\u0000v" +
            "w\u0005a\u0000\u0000wx\u0005l\u0000\u0000xy\u0005s\u0000\u0000yz\u0005" +
            "e\u0000\u0000z.\u0001\u0000\u0000\u0000{}\u0005\"\u0000\u0000|~\u0003" +
            "7\u001b\u0000}|\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000~\u007f" +
            "\u0001\u0000\u0000\u0000\u007f\u0086\u0005\"\u0000\u0000\u0080\u0082\u0005" +
            "\'\u0000\u0000\u0081\u0083\u00037\u001b\u0000\u0082\u0081\u0001\u0000" +
            "\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0084\u0001\u0000" +
            "\u0000\u0000\u0084\u0086\u0005\'\u0000\u0000\u0085{\u0001\u0000\u0000" +
            "\u0000\u0085\u0080\u0001\u0000\u0000\u0000\u00860\u0001\u0000\u0000\u0000" +
            "\u0087\u0089\u0007\u0000\u0000\u0000\u0088\u0087\u0001\u0000\u0000\u0000" +
            "\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000" +
            "\u008a\u008b\u0001\u0000\u0000\u0000\u008b2\u0001\u0000\u0000\u0000\u008c" +
            "\u008d\u00031\u0018\u0000\u008d\u008f\u0005.\u0000\u0000\u008e\u0090\u0003" +
            "1\u0018\u0000\u008f\u008e\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000" +
            "\u0000\u0000\u0090\u0092\u0001\u0000\u0000\u0000\u0091\u0093\u00039\u001c" +
            "\u0000\u0092\u0091\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000" +
            "\u0000\u0093\u009e\u0001\u0000\u0000\u0000\u0094\u0095\u0005.\u0000\u0000" +
            "\u0095\u0097\u00031\u0018\u0000\u0096\u0098\u00039\u001c\u0000\u0097\u0096" +
            "\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u0098\u009e" +
            "\u0001\u0000\u0000\u0000\u0099\u009a\u00031\u0018\u0000\u009a\u009b\u0003" +
            "9\u001c\u0000\u009b\u009e\u0001\u0000\u0000\u0000\u009c\u009e\u00031\u0018" +
            "\u0000\u009d\u008c\u0001\u0000\u0000\u0000\u009d\u0094\u0001\u0000\u0000" +
            "\u0000\u009d\u0099\u0001\u0000\u0000\u0000\u009d\u009c\u0001\u0000\u0000" +
            "\u0000\u009e4\u0001\u0000\u0000\u0000\u009f\u00a3\u0007\u0001\u0000\u0000" +
            "\u00a0\u00a2\u0007\u0002\u0000\u0000\u00a1\u00a0\u0001\u0000\u0000\u0000" +
            "\u00a2\u00a5\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000" +
            "\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a46\u0001\u0000\u0000\u0000\u00a5" +
            "\u00a3\u0001\u0000\u0000\u0000\u00a6\u00a8\b\u0003\u0000\u0000\u00a7\u00a6" +
            "\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u00a7" +
            "\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000\u00aa8\u0001" +
            "\u0000\u0000\u0000\u00ab\u00ad\u0007\u0004\u0000\u0000\u00ac\u00ae\u0007" +
            "\u0005\u0000\u0000\u00ad\u00ac\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001" +
            "\u0000\u0000\u0000\u00ae\u00af\u0001\u0000\u0000\u0000\u00af\u00b0\u0003" +
            "1\u0018\u0000\u00b0:\u0001\u0000\u0000\u0000\u00b1\u00b3\u0007\u0006\u0000" +
            "\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000" +
            "\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000" +
            "\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6\u00b7\u0006\u001d\u0000" +
            "\u0000\u00b7<\u0001\u0000\u0000\u0000\r\u0000}\u0082\u0085\u008a\u008f" +
            "\u0092\u0097\u009d\u00a3\u00a9\u00ad\u00b4\u0001\u0006\u0000\u0000";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    static {RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION);}

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    public BindExpressionLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
                "T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16",
                "T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "String", "Decimal",
                "Fractional", "Identifier", "StringCharacters", "ExponentPart", "Whitespace"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'.'", "'('", "','", "')'", "'['", "']'", "'!'", "'-'", "'*'",
                "'/'", "'%'", "'+'", "'>'", "'>='", "'<'", "'<='", "'=='", "'!='", "'&&'",
                "'||'", "'null'", "'true'", "'false'"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                "String", "Decimal", "Fractional", "Identifier", "Whitespace"
        };
    }

    @Override
    public String[] getRuleNames() {return ruleNames;}

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getSerializedATN() {return _serializedATN;}

    @Override
    public String getGrammarFileName() {return "BindExpression.g4";}

    @Override
    public ATN getATN() {return _ATN;}

    @Override
    public String[] getChannelNames() {return channelNames;}

    @Override
    public String[] getModeNames() {return modeNames;}

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }
}