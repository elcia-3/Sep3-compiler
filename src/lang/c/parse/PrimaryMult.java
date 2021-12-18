package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class PrimaryMult extends CParseRule {
    // PrimaryMult ::= MULT variable
    private CToken nt;
    private CParseRule primarymult;

    public PrimaryMult(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        if (tk.getType() == CToken.TK_MULT) {
            return true;
        } else {
            return false;
        }
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        nt = ct.getNextToken(pcx);
        if (Variable.isFirst(nt)) {
            primarymult = new Variable(pcx);
            primarymult.parse(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "*の後ろはvariableです");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (primarymult != null) {
            primarymult.semanticCheck(pcx);
            if (primarymult.getCType().getType() == CType.T_pint || primarymult.getCType().getType() == CType.T_paint) {
                this.setCType(CType.getCType(CType.T_int));
            } else {
                pcx.fatalError("*の後ろはポインタ型です");
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        if (primarymult != null) {
            primarymult.codeGen(pcx);
            o.println("\tMOV\t-(R6), R0\t; PrimaryMult: アドレスを取り出して、内容を参照して、積む<" + nt.toExplainString() + ">");
            o.println("\tMOV\t(R0), (R6)+\t; PrimaryMult:");
        }
    }
}
