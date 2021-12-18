package lang.c.parse;

import lang.*;
import lang.c.*;

import java.io.PrintStream;

public class MinusFactor extends CParseRule {
    // MinusFactor ::= Ampminusfactorber
    private CParseRule minusfactor;

    public MinusFactor(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_MINUS;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        CToken nt = ct.getNextToken(pcx);
        if (UnsignedFactor.isFirst(nt)) {
            minusfactor = new UnsignedFactor(pcx);
            minusfactor.parse(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "-の後ろはunsignedFactorです");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (minusfactor != null) {
            minusfactor.semanticCheck(pcx);
            if (minusfactor.getCType().getType() == CType.T_pint) {
                pcx.fatalError("-の後ろはポインターではありません");
            } else {
                this.setCType(minusfactor.getCType()); // minusfactor の型をそのままコピー
                this.setConstant(minusfactor.isConstant());
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        if (minusfactor != null) {
            o.println("\tMOV\t#" + 0 + ", (R6)+\t; Number: 数を積む<" + ">");
            minusfactor.codeGen(pcx);
            o.println("\tMOV\t-(R6), R1\t; ExpressionSub: 0から、引き、積む<" + minusfactor.toString() + ">");
            o.println("\tMOV\t-(R6), R0\t; ExpressionSub:");
            o.println("\tSUB\tR1, R0\t; ExpressionSub:");
            o.println("\tMOV\tR0, (R6)+\t; ExpressionSub:");
        }

    }
}
