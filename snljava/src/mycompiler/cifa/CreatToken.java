package mycompiler.cifa;
import java.io.*;
import java.awt.*;
import java.util.*;

class TokenType {
	int lineshow;
	String Lex;
	String Sem;
}

class ChainNodeType {
	TokenType Token = new TokenType(); // 单词
	ChainNodeType nextToken = null; // 指向下一个单词的指针
}

/********************************************************************/
/* 类 名 CreatToken */
/* 功 能 总程序的处理 */
/* 说 明 建立一个类，处理总程序 */
/********************************************************************/
public class CreatToken {
	/* MAXTOKENLEN为单词最大长度定义为40 */
	int MAXTOKENLEN = 40;

	int l = 0; /* 记录源程序长度 */
	int char_num = 0; /* 记录文本行的字节位置 */
	int lineno = 1; /* 记录行号，从1开始 */
	int Tokennum = 0; /* 记录token个数的变量 */
	/* 看单词是否是小写，保留字只能小写，如果有大写则为标识符，is为false */
	boolean is = true;
	boolean EOF = false; /* EOF当为文件尾时,值为true */
	boolean Error = false;
	public String tok = null;

	/********************************************************************/
	/* 函数名 CreatToken */
	/* 功 能 构造函数 */
	/* 说 明 */
	/********************************************************************/
	public CreatToken(String s) {
		tok = returnTokenlist(getTokenlist(s));
	}

