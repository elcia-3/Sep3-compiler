package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class AddressToValue extends CParseRule {
    // addresstovalue ::= primary
    private CParseRule primary;

    public AddressToValue(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return Primary.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        primary = new Primary(pcx);
        primary.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (primary != null) {
            primary.semanticCheck(pcx);
            this.setCType(primary.getCType()); // primary の型をそのままコピー
            this.setConstant(primary.isConstant());
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; addresstovalue starts");
        if (primary != null) {
            primary.codeGen(pcx);
            o.println("\tMOV\t-(R6), R0\t; AddressToValue: アドレスを取り出して、内容を参照して、積む");
            o.println("\tMOV\t(R0), (R6)+\t; AddressToValue:");
        }
        o.println(";;; addresstovalue completes");
    }
}