package lang.c.parse;

import lang.*;
import lang.c.*;

public class PlusFactor extends CParseRule {
    // PlusFactor ::= Ampplusfactorber
    private CParseRule plusfactor;

    public PlusFactor(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_PLUS;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        CToken nt = ct.getNextToken(pcx);
        if (UnsignedFactor.isFirst(nt)) {
            plusfactor = new UnsignedFactor(pcx);
            plusfactor.parse(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "+の後ろはunsignedFactorです");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (plusfactor != null) {
            plusfactor.semanticCheck(pcx);
            this.setCType(plusfactor.getCType()); // plusfactor の型をそのままコピー
            this.setConstant(plusfactor.isConstant());
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        plusfactor.codeGen(pcx);
    }
}
