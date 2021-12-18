package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class AmpNumber extends CParseRule {
    // Ampnumber ::= NUM
    private CToken num;

    public AmpNumber(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_POINTER;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getNextToken(pcx);
        if (Number.isFirst(tk)) {
            tk = ct.getNextToken(pcx);
            tk = ct.getNextToken(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "&の後ろはnumberです");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; number starts");
        if (num != null) {
            o.println("\tMOV\t#" + num.getText() + ", (R6)+\t; AmpNumber: 数を積む<" + num.toExplainString() + ">");
        }
        o.println(";;; number completes");
    }
}
