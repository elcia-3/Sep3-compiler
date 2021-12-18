package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class FactorAmp extends CParseRule {
    // FactorAmp ::= Ampnumber
    private CParseRule num;

    public FactorAmp(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_POINTER;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        CToken nt = ct.getNextToken(pcx);
        if (Primary.isFirst(nt)) {
            if (PrimaryMult.isFirst(nt)) {
                pcx.fatalError(tk.toExplainString() + "文法エラーです");
            }
            num = new Primary(pcx);
            num.parse(pcx);
        } else if (Number.isFirst(nt)) {
            num = new Number(pcx);
            num.parse(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "&の後ろは number か　primary です");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (num != null) {
            num.semanticCheck(pcx);
            if (num.getCType().getType() == CType.T_pint) {
                pcx.fatalError("pointerの後ろはpointerではありません");
            } else {
                this.setCType(CType.getCType(CType.T_pint));
                this.setConstant(true);
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; FactorAmp starts");
        num.codeGen(pcx);
        o.println(";;; FactorAmp completes");
    }
}
