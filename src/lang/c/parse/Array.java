package lang.c.parse;

import lang.*;
import lang.c.*;

public class Array extends CParseRule {
    // Array ::= LBRA expression RBRA
    private CParseRule array;

    public Array(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_LBRA;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken lbra = ct.getCurrentToken(pcx);
        CToken expression = ct.getNextToken(pcx);
        if (Expression.isFirst(expression)) {
            array = new Expression(pcx);
            array.parse(pcx);
        } else {
            pcx.fatalError(lbra.toExplainString() + "[の後ろはexpressionです");
        }
        CTokenizer nt = pcx.getTokenizer();
        CToken rbra = nt.getCurrentToken(pcx);
        if (rbra.getType() == CToken.TK_RBRA) {
            rbra = nt.getNextToken(pcx);
        } else {
            pcx.fatalError(lbra.toExplainString() + "[の後ろのexpressionの後ろは]です");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (array != null) {
            array.semanticCheck(pcx);
            if (array.getCType().getType() == CType.T_int) {
                setCType(array.getCType()); // number の型をそのままコピー
                setConstant(array.isConstant()); // number は常に定数
            } else {
                pcx.fatalError("[ ]の中がint型ではありません");
            }
        }

    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        array.codeGen(pcx);

    }
}
