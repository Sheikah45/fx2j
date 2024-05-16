// Generated from C:/Users/corey/FAFProjects/fx2j/fx2j-parser/src/main/antlr4/io/github/sheikah45/fx2j/parser/expression/BindExpression.g4 by ANTLR 4.13.1

package io.github.sheikah45.fx2j.parser.antlr;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class BindExpressionParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T__22 = 23, String = 24,
            Decimal = 25, Fractional = 26, Identifier = 27, Whitespace = 28;
    public static final int
            RULE_expression = 0;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\u0004\u0001\u001cX\u0002\u0000\u0007\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0003\u0000\u0013\b\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u0000F\b" +
            "\u0000\n\u0000\f\u0000I\t\u0000\u0003\u0000K\b\u0000\u0001\u0000\u0001" +
            "\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0005\u0000S\b" +
            "\u0000\n\u0000\f\u0000V\t\u0000\u0001\u0000\u0000\u0001\u0000\u0001\u0000" +
            "\u0000\u0000q\u0000\u0012\u0001\u0000\u0000\u0000\u0002\u0003\u0006\u0000" +
            "\uffff\uffff\u0000\u0003\u0004\u0005\u0007\u0000\u0000\u0004\u0013\u0003" +
            "\u0000\u0000\u0017\u0005\u0006\u0005\b\u0000\u0000\u0006\u0013\u0003\u0000" +
            "\u0000\u0016\u0007\b\u0005\u0002\u0000\u0000\b\t\u0003\u0000\u0000\u0000" +
            "\t\n\u0005\u0004\u0000\u0000\n\u0013\u0001\u0000\u0000\u0000\u000b\u0013" +
            "\u0005\u001b\u0000\u0000\f\u0013\u0005\u0018\u0000\u0000\r\u0013\u0005" +
            "\u0019\u0000\u0000\u000e\u0013\u0005\u001a\u0000\u0000\u000f\u0013\u0005" +
            "\u0015\u0000\u0000\u0010\u0013\u0005\u0016\u0000\u0000\u0011\u0013\u0005" +
            "\u0017\u0000\u0000\u0012\u0002\u0001\u0000\u0000\u0000\u0012\u0005\u0001" +
            "\u0000\u0000\u0000\u0012\u0007\u0001\u0000\u0000\u0000\u0012\u000b\u0001" +
            "\u0000\u0000\u0000\u0012\f\u0001\u0000\u0000\u0000\u0012\r\u0001\u0000" +
            "\u0000\u0000\u0012\u000e\u0001\u0000\u0000\u0000\u0012\u000f\u0001\u0000" +
            "\u0000\u0000\u0012\u0010\u0001\u0000\u0000\u0000\u0012\u0011\u0001\u0000" +
            "\u0000\u0000\u0013T\u0001\u0000\u0000\u0000\u0014\u0015\n\u0015\u0000" +
            "\u0000\u0015\u0016\u0005\t\u0000\u0000\u0016S\u0003\u0000\u0000\u0016" +
            "\u0017\u0018\n\u0014\u0000\u0000\u0018\u0019\u0005\n\u0000\u0000\u0019" +
            "S\u0003\u0000\u0000\u0015\u001a\u001b\n\u0013\u0000\u0000\u001b\u001c" +
            "\u0005\u000b\u0000\u0000\u001cS\u0003\u0000\u0000\u0014\u001d\u001e\n" +
            "\u0012\u0000\u0000\u001e\u001f\u0005\f\u0000\u0000\u001fS\u0003\u0000" +
            "\u0000\u0013 !\n\u0011\u0000\u0000!\"\u0005\b\u0000\u0000\"S\u0003\u0000" +
            "\u0000\u0012#$\n\u0010\u0000\u0000$%\u0005\r\u0000\u0000%S\u0003\u0000" +
            "\u0000\u0011&\'\n\u000f\u0000\u0000\'(\u0005\u000e\u0000\u0000(S\u0003" +
            "\u0000\u0000\u0010)*\n\u000e\u0000\u0000*+\u0005\u000f\u0000\u0000+S\u0003" +
            "\u0000\u0000\u000f,-\n\r\u0000\u0000-.\u0005\u0010\u0000\u0000.S\u0003" +
            "\u0000\u0000\u000e/0\n\f\u0000\u000001\u0005\u0011\u0000\u00001S\u0003" +
            "\u0000\u0000\r23\n\u000b\u0000\u000034\u0005\u0012\u0000\u00004S\u0003" +
            "\u0000\u0000\f56\n\n\u0000\u000067\u0005\u0013\u0000\u00007S\u0003\u0000" +
            "\u0000\u000b89\n\t\u0000\u00009:\u0005\u0014\u0000\u0000:S\u0003\u0000" +
            "\u0000\n;<\n\u001a\u0000\u0000<=\u0005\u0001\u0000\u0000=S\u0005\u001b" +
            "\u0000\u0000>?\n\u0019\u0000\u0000?@\u0005\u0001\u0000\u0000@A\u0005\u001b" +
            "\u0000\u0000AJ\u0005\u0002\u0000\u0000BG\u0003\u0000\u0000\u0000CD\u0005" +
            "\u0003\u0000\u0000DF\u0003\u0000\u0000\u0000EC\u0001\u0000\u0000\u0000" +
            "FI\u0001\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000GH\u0001\u0000\u0000" +
            "\u0000HK\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000\u0000JB\u0001\u0000" +
            "\u0000\u0000JK\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000LS\u0005" +
            "\u0004\u0000\u0000MN\n\u0018\u0000\u0000NO\u0005\u0005\u0000\u0000OP\u0003" +
            "\u0000\u0000\u0000PQ\u0005\u0006\u0000\u0000QS\u0001\u0000\u0000\u0000" +
            "R\u0014\u0001\u0000\u0000\u0000R\u0017\u0001\u0000\u0000\u0000R\u001a" +
            "\u0001\u0000\u0000\u0000R\u001d\u0001\u0000\u0000\u0000R \u0001\u0000" +
            "\u0000\u0000R#\u0001\u0000\u0000\u0000R&\u0001\u0000\u0000\u0000R)\u0001" +
            "\u0000\u0000\u0000R,\u0001\u0000\u0000\u0000R/\u0001\u0000\u0000\u0000" +
            "R2\u0001\u0000\u0000\u0000R5\u0001\u0000\u0000\u0000R8\u0001\u0000\u0000" +
            "\u0000R;\u0001\u0000\u0000\u0000R>\u0001\u0000\u0000\u0000RM\u0001\u0000" +
            "\u0000\u0000SV\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000TU\u0001" +
            "\u0000\u0000\u0000U\u0001\u0001\u0000\u0000\u0000VT\u0001\u0000\u0000" +
            "\u0000\u0005\u0012GJRT";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

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

    public BindExpressionParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "expression"
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
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
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

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 0:
                return expression_sempred((ExpressionContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 21);
            case 1:
                return precpred(_ctx, 20);
            case 2:
                return precpred(_ctx, 19);
            case 3:
                return precpred(_ctx, 18);
            case 4:
                return precpred(_ctx, 17);
            case 5:
                return precpred(_ctx, 16);
            case 6:
                return precpred(_ctx, 15);
            case 7:
                return precpred(_ctx, 14);
            case 8:
                return precpred(_ctx, 13);
            case 9:
                return precpred(_ctx, 12);
            case 10:
                return precpred(_ctx, 11);
            case 11:
                return precpred(_ctx, 10);
            case 12:
                return precpred(_ctx, 9);
            case 13:
                return precpred(_ctx, 26);
            case 14:
                return precpred(_ctx, 25);
            case 15:
                return precpred(_ctx, 24);
        }
        return true;
    }

    public final ExpressionContext expression() throws RecognitionException {
        return expression(0);
    }

    private ExpressionContext expression(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
        ExpressionContext _prevctx = _localctx;
        int _startState = 0;
        enterRecursionRule(_localctx, 0, RULE_expression, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(18);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case T__6: {
                        _localctx = new InvertContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(3);
                        match(T__6);
                        setState(4);
                        ((InvertContext) _localctx).base = expression(23);
                    }
                    break;
                    case T__7: {
                        _localctx = new NegateContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(5);
                        match(T__7);
                        setState(6);
                        ((NegateContext) _localctx).base = expression(22);
                    }
                    break;
                    case T__1: {
                        _localctx = new EnclosedContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(7);
                        match(T__1);
                        setState(8);
                        ((EnclosedContext) _localctx).inside = expression(0);
                        setState(9);
                        match(T__3);
                    }
                    break;
                    case Identifier: {
                        _localctx = new VariableContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(11);
                        match(Identifier);
                    }
                    break;
                    case String: {
                        _localctx = new StringLiteralContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(12);
                        match(String);
                    }
                    break;
                    case Decimal: {
                        _localctx = new DecimalLiteralContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(13);
                        match(Decimal);
                    }
                    break;
                    case Fractional: {
                        _localctx = new FractionalLiteralContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(14);
                        match(Fractional);
                    }
                    break;
                    case T__20: {
                        _localctx = new NullLiteralContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(15);
                        match(T__20);
                    }
                    break;
                    case T__21: {
                        _localctx = new TrueLiteralContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(16);
                        match(T__21);
                    }
                    break;
                    case T__22: {
                        _localctx = new FalseLiteralContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(17);
                        match(T__22);
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(84);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
						if (_parseListeners != null) {triggerExitRuleEvent();}
                        _prevctx = _localctx;
                        {
                            setState(82);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 3, _ctx)) {
                                case 1: {
                                    _localctx = new MultiplyContext(new ExpressionContext(_parentctx, _parentState));
                                    ((MultiplyContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(20);
									if (!(precpred(_ctx, 21))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 21)");
									}
                                    setState(21);
                                    match(T__8);
                                    setState(22);
                                    ((MultiplyContext) _localctx).right = expression(22);
                                }
                                break;
                                case 2: {
                                    _localctx = new DivideContext(new ExpressionContext(_parentctx, _parentState));
                                    ((DivideContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(23);
									if (!(precpred(_ctx, 20))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 20)");
									}
                                    setState(24);
                                    match(T__9);
                                    setState(25);
                                    ((DivideContext) _localctx).right = expression(21);
                                }
                                break;
                                case 3: {
                                    _localctx = new RemainderContext(new ExpressionContext(_parentctx, _parentState));
                                    ((RemainderContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(26);
									if (!(precpred(_ctx, 19))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 19)");
									}
                                    setState(27);
                                    match(T__10);
                                    setState(28);
                                    ((RemainderContext) _localctx).right = expression(20);
                                }
                                break;
                                case 4: {
                                    _localctx = new AddContext(new ExpressionContext(_parentctx, _parentState));
                                    ((AddContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(29);
									if (!(precpred(_ctx, 18))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 18)");
									}
                                    setState(30);
                                    match(T__11);
                                    setState(31);
                                    ((AddContext) _localctx).right = expression(19);
                                }
                                break;
                                case 5: {
                                    _localctx = new SubtractContext(new ExpressionContext(_parentctx, _parentState));
                                    ((SubtractContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(32);
									if (!(precpred(_ctx, 17))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 17)");
									}
                                    setState(33);
                                    match(T__7);
                                    setState(34);
                                    ((SubtractContext) _localctx).right = expression(18);
                                }
                                break;
                                case 6: {
                                    _localctx = new GreaterThanContext(new ExpressionContext(_parentctx, _parentState));
                                    ((GreaterThanContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(35);
									if (!(precpred(_ctx, 16))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 16)");
									}
                                    setState(36);
                                    match(T__12);
                                    setState(37);
                                    ((GreaterThanContext) _localctx).right = expression(17);
                                }
                                break;
                                case 7: {
                                    _localctx = new GreaterThanEqualContext(
                                            new ExpressionContext(_parentctx, _parentState));
                                    ((GreaterThanEqualContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(38);
									if (!(precpred(_ctx, 15))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 15)");
									}
                                    setState(39);
                                    match(T__13);
                                    setState(40);
                                    ((GreaterThanEqualContext) _localctx).right = expression(16);
                                }
                                break;
                                case 8: {
                                    _localctx = new LessThanContext(new ExpressionContext(_parentctx, _parentState));
                                    ((LessThanContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(41);
									if (!(precpred(_ctx, 14))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 14)");
									}
                                    setState(42);
                                    match(T__14);
                                    setState(43);
                                    ((LessThanContext) _localctx).right = expression(15);
                                }
                                break;
                                case 9: {
                                    _localctx = new LessThanEqualContext(
                                            new ExpressionContext(_parentctx, _parentState));
                                    ((LessThanEqualContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(44);
									if (!(precpred(_ctx, 13))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 13)");
									}
                                    setState(45);
                                    match(T__15);
                                    setState(46);
                                    ((LessThanEqualContext) _localctx).right = expression(14);
                                }
                                break;
                                case 10: {
                                    _localctx = new EqualityContext(new ExpressionContext(_parentctx, _parentState));
                                    ((EqualityContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(47);
									if (!(precpred(_ctx, 12))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 12)");
									}
                                    setState(48);
                                    match(T__16);
                                    setState(49);
                                    ((EqualityContext) _localctx).right = expression(13);
                                }
                                break;
                                case 11: {
                                    _localctx = new InequalityContext(new ExpressionContext(_parentctx, _parentState));
                                    ((InequalityContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(50);
									if (!(precpred(_ctx, 11))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 11)");
									}
                                    setState(51);
                                    match(T__17);
                                    setState(52);
                                    ((InequalityContext) _localctx).right = expression(12);
                                }
                                break;
                                case 12: {
                                    _localctx = new AndContext(new ExpressionContext(_parentctx, _parentState));
                                    ((AndContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(53);
									if (!(precpred(_ctx, 10))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 10)");
									}
                                    setState(54);
                                    match(T__18);
                                    setState(55);
                                    ((AndContext) _localctx).right = expression(11);
                                }
                                break;
                                case 13: {
                                    _localctx = new OrContext(new ExpressionContext(_parentctx, _parentState));
                                    ((OrContext) _localctx).left = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(56);
									if (!(precpred(_ctx, 9))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 9)");
									}
                                    setState(57);
                                    match(T__19);
                                    setState(58);
                                    ((OrContext) _localctx).right = expression(10);
                                }
                                break;
                                case 14: {
                                    _localctx = new PropertyReadContext(
                                            new ExpressionContext(_parentctx, _parentState));
                                    ((PropertyReadContext) _localctx).base = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(59);
									if (!(precpred(_ctx, 26))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 26)");
									}
                                    setState(60);
                                    match(T__0);
                                    setState(61);
                                    ((PropertyReadContext) _localctx).property = match(Identifier);
                                }
                                break;
                                case 15: {
                                    _localctx = new MethodCallContext(new ExpressionContext(_parentctx, _parentState));
                                    ((MethodCallContext) _localctx).base = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(62);
									if (!(precpred(_ctx, 25))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 25)");
									}
                                    setState(63);
                                    match(T__0);
                                    setState(64);
                                    ((MethodCallContext) _localctx).method = match(Identifier);
                                    setState(65);
                                    match(T__1);
                                    setState(74);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 266338692L) != 0)) {
                                        {
                                            setState(66);
                                            ((MethodCallContext) _localctx).expression = expression(0);
                                            ((MethodCallContext) _localctx).args.add(
                                                    ((MethodCallContext) _localctx).expression);
                                            setState(71);
                                            _errHandler.sync(this);
                                            _la = _input.LA(1);
                                            while (_la == T__2) {
                                                {
                                                    {
                                                        setState(67);
                                                        match(T__2);
                                                        setState(68);
                                                        ((MethodCallContext) _localctx).expression = expression(0);
                                                        ((MethodCallContext) _localctx).args.add(
                                                                ((MethodCallContext) _localctx).expression);
                                                    }
                                                }
                                                setState(73);
                                                _errHandler.sync(this);
                                                _la = _input.LA(1);
                                            }
                                        }
                                    }

                                    setState(76);
                                    match(T__3);
                                }
                                break;
                                case 16: {
                                    _localctx = new CollectionAccessContext(
                                            new ExpressionContext(_parentctx, _parentState));
                                    ((CollectionAccessContext) _localctx).collection = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                    setState(77);
									if (!(precpred(_ctx, 24))) {
										throw new FailedPredicateException(this, "precpred(_ctx, 24)");
									}
                                    setState(78);
                                    match(T__4);
                                    setState(79);
                                    ((CollectionAccessContext) _localctx).accessor = expression(0);
                                    setState(80);
                                    match(T__5);
                                }
                                break;
                            }
                        }
                    }
                    setState(86);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }
    @SuppressWarnings("CheckReturnValue")
    public static class ExpressionContext extends ParserRuleContext {
        public ExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ExpressionContext() {}

        @Override
        public int getRuleIndex() {return RULE_expression;}

        public void copyFrom(ExpressionContext ctx) {
            super.copyFrom(ctx);
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class DecimalLiteralContext extends ExpressionContext {
        public DecimalLiteralContext(ExpressionContext ctx) {copyFrom(ctx);}

        public TerminalNode Decimal() {return getToken(BindExpressionParser.Decimal, 0);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterDecimalLiteral(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitDecimalLiteral(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitDecimalLiteral(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class LessThanEqualContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public LessThanEqualContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterLessThanEqual(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitLessThanEqual(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitLessThanEqual(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class CollectionAccessContext extends ExpressionContext {
        public ExpressionContext collection;
        public ExpressionContext accessor;

        public CollectionAccessContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterCollectionAccess(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitCollectionAccess(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitCollectionAccess(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class FractionalLiteralContext extends ExpressionContext {
        public FractionalLiteralContext(ExpressionContext ctx) {copyFrom(ctx);}

        public TerminalNode Fractional() {return getToken(BindExpressionParser.Fractional, 0);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterFractionalLiteral(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitFractionalLiteral(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitFractionalLiteral(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class TrueLiteralContext extends ExpressionContext {
        public TrueLiteralContext(ExpressionContext ctx) {copyFrom(ctx);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterTrueLiteral(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitTrueLiteral(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitTrueLiteral(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class EnclosedContext extends ExpressionContext {
        public ExpressionContext inside;

        public EnclosedContext(ExpressionContext ctx) {copyFrom(ctx);}

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterEnclosed(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitEnclosed(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitEnclosed(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class FalseLiteralContext extends ExpressionContext {
        public FalseLiteralContext(ExpressionContext ctx) {copyFrom(ctx);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterFalseLiteral(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitFalseLiteral(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitFalseLiteral(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class AndContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public AndContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterAnd(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitAnd(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitAnd(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class LessThanContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public LessThanContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterLessThan(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitLessThan(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitLessThan(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class DivideContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public DivideContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterDivide(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitDivide(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitDivide(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class MultiplyContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public MultiplyContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterMultiply(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitMultiply(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitMultiply(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class EqualityContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public EqualityContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterEquality(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitEquality(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitEquality(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class GreaterThanContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public GreaterThanContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterGreaterThan(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitGreaterThan(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitGreaterThan(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class AddContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public AddContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterAdd(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitAdd(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitAdd(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class GreaterThanEqualContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public GreaterThanEqualContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterGreaterThanEqual(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitGreaterThanEqual(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitGreaterThanEqual(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class NullLiteralContext extends ExpressionContext {
        public NullLiteralContext(ExpressionContext ctx) {copyFrom(ctx);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterNullLiteral(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitNullLiteral(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitNullLiteral(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class OrContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public OrContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterOr(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitOr(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitOr(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class InvertContext extends ExpressionContext {
        public ExpressionContext base;

        public InvertContext(ExpressionContext ctx) {copyFrom(ctx);}

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterInvert(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitInvert(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitInvert(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class SubtractContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public SubtractContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterSubtract(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitSubtract(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitSubtract(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class InequalityContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public InequalityContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterInequality(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitInequality(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitInequality(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class StringLiteralContext extends ExpressionContext {
        public StringLiteralContext(ExpressionContext ctx) {copyFrom(ctx);}

        public TerminalNode String() {return getToken(BindExpressionParser.String, 0);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterStringLiteral(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitStringLiteral(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitStringLiteral(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class NegateContext extends ExpressionContext {
        public ExpressionContext base;

        public NegateContext(ExpressionContext ctx) {copyFrom(ctx);}

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterNegate(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitNegate(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitNegate(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class VariableContext extends ExpressionContext {
        public VariableContext(ExpressionContext ctx) {copyFrom(ctx);}

        public TerminalNode Identifier() {return getToken(BindExpressionParser.Identifier, 0);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterVariable(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitVariable(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitVariable(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class PropertyReadContext extends ExpressionContext {
        public ExpressionContext base;
        public Token property;

        public PropertyReadContext(ExpressionContext ctx) {copyFrom(ctx);}

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode Identifier() {return getToken(BindExpressionParser.Identifier, 0);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).enterPropertyRead(this);
			}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {
				((BindExpressionListener) listener).exitPropertyRead(this);
			}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitPropertyRead(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class RemainderContext extends ExpressionContext {
        public ExpressionContext left;
        public ExpressionContext right;

        public RemainderContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterRemainder(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitRemainder(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitRemainder(this);
			} else {return visitor.visitChildren(this);}
        }
    }
    @SuppressWarnings("CheckReturnValue")
    public static class MethodCallContext extends ExpressionContext {
        public ExpressionContext base;
        public Token method;
        public ExpressionContext expression;
        public List<ExpressionContext> args = new ArrayList<ExpressionContext>();

        public MethodCallContext(ExpressionContext ctx) {copyFrom(ctx);}

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public TerminalNode Identifier() {return getToken(BindExpressionParser.Identifier, 0);}

        @Override
        public void enterRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).enterMethodCall(this);}
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
			if (listener instanceof BindExpressionListener) {((BindExpressionListener) listener).exitMethodCall(this);}
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BindExpressionVisitor) {
				return ((BindExpressionVisitor<? extends T>) visitor).visitMethodCall(this);
			} else {return visitor.visitChildren(this);}
        }
    }
}