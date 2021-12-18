package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Assign extends CParseRule {
    // number ::= NUM
    private CToken assign;

    public Assign(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_ASSIGN;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        assign = tk;
        tk = ct.getNextToken(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        this.setCType(CType.getCType(CToken.TK_ASSIGN));
        this.setConstant(true);
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
    }
}
