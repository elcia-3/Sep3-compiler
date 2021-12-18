package lang.c.parse;

import lang.*;
import lang.c.*;

import java.io.PrintStream;

public class Variable extends CParseRule {
    // Variable ::= ident [ array ]
    private CParseRule variable;
    private int identArray = 0;
    private CParseRule ident = null;

    public Variable(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return Ident.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CParseRule array = null;
        ident = new Ident(pcx);
        ident.parse(pcx);
        variable = ident;
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);
        if (Array.isFirst(tk)) {
            array = new IdentArray(pcx, ident);
            array.parse(pcx);
            tk = ct.getCurrentToken(pcx);
            identArray = 1;
            variable = array;
        }
        System.out.println(tk.getType());
        if ((identArray == 0 && tk.getType() == CType.T_paint) || (identArray == 0 && tk.getType() == CType.T_paint)) {
            pcx.fatalError("識別子の後ろは[ array ]です");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (variable != null) {
            ident.semanticCheck(pcx);
            variable.semanticCheck(pcx);
            if (identArray == 1) {
                if (ident.getCType().getType() == CType.T_aint || ident.getCType().getType() == CType.T_paint) {
                    this.setCType(variable.getCType()); // variable の型をそのままコピー
                    this.setConstant(variable.isConstant());
                } else {
                    pcx.fatalError("識別子がint[]型かint*[]型になっていません");
                }
            } else {
                if (ident.getCType().getType() == CType.T_int || ident.getCType().getType() == CType.T_pint) {
                    this.setCType(variable.getCType()); // variable の型をそのままコピー
                    this.setConstant(variable.isConstant());
                } else {
                    pcx.fatalError("文法エラーです");
                }
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        variable.codeGen(pcx);
    }
}

class IdentArray extends CParseRule {
    private CToken op;
    private CParseRule left, right;

    public IdentArray(CParseContext pcx, CParseRule left) {
        this.left = left;
    }

    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_IDENT;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        // ここにやってくるときは、必ずisFirst()が満たされている
        CTokenizer ct = pcx.getTokenizer();
        op = ct.getCurrentToken(pcx);
        // /の次の字句を読む
        System.out.println("in IdentArray:" + op.getText());
        if (Array.isFirst(op)) {
            right = new Array(pcx);
            right.parse(pcx);
        } else {
            pcx.fatalError(op.toExplainString() + "identの後ろは[ array ]です");
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        final int s[][] = {
                // T_err T_int T_pint T_aint T_paint
                { CType.T_err, CType.T_err, CType.T_err, CType.T_err, CType.T_err }, // T_err
                { CType.T_err, CType.T_err, CType.T_err, CType.T_err, CType.T_err }, // T_int
                { CType.T_err, CType.T_err, CType.T_err, CType.T_err, CType.T_err }, // T_pint
                { CType.T_err, CType.T_int, CType.T_err, CType.T_int, CType.T_err }, // T_aint
                { CType.T_err, CType.T_pint, CType.T_err, CType.T_err, CType.T_err }, // T_paint
        };

        left.semanticCheck(pcx);
        if (left.getCType().getType() == CType.T_aint || left.getCType().getType() == CType.T_paint) {
            if (right == null) {
                pcx.fatalError(op.toExplainString() + "文法エラーです");
            }
        }

        if (left != null && right != null)

        {
            left.semanticCheck(pcx);
            right.semanticCheck(pcx);
            int lt = left.getCType().getType(); // +の左辺の型
            int rt = right.getCType().getType(); // +の右辺の型
            System.out.println(left.getCType().getType());
            System.out.println(right.getCType().getType());
            int nt = s[lt][rt]; // 規則による型計算
            if (nt == CType.T_err) {
                pcx.fatalError(op.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型["
                        + right.getCType().toString() + "]は文法エラーです");
            }
            this.setCType(CType.getCType(nt));
            this.setConstant(left.isConstant() && right.isConstant()); // +の左右両方が定数のときだけ定数
        }
        if (left != null && right == null) {
            this.setCType(left.getCType()); // ident の型をそのままコピー
            this.setConstant(left.isConstant());
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        if (left != null && right != null) {
            left.codeGen(pcx); // 左部分木のコード生成を頼む
            right.codeGen(pcx); // 右部分木のコード生成を頼む
            o.println("\tMOV\t(R6), R0\t; array:");
            o.println("\tADD\t-(R6), R0\t; array:");
            o.println("\tMOV\tR1, (R6)+\t; array:");
        }
    }

}
