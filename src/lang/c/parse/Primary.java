package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Primary extends CParseRule {
    // primary ::= primaryMult | variable
    private CParseRule primary;

    public Primary(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        if (PrimaryMult.isFirst(tk) || Variable.isFirst(tk)) {
            return true;
        } else {
            return false;
        }
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        if (PrimaryMult.isFirst(tk)) {
            primary = new PrimaryMult(pcx);
            primary.parse(pcx);
        }
        if (Variable.isFirst(tk)) {
            primary = new Variable(pcx);
            primary.parse(pcx);
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (primary != null) {
            primary.semanticCheck(pcx);
            setCType(primary.getCType()); // number の型をそのままコピー
            setConstant(primary.isConstant()); // number は常に定数
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; primary starts");
        if (primary != null) {
            primary.codeGen(pcx);
        }
        o.println(";;; primary completes");
    }
}
