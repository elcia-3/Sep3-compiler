package lang.c.parse;

import lang.*;
import lang.c.*;

public class LPARexpressionRPAR extends CParseRule {
    // LPAR expression RPAR ::= '(' expression ')'
    private CParseRule LparExpressionRpar;

    public LPARexpressionRPAR(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_LPAR;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken lpar = ct.getCurrentToken(pcx);
        CToken expression = ct.getNextToken(pcx);
        if (Expression.isFirst(expression)) {
            LparExpressionRpar = new Expression(pcx);
            LparExpressionRpar.parse(pcx);
        } else {
            pcx.fatalError(lpar.toExplainString() + "(の後ろはexpressionです");
        }
        CTokenizer nt = pcx.getTokenizer();
        CToken rpar = nt.getCurrentToken(pcx);
        if (rpar.getType() == CToken.TK_RPAR) {
            rpar = nt.getNextToken(pcx);
        } else {
            pcx.fatalError(lpar.toExplainString() + "(の後ろのexpressionの後ろは)です");
        }

    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (LparExpressionRpar != null) {
            LparExpressionRpar.semanticCheck(pcx);
            setCType(LparExpressionRpar.getCType()); // number の型をそのままコピー
            setConstant(LparExpressionRpar.isConstant()); // number は常に定数
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        LparExpressionRpar.codeGen(pcx);
    }
}