	/************************************************************/
	/* 函数名 getTokenlist */
	/* 功 能 取得Token序列函数 */
	/* 说 明 函数从源文件字符串序列中获取所有Token序列 */
	/* 使用确定性有限自动机DFA,采用直接转向法 */
	/* 超前读字符,对保留字采用查表方式识别 */
	/* 产生词法错误时候,仅仅略过产生错误的字符,不加改正 */
	/************************************************************/
	ChainNodeType getTokenlist(String s) {
		ChainNodeType chainHead = new ChainNodeType(); /* 链表的表头 */
		ChainNodeType preNode = chainHead; /* 当前结点的前驱结点 */
		TokenType currentToken = new TokenType(); /* 存放当前的Token */

		/*
		 * String ss=null; int beg=0; StringTokenizer fenxi=new
		 * StringTokenizer(s,"\n"); while (fenxi.hasMoreTokens()) { String
		 * stok=fenxi.nextToken(); if (beg==0) ss=stok+" "+"\n"; else
		 * ss=ss+stok+" "+"\n"; beg=1; }
		 */
		s = s + " ";
		l = s.length();
		char t[] = s.toCharArray();

		do {
			/*
			 * tokenStringIndex用于记录当前正在识别单词的词元存储区 tokenString中的当前正在识别字符位置,初始为0
			 */
			char tokenString[] = new char[MAXTOKENLEN + 1];
			int tokenStringIndex = 0;

			/* 当前状态标志state,始终都是以START作为开始 */
			String state = "START";

			/* tokenString的存储标志save,决定当前识别字符是否存入tokenString */
			boolean save;

			is = true;

			/* 当前确定性有限自动机DFA状态state不是完成状态DONE */
			while (!(state.equals("DONE"))) {
				/* 从源代码文件中获取下一个字符,送入变量c作为当前字符 */
				char c = getNextChar(t);

				/* 当前正识别字符的存储标志save初始为TRUE */
				save = true;

				/*
				 * 当EOF为true,到达文件尾,字符存储标志save设置为false,无需存储
				 * 当前识别单词返回值currentToken设置为文件结束单词ENDFILE
				 */
				if (EOF) {
					state = "DONE";
					save = false;
					currentToken.Lex = "ENDFILE";
				} else if (state.equals("START")) {
					/* 当前DFA状态state为开始状态START,DFA处于当前单词开始位置 */
					/*
					 * 当前字符c为数字,当前DFA状态state设置为数字状态INNUM 确定性有限自动机DFA处于数字类型单词中
					 */
					if (isdigit(c))
						state = "INNUM";

					/*
					 * 当前字符c为字母,当前DFA状态state设置为标识符状态INID 确定性有限自动机DFA处于标识符类型单词中
					 */
					else if (isalpha(c))
						state = "INID";

					/*
					 * 当前字符c为冒号,当前DFA状态state设置为赋值状态INASSIGN 确定性有限自动机DFA处于赋值类型单词中
					 */
					else if (c == ':') {
						state = "INASSIGN";
						save = false;
					}

					/* 当前字符c为.,当前DFA状态state设置为数组下标界限状态 */
					/* INRANGE，确定性有限自动机DFA处于数组下标界限类型单词中 */else if (c == '.') {
						state = "INRANGE";
						save = false;
					}

					/* 当前字符c为',当前DFA状态state设置为字符标志状态 */
					/* INCHAR，确定性有限自动机DFA处于字符标志类型单词中 */
					else if (c == '\'') {
						save = false;
						state = "INCHAR";
					}

					/*
					 * 当前字符c为空白(空格,制表符,换行符),字符存储标志save设置为FALSE
					 * 当前字符为分隔符,不需要产生单词,无须存储
					 */
					else if ((c == ' ') || (c == '\t') || (c == '\n')
							|| (c == '\r'))
						save = false;

					/*
					 * 当前字符c为左括号,字符存储标志save设置为false 当前DFA状态state设置为注释状态INCOMMENT
					 * 确定性有限自动机DFA处于注释中,不生成单词,无需存储
					 */
					else if (c == '{') {
						save = false;
						state = "INCOMMENT";
					}

					/*
					 * 当前字符c为其它字符,当前DFA状态state设置为完成状态DONE
					 * 确定性有限自动机DFA处于单词的结束位置,需进一步分类处理
					 */
					else {
						state = "DONE";
						save = false;
						switch (c) {
						/*
						 * 当前字符c为"=",当前识别单词返回值currentToken设置为 等号单词EQ
						 */
						case '=':
							currentToken.Lex = "EQ";
							break;

						/*
						 * 当前字符c为"<",当前识别单词返回值currentToken设置为 小于单词LT
						 */
						case '<':
							currentToken.Lex = "LT";
							break;

						/*
						 * 当前字符c为"+",当前识别单词返回值currentToken设置为 加号单词PLUS
						 */
						case '+':
							currentToken.Lex = "PLUS";
							break;

						/*
						 * 当前字符c为"-",当前识别单词返回值currentToken设置为 减号单词MINUS
						 */
						case '-':
							currentToken.Lex = "MINUS";
							break;

						/*
						 * 当前字符c为"*",当前识别单词返回值currentToken设置为 乘号单词TIMES
						 */
						case '*':
							currentToken.Lex = "TIMES";
							break;

						/*
						 * 当前字符c为"/",当前识别单词返回值currentToken设置为 除号单词OVER
						 */
						case '/':
							currentToken.Lex = "OVER";
							break;

						/*
						 * 当前字符c为"(",当前识别单词返回值currentToken设置为 左括号单词LPAREN
						 */
						case '(':
							currentToken.Lex = "LPAREN";
							break;

						/*
						 * 当前字符c为")",当前识别单词返回值currentToken设置为 右括号单词RPAREN
						 */
						case ')':
							currentToken.Lex = "RPAREN";
							break;

						/*
						 * 当前字符c为";",当前识别单词返回值currentToken设置为 分号单词SEMI
						 */
						case ';':
							currentToken.Lex = "SEMI";
							break;
						/*
						 * 当前字符c为",",当前识别单词返回值currentToken设置为 逗号单词COMMA
						 */
						case ',':
							currentToken.Lex = "COMMA";
							break;
						/*
						 * 当前字符c为"[",当前识别单词返回值currentToken设置为 左中括号单词LMIDPAREN
						 */
						case '[':
							currentToken.Lex = "LMIDPAREN";
							break;

						/*
						 * 当前字符c为"]",当前识别单词返回值currentToken设置为 右中括号单词RMIDPAREN
						 */
						case ']':
							currentToken.Lex = "RMIDPAREN";
							break;

						/*
						 * 当前字符c为其它字符,当前识别单词返回值currentToken 设置为错误单词ERROR
						 */
						default:
							currentToken.Lex = "ERROR";
							Error = true;
							break;
						}
					}
				}
				/********** 当前状态不为开始状态START的处理结束 **********/

				/* 当前DFA状态state为注释状态INCOMMENT,DFA处于注释位置 */
				else if (state.equals("INCOMMENT")) {
					/* 当前字符存储状态save设置为FALSE,注释中内容不生成单词,无需存储 */
					save = false;

					/* 当前字符c为"}",注释结束.当前DFA状态state设置为开始状态START */
					if (c == '}')
						state = "START";
				}

				/*
				 * 当前DFA状态state为赋值状态INASSIGN, 确定性有限自动机DFA处于赋值单词位置
				 */
				else if (state.equals("INASSIGN")) {
					/* 当前DFA状态state设置为完成状态DONE,赋值单词结束 */
					state = "DONE";
					save = false;

					/*
					 * 当前字符c为"=",当前识别单词返回值currentToken设置为 赋值单词ASSIGN
					 */
					if (c == '=')
						currentToken.Lex = "ASSIGN";

					/*
					 * 当前字符c为其它字符,即":"后不是"=",在输入行缓冲区中回退一个字符
					 * 字符存储状态save设置为FALSE,当前识别单词返回值currentToken设置为 ERROR
					 */
					else {
						ungetNextChar();
						currentToken.Lex = "ERROR";
						Error = true;
					}
				}
				/*
				 * 当前DFA状态state设置为数组下标界限状态INRANGE, 确定性有限自动机DFA处于数组下标界限类型单词中
				 */
				else if (state.equals("INRANGE")) {
					/* 当前DFA状态state设置为完成状态DONE,赋值单词结束 */
					state = "DONE";
					save = false;

					/*
					 * 当前字符c为".",当前识别单词返回值currentToken设置为 下标界UNDERANGE
					 */
					if (c == '.')
						currentToken.Lex = "UNDERANGE";

					/*
					 * 当前字符c为其它字符,即"."后不是".",在输入行缓冲区中回退一个字符
					 * 字符存储状态save设置为FALSE,当前识别单词返回值currentToken设置为 ERROR
					 */
					else {
						ungetNextChar();
						currentToken.Lex = "DOT";
					}
				}

				/* 当前DFA状态state为数字状态INNUM,确定性有限自动机处于数字单词位置 */
				else if (state.equals("INNUM")) {
					/*
					 * 当前字符c不是数字,则在输入行缓冲区源中回退一个字符
					 * 字符存储标志设置为FALSE,当前DFA状态state设置为DONE,数字单词识别完
					 * 成,当前识别单词返回值currentToken设置为数字单词NUM
					 */
					if (!isdigit(c)) {
						ungetNextChar();
						save = false;
						state = "DONE";
						currentToken.Lex = "INTC";
					}
				}
				/* 当前DFA状态state为字符标志状态INCHAR,确定有限自动机处于字符标志状态 */
				else if (state.equals("INCHAR")) {
					if (isalpha(c)) {
						char c1 = getNextChar(t);
						if (c1 == '\'') {
							save = true;
							state = "DONE";
							currentToken.Lex = "ID";
						} else {
							ungetNextChar();
							ungetNextChar();
							state = "DONE";
							currentToken.Lex = "ERROR";
							Error = true;
						}
					} else {
						ungetNextChar();
						state = "DONE";
						currentToken.Lex = "ERROR";
						Error = true;
					}
				}
				/*
				 * 当前DFA状态state为标识符状态INID, 确定性有限自动机DFA处于标识符单词位置
				 */
				else if (state.equals("INID")) {
					/*
					 * 当前字符c不是字母,则在输入行缓冲区源中回退一个字符
					 * 字符存储标志设置为FALSE,当前DFA状态state设置为DONE,标识符单词识别
					 * 完成,当前识别单词返回值currentToken设置为标识符单词ID
					 */
					if ((!isalpha(c)) && (!isdigit(c))) {
						ungetNextChar();
						save = false;
						state = "DONE";
						currentToken.Lex = "ID";
					}
				}
				/* 当前DFA状态state为完成状态DONE,确定性有限自动机DFA处于单词结束位置 */
				else if (state.equals("DONE")) {
				}
				/* 当前DFA状态state为其它状态,此种情况不应发生 */
				else {
					/* 当前DFA状态state设置为完成状态DONE 当前识别单词返回值currentToken设置为错误单词ERROR */
					Error = true;
					state = "DONE";
					currentToken.Lex = "ERROR";
				}
				/*************** 分类判断处理结束 *******************/

				/*
				 * 当前字符存储状态save为TRUE,且当前正识别单词已经识别部分未超过单词
				 * 最大长度,将当前字符c写入当前正识别单词词元存储区tokenString
				 */
				if ((save) && (tokenStringIndex <= MAXTOKENLEN)) {
					tokenString[tokenStringIndex] = c;
					tokenStringIndex = tokenStringIndex + 1;
				}
				if (state.equals("DONE")) {
					/* 当前DFA状态state为完成状态DONE,单词识别完成,将其转化为字符串 */
					String st = (new String(tokenString)).trim(); /* 去掉前后空格 */

					/* 当前单词currentToken为标识符单词类型,查看其是否为保留字单词 */
					if (currentToken.Lex.equals("ID")) {
						if (is) /* 如果单词全是小写，就有可能是保留字 */
							currentToken.Lex = reservedLookup(st);
						if (currentToken.Lex.equals("ID"))
							currentToken.Sem = st;
						else
							currentToken.Sem = " ";
					} else if (currentToken.Lex.equals("INTC"))
						currentToken.Sem = st;
					else
						currentToken.Sem = " ";
				}
			}
			/**************** 循环处理结束 ********************/
			/* 将行号信息存入Token */
			currentToken.lineshow = lineno;

			Tokennum++; /* Token总数目加1 */

			copy(preNode, currentToken);
			preNode.nextToken = new ChainNodeType();
			preNode = preNode.nextToken;
		}
		/*
		 * 直到处理完表示文件结束的Token:ENDFILE，说明处理完所有的Token 并存入了链表中，循环结束
		 */
		while (!(currentToken.Lex.equals("ENDFILE")));
		return chainHead;
	}

