package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.*;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	@SuppressWarnings("unused")
	private CTokenRule rule;
	private int lineNo, colNo;
	private char backCh;
	private boolean backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.rule = rule;
		lineNo = 1;
		colNo = 1;
	}

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n') {
			colNo = 1;
			++lineNo;
		}
		// System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}

	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') {
			--lineNo;
		}
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;

	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}

	// 次のトークンを読んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken(pctx);
		// System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}

	private CToken readToken(CParseContext pctx) {

		CToken tk = null;
		char ch;
		int startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
				case 0: // 初期状態
					ch = readChar();
					if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
					} else if (ch == (char) -1) { // EOF
						startCol = colNo - 1;
						state = 1;

					} else if (ch == '0') { // 進数
						startCol = colNo - 1;
						text.append(ch);
						state = 10;
					} else if (ch >= '0' && ch <= '9') {
						startCol = colNo - 1;
						text.append(ch);
						state = 3;
					} else if (ch == '+') {
						startCol = colNo - 1;
						text.append(ch);
						state = 4;
					} else if (ch == '-') {
						startCol = colNo - 1;
						text.append(ch);
						state = 5;
					} else if (ch == '/') { // comment,割り算
						startCol = colNo - 1;
						state = 6;
					} else if (ch == '&') { // pointer
						startCol = colNo - 1;
						state = 11;
					} else if (ch == '*') { // 掛け算
						startCol = colNo - 1;
						state = 12;
					} else if (ch == '(') { // LPAR
						startCol = colNo - 1;
						state = 13;
					} else if (ch == ')') { // RPAR
						startCol = colNo - 1;
						state = 14;
					} else if (ch == '[') { // LBRA
						startCol = colNo - 1;
						state = 15;
					} else if (ch == ']') { // RBRA
						startCol = colNo - 1;
						state = 16;
					} else if (Character.isLetter(ch)) { // IDENT
						startCol = colNo - 1;
						text.append(ch);
						state = 17;
					} else if (ch == '=') { // RBRA
						startCol = colNo - 1;
						state = 18;
					} else if (ch == ';') { // RBRA
						startCol = colNo - 1;
						state = 19;
					} else { // ヘンな文字を読んだ
						startCol = colNo - 1;
						text.append(ch);
						state = 2;
					}
					break;
				case 1: // EOFを読んだ
					tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
					accept = true;
					break;
				case 2: // ヘンな文字を読んだ
					tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
					accept = true;
					break;
				case 3: // 数（10進数）の開始
					ch = readChar();
					if (Character.isDigit(ch)) {
						text.append(ch);
					} else {
						// 数の終わり

						backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
						String StringOFR = tk.getText();
						int intOFR = Integer.parseInt(StringOFR);
						if (intOFR >= 32768) {
							tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
						}

						accept = true;
					}
					break;
				case 4: // +を読んだ
					tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
					accept = true;
					break;
				case 5: // -を読んだ
					tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
					accept = true;
					break;
				case 6: // /を読んだ
					ch = readChar();
					if (ch == '/') {
						startCol = colNo - 1;
						state = 7;
					} else if (ch == '*') {
						startCol = colNo - 1;
						state = 8;
					} else {
						state = 9; // 割り算のState
						backChar(ch);
					}
					break;
				case 7:
					ch = readChar();

					while (ch != '\r' && ch != '\n' && ch != (char) -1) {
						ch = readChar();
					}

					if (ch == '\r') {
						ch = readChar();
					} else if (ch == (char) -1) {
						backChar(ch);
						state = 0;
						accept = true;
						tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
						System.err.println("ラインコメントコメント中にEOFが発見されました");

					}

					state = 0;
					break;
				case 8:
					boolean isEnd = false;
					boolean aster = false;

					while (!isEnd) {
						ch = readChar();
						if (aster) {
							if (ch == '/') {
								isEnd = true;
							}
						}
						if (ch == '*') {
							aster = true;
						} else {
							aster = false;
						}

						if (ch == (char) -1) {
							backChar(ch);
							state = 0;
							isEnd = true;
							accept = true;
							tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
							System.err.println("ブロックコメントコメント中にEOFが発見されました");
						}
					}

					state = 0;
					break;
				case 9:
					// 割り算用のステート
					tk = new CToken(CToken.TK_DIV, lineNo, startCol, "/");
					accept = true;
					break;
				case 10:
					ch = readChar();
					boolean end = false;

					if (ch == 'x') {
						text.append(ch);
						ch = readChar();
						int digits = 0;
						if (!(ch >= '0' && ch <= '9' || ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e'
								|| ch == 'f')) {
							backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
							tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
							end = true;
							accept = true;
							state = 0;

							break;
						}

						while (!end) {

							if (ch >= '0' && ch <= '9' || ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e'
									|| ch == 'f') {
								text.append(ch);
								ch = readChar();
								++digits;
							} else {
								if (digits > 4) {
									tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
									end = true;
									accept = true;
									state = 0;
								} else {// 数の終わり
									backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
									tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
									end = true;
									accept = true;
									state = 0;
									break;
								}
							}
						}
					} else {
						int digits = 0;
						int msb = 0;
						while (!end) {
							if (digits == 0 && ch == '1') {
								msb = 1;
							}

							if (ch >= '0' && ch <= '7') {
								text.append(ch);
								ch = readChar();
								++digits;
							} else {
								// 数の終わり
								if (msb != 1 && digits > 5) {
									tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
									end = true;
									accept = true;
									state = 0;
								} else {
									backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
									tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
									end = true;
									accept = true;
									state = 0;
								}
							}
						}

					}
					break;
				case 11: // &を読んだ
					tk = new CToken(CToken.TK_POINTER, lineNo, startCol, "&");
					accept = true;
					break;
				case 12:
					tk = new CToken(CToken.TK_MULT, lineNo, startCol, "*");
					accept = true;
					break;
				case 13:
					tk = new CToken(CToken.TK_LPAR, lineNo, startCol, "(");
					accept = true;
					break;
				case 14:
					tk = new CToken(CToken.TK_RPAR, lineNo, startCol, ")");
					accept = true;
					break;
				case 15:
					tk = new CToken(CToken.TK_LBRA, lineNo, startCol, "[");
					accept = true;
					break;
				case 16:
					tk = new CToken(CToken.TK_RBRA, lineNo, startCol, "]");
					accept = true;
					break;
				case 17:
					ch = readChar();
					if (Character.isDigit(ch) || Character.isLetter(ch) || ch == '_') {
						text.append(ch);
					} else {
						// 英数字の終わり
						backChar(ch); // 数を表さない文字は戻す（読まなかったことにする）
						tk = new CToken(CToken.TK_IDENT, lineNo, startCol, text.toString());
						accept = true;
					}
					break;
				case 18:
					tk = new CToken(CToken.TK_ASSIGN, lineNo, startCol, "=");
					accept = true;
					break;
				case 19:
					tk = new CToken(CToken.TK_SEMI, lineNo, startCol, ";");
					accept = true;
					break;
			}
		}
		return tk;
	}
}
