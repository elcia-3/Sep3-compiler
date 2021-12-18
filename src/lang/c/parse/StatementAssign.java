package lang.c.parse;

import java.io.PrintStream;

import lang.*;
import lang.c.*;

public class StatementAssign extends CParseRule {
    // StatementAssign ::= primary ASSIGN expression SEMI
    private CParseRule statementAssign;
    private CParseRule primary;
    private CParseRule expression;

    public StatementAssign(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return Primary.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        primary = new Primary(pcx);
        statementAssign = primary;
        statementAssign.parse(pcx);
        tk = ct.getCurrentToken(pcx);
        if (tk.getType() == CToken.TK_ASSIGN) {
            tk = ct.getNextToken(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "primariyの後ろはASSIGNです");
        }
        if (Expression.isFirst(tk)) {
            expression = new Expression(pcx);
            statementAssign = expression;
            statementAssign.parse(pcx);
            tk = ct.getCurrentToken(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "ASSIGNの後ろはexpressionです");
        }
        if (tk.getType() == CToken.TK_SEMI) {
            ct.getNextToken(pcx);
        } else {
            pcx.fatalError(tk.toExplainString() + "expressionの後ろはSEMIです");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (primary != null && expression != null) {
            primary.semanticCheck(pcx);
            expression.semanticCheck(pcx);
            if (primary.getCType() != expression.getCType()) {
                pcx.fatalError("左辺と右辺で方が一致していません");
            }
            if (primary.isConstant() == true) {
                pcx.fatalError("定数には代入できません");
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; statementAssign starts");
        if (primary != null) {
            primary.codeGen(pcx);
        }
        if (expression != null) {
            expression.codeGen(pcx);
        }
        o.println("\tMOV\t-(R6), R0\t; StatementAssign:");
        o.println("\tMOV\t-(R6), R1\t; StatementAssign:");
        o.println("\tMOV\tR0, (R1)\t; StatementAssign:");
        o.println(";;; statementAssign completes");
    }
}