	/*******************************************************************/
	/* 函数名 getNextChar */
	/* 功 能 取得下一非空字符函数 */
	/* 说 明 读取一个字节的数据,到达文件尾时,EOF为true */
	/*******************************************************************/
	char getNextChar(char t[]) {
		char a = ' ';
		if (char_num < l) {
			if (t[char_num] == '\n')
				lineno++;
			a = t[char_num];
			char_num++;
		} else
			EOF = true;
		return a;
	}

	/********************************************************/
	/* 函数名 ungetNextChar */
	/* 功 能 字符回退函数 */
	/* 说 明 回退一个字节的数据 */
	/********************************************************/
	void ungetNextChar() {
		/* 如果EOF为false,不是处于源文件末尾,回退一个字节 */
		if (!EOF)
			char_num--;
	}

	/****************************************************/
	/* 函数名 isdigit */
	/* 功 能 检查参数c是不是数字 */
	/* 说 明 */
	/****************************************************/
	boolean isdigit(char c) {
		if ((c == '0') || (c == '1') || (c == '2') || (c == '3') || (c == '4')
				|| (c == '5') || (c == '6') || (c == '7') || (c == '8')
				|| (c == '9'))
			return true;
		else
			return false;
	}

	/****************************************************/
	/* 函数名 isalpha */
	/* 功 能 检查参数c是不是字母 */
	/* 说 明 */
	/****************************************************/
	boolean isalpha(char c) {
		if ((c == 'a') || (c == 'b') || (c == 'c') || (c == 'd') || (c == 'e')
				|| (c == 'f') || (c == 'g') || (c == 'h') || (c == 'i')
				|| (c == 'j') || (c == 'k') || (c == 'l') || (c == 'm')
				|| (c == 'n') || (c == 'o') || (c == 'p') || (c == 'q')
				|| (c == 'r') || (c == 's') || (c == 't') || (c == 'u')
				|| (c == 'v') || (c == 'w') || (c == 'x') || (c == 'y')
				|| (c == 'z'))
			return true;
		else if ((c == 'A') || (c == 'B') || (c == 'C') || (c == 'D')
				|| (c == 'E') || (c == 'F') || (c == 'G') || (c == 'H')
				|| (c == 'I') || (c == 'J') || (c == 'K') || (c == 'L')
				|| (c == 'M') || (c == 'N') || (c == 'O') || (c == 'P')
				|| (c == 'Q') || (c == 'R') || (c == 'S') || (c == 'T')
				|| (c == 'U') || (c == 'V') || (c == 'W') || (c == 'X')
				|| (c == 'Y') || (c == 'Z')) {
			is = false;
			return true;
		} else
			return false;
	}

