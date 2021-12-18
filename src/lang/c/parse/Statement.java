package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class Statement extends CParseRule {
    // statement ::= statementAssign
    private CParseRule statement;

    public Statement(CToken tk) {
    }

    public static boolean isFirst(CToken tk) {
        return StatementAssign.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        statement = new StatementAssign(pcx);
        statement.parse(pcx);
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (statement != null) {
            statement.semanticCheck(pcx);
            setCType(statement.getCType()); // number の型をそのままコピー
            setConstant(statement.isConstant()); // number は常に定数
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; statement starts");
        if (statement != null) {
            statement.codeGen(pcx);
        }
        o.println(";;; statement completes");
    }
}