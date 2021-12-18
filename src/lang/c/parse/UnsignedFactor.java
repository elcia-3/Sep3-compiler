package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class UnsignedFactor extends CParseRule {
    // unsignedfactor ::= factorAmp | number | LPAR expression RPAR
    private CParseRule unsignedfactor;

    public UnsignedFactor(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        if (Number.isFirst(tk) || FactorAmp.isFirst(tk) || LPARexpressionRPAR.isFirst(tk)
                || AddressToValue.isFirst(tk)) {
            return true;
        } else {
            return false;
        }
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        System.out.println("in UnsignedFactor" + tk.getText());
        if (AddressToValue.isFirst(tk)) {
            unsignedfactor = new AddressToValue(pcx);
        }
        if (Number.isFirst(tk)) {
            unsignedfactor = new Number(pcx);
        }
        if (FactorAmp.isFirst(tk)) {
            unsignedfactor = new FactorAmp(pcx);
        }
        if (LPARexpressionRPAR.isFirst(tk)) {
            unsignedfactor = new LPARexpressionRPAR(pcx);
        }
        unsignedfactor.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (unsignedfactor != null) {
            unsignedfactor.semanticCheck(pcx);
            setCType(unsignedfactor.getCType()); // unsignedfactor の型をそのままコピー
            setConstant(unsignedfactor.isConstant()); // unsignedfactor は常に定数
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; unsignedfactor starts");
        if (unsignedfactor != null) {
            unsignedfactor.codeGen(pcx);
        }
        o.println(";;; unsignedfactor completes");
    }
}