	/**************************************************************/
	/* 函数名 reservedLookup */
	/* 功 能 保留字查找函数 */
	/* 说 明 使用线性查找,查看一个标识符是否是保留字 */
	/* 标识符如果在保留字表中则返回相应单词,否则返回单词ID */
	/**************************************************************/
	String reservedLookup(String s) {
		/* 字符串s与保留字表中某一表项匹配,函数返回对应保留字单词 */
		if (s.equals("program"))
			return "PROGRAM";
		else if (s.equals("type"))
			return "TYPE";
		else if (s.equals("var"))
			return "VAR";
		else if (s.equals("procedure"))
			return "PROCEDURE";
		else if (s.equals("begin"))
			return "BEGIN";
		else if (s.equals("end"))
			return "END";
		else if (s.equals("array"))
			return "ARRAY";
		else if (s.equals("of"))
			return "OF";
		else if (s.equals("record"))
			return "RECORD";
		else if (s.equals("if"))
			return "IF";
		else if (s.equals("then"))
			return "THEN";
		else if (s.equals("else"))
			return "ELSE";
		else if (s.equals("read"))
			return "READ";
		else if (s.equals("write"))
			return "WRITE";
		else if (s.equals("return"))
			return "RETURN";
		else if (s.equals("integer"))
			return "INTEGER";
		else if (s.equals("fi"))
			return "FI";
		else if (s.equals("while"))
			return "WHILE";
		else if (s.equals("do"))
			return "DO";
		else if (s.equals("endwh"))
			return "ENDWH";
		else if (s.equals("char"))
			return "CHAR";
		else
			/* 字符串s未在保留字表中找到,函数返回标识符单词ID */
			return "ID";
	}

	/*****************************************************************/
	/* 函数名 copy */
	/* 功 能 将b中的信息拷贝到a.Token中。 */
	/* 说 明 */
	/*****************************************************************/
	void copy(ChainNodeType a, TokenType b) {
		a.Token.lineshow = b.lineshow;
		a.Token.Lex = b.Lex;
		a.Token.Sem = b.Sem;
	}

	/*****************************************************************/
	/* 函数名 returnTokenlist */
	/* 功 能 将Token序列输出，显示在文件中。 */
	/* 说 明 用于显示词法分析结果 */
	/*****************************************************************/
	String returnTokenlist(ChainNodeType n) {
		String a = " ";
		ChainNodeType node = n;
		TokenType token = n.Token;
		for (int m = 1; m <= Tokennum; m++) {
			a = a + String.valueOf(token.lineshow) + ":" + token.Lex + ",";

			if (token.Sem == null)
				a = a + " ";
			else
				a = a + token.Sem; /* 输出Sem */

			a = a + "\n";
			node = node.nextToken;
			token = node.Token;
		}
		return a;
	}
}
