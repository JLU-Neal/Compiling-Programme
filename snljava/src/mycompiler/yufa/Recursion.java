package mycompiler.yufa;
import java.util.*;
import java.io.*;
import java.awt.*;

/***************************************/
class SymbTable  /* 在语义分析时用到 */
{
    String idName;
    AttributeIR  attrIR=new AttributeIR();
    SymbTable next=null;
}
class AttributeIR
{
    TypeIR  idtype=new TypeIR();
    String kind;	
    Var var;
    Proc proc;
}
class Var
{
    String access;
    int level;
    int off;
    boolean isParam;
}
class Proc
{
    int level;
    ParamTable param=new ParamTable();
    int mOff;
    int nOff;
    int procEntry;
    int codeEntry;
}
class ParamTable
{
    SymbTable entry=new SymbTable();
    ParamTable next=null;
}
class TypeIR
{
    int size;
    String kind;
    Array array=null;
    FieldChain body=null;
}
class Array
{
    TypeIR indexTy=new TypeIR();
    TypeIR elementTy=new TypeIR();
    int low;
    int up;
}
class FieldChain
{
    String id;
    int off;
    TypeIR unitType=new TypeIR();
    FieldChain next=null;
}
/*******************************************/
class TreeNode   /* 语法树结点的定义 */
{
    TreeNode child[]=new TreeNode[3];
    TreeNode sibling=null;
    int lineno;
    String nodekind;
    String kind;
    int idnum;
    String name[]=new String[10];
    SymbTable table[]=new SymbTable[10];
    Attr attr=new Attr();
}   
class Attr
{
    ArrayAttr arrayAttr=null;  /* 只用到其中一个，用到时再分配内存 */
    ProcAttr procAttr=null;
    ExpAttr expAttr=null;
    String type_name;
}
class ArrayAttr
{
    int low;
    int up;
    String childtype;
}
class ProcAttr
{
    String paramt;
}
class ExpAttr
{
    String op;
    int val;
    String varkind;
    String type;
}


/************************************************/
class TokenType       /****Token序列的定义*******/
{
    int lineshow;
    String Lex;
    String Sem;
} 

/********************************************************************/
/* 类  名 Recursion	                                            */
/* 功  能 总程序的处理					            */
/* 说  明 建立一个类，处理总程序                                    */
/********************************************************************/
public class Recursion
{       
TokenType token=new TokenType();

int MAXTOKENLEN=10;
int  lineno=0;
String temp_name;
StringTokenizer fenxi;

public boolean Error=false;
public String stree;
public String serror;
public TreeNode yufaTree;

public Recursion(String s)
{
    yufaTree=Program(s);
    printTree(yufaTree,0);
}
/********************************************************************/
/********************************************************************/
/********************************************************************/
/* 函数名 Program					            */
/* 功  能 总程序的处理函数					    */
/* 产生式 < Program > ::= ProgramHead DeclarePart ProgramBody .     */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/*        语法树的根节点的第一个子节点指向程序头部分ProgramHead,    */
/*        DeclaraPart为ProgramHead的兄弟节点,程序体部分ProgramBody  */
/*        为DeclarePart的兄弟节点.                                  */
/********************************************************************/
TreeNode Program(String ss)
{
        fenxi=new StringTokenizer(ss,"\n");
        ReadNextToken();

        TreeNode root = newNode("ProcK");
	TreeNode t=ProgramHead();
	TreeNode q=DeclarePart();
	TreeNode s=ProgramBody();		
       
	if (t!=null) 
            root.child[0] = t;
	else 
            syntaxError("a program head is expected!");
	if (q!=null) 
            root.child[1] = q;
	if (s!=null) 
            root.child[2] = s;
	else syntaxError("a program body is expected!");

	match("DOT");
        if (!(token.Lex.equals("ENDFILE")))
	    syntaxError("Code ends before file\n");

        if (Error==true)
            return null;
	return root;
}

/**************************函数头部分********************************/
/********************************************************************/
/********************************************************************/
/* 函数名 ProgramHead						    */
/* 功  能 程序头的处理函数					    */
/* 产生式 < ProgramHead > ::= PROGRAM  ProgramName                  */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProgramHead()
{
    TreeNode t = newNode("PheadK");
    match("PROGRAM");
    if (token.Lex.equals("ID"))
        t.name[0]=token.Sem;
    match("ID");
    return t;
}	
    
/**************************声明部分**********************************/
/********************************************************************/	
/********************************************************************/
/* 函数名 DeclarePart						    */
/* 功  能 声明部分的处理					    */
/* 产生式 < DeclarePart > ::= TypeDec  VarDec  ProcDec              */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode DeclarePart()
{
    /*类型*/
    TreeNode typeP = newNode("TypeK");    	 
    TreeNode tp1 = TypeDec();
    if (tp1!=null)
        typeP.child[0] = tp1;
    else
	typeP=null;

    /*变量*/
    TreeNode varP = newNode("VarK");
    TreeNode tp2 = VarDec();
    if (tp2 != null)
        varP.child[0] = tp2;
    else 
        varP=null;
		 
    /*函数*/
    TreeNode procP = ProcDec();
    if (procP==null)  {}
    if (varP==null)   { varP=procP; }	 
    if (typeP==null)  { typeP=varP; }
    if (typeP!=varP)
	typeP.sibling = varP;
    if (varP!=procP)
        varP.sibling = procP;
    return typeP;
}

/**************************类型声明部分******************************/
/********************************************************************/
/* 函数名 TypeDec					            */
/* 功  能 类型声明部分的处理    				    */
/* 产生式 < TypeDec > ::= ε | TypeDeclaration                      */
/* 说  明 根据文法产生式,调用相应的递归处理函数,生成语法树节点      */
/********************************************************************/
TreeNode TypeDec()
{
    TreeNode t = null;
    if (token.Lex.equals("TYPE"))
        t = TypeDeclaration();
    else if ((token.Lex.equals("VAR"))||(token.Lex.equals("PROCEDURE"))      
            ||(token.Lex.equals("BEGIN"))) {}
         else      
	     ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 TypeDeclaration					    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < TypeDeclaration > ::= TYPE  TypeDecList                 */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode TypeDeclaration()
{
    match("TYPE");
    TreeNode t = TypeDecList();
    if (t==null)
        syntaxError("a type declaration is expected!");
    return t;
}

/********************************************************************/
/* 函数名 TypeDecList		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < TypeDecList > ::= TypeId = TypeName ; TypeDecMore       */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode TypeDecList()
{
    TreeNode t = newNode("DecK");
    if (t != null)
    {
	TypeId(t);                               
	match("EQ");  
	TypeName(t); 
	match("SEMI");                           
        TreeNode p = TypeDecMore();
	if (p!=null)
	    t.sibling = p;
    }
    return t;
}

/********************************************************************/
/* 函数名 TypeDecMore		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < TypeDecMore > ::=    ε | TypeDecList                   */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode TypeDecMore()
{
    TreeNode t=null;
    if (token.Lex.equals("ID"))
        t = TypeDecList();
    else if ((token.Lex.equals("VAR"))||(token.Lex.equals("PROCEDURE"))                       ||(token.Lex.equals("BEGIN"))) {}       
         else
	     ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 TypeId		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < TypeId > ::= id                                         */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void TypeId(TreeNode t)
{
    if ((token.Lex.equals("ID"))&&(t!=null))
    {
	t.name[(t.idnum)]=token.Sem;
	t.idnum = t.idnum+1;
    }
    match("ID");
}

/********************************************************************/
/* 函数名 TypeName		 				    */
/* 功  能 类型声明部分的处理				            */
/* 产生式 < TypeName > ::= BaseType | StructureType | id            */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void TypeName(TreeNode t)
{
   if (t !=null)
   {
      if ((token.Lex.equals("INTEGER"))||(token.Lex.equals("CHAR")))    
          BaseType(t);
      else if ((token.Lex.equals("ARRAY"))||(token.Lex.equals("RECORD")))   
              StructureType(t);
      else if (token.Lex.equals("ID")) 
           {
                 t.kind = "IdK";
	         t.attr.type_name = token.Sem;    
                 match("ID");  
           }
	   else
	       ReadNextToken();
   }
}
/********************************************************************/
/* 函数名 BaseType		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < BaseType > ::=  INTEGER | CHAR                          */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void BaseType(TreeNode t)
{
       if (token.Lex.equals("INTEGER"))
       { 
             match("INTEGER");
             t.kind = "IntegerK";
       }
       else if (token.Lex.equals("CHAR"))     
             {
                 match("CHAR");
                 t.kind = "CharK";
             }
             else
                ReadNextToken();   
}

/********************************************************************/
/* 函数名 StructureType		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < StructureType > ::=  ArrayType | RecType                */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void StructureType(TreeNode t)
{
       if (token.Lex.equals("ARRAY"))
       {
           ArrayType(t); 
       }          
       else if (token.Lex.equals("RECORD"))     
            {
                 t.kind = "RecordK";
                 RecType(t);
            }
            else
                ReadNextToken();   
}
/********************************************************************/
/* 函数名 ArrayType                                                 */
/* 功  能 类型声明部分的处理函数			            */
/* 产生式 < ArrayType > ::=  ARRAY [low..top] OF BaseType           */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void ArrayType(TreeNode t)
{
     t.attr.arrayAttr = new ArrayAttr();
     match("ARRAY");
     match("LMIDPAREN");
     if (token.Lex.equals("INTC"))
	 t.attr.arrayAttr.low = Integer.parseInt(token.Sem);
     match("INTC");
     match("UNDERANGE");
     if (token.Lex.equals("INTC"))
	 t.attr.arrayAttr.up = Integer.parseInt(token.Sem);
     match("INTC");
     match("RMIDPAREN");
     match("OF");
     BaseType(t);
     t.attr.arrayAttr.childtype = t.kind;
     t.kind = "ArrayK";
}

/********************************************************************/
/* 函数名 RecType		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < RecType > ::=  RECORD FieldDecList END                  */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void RecType(TreeNode t)
{
    TreeNode p = null;
    match("RECORD");
    p = FieldDecList();
    if (p!=null)
        t.child[0] = p;
    else
        syntaxError("a record body is requested!");         
    match("END");
}
/********************************************************************/
/* 函数名 FieldDecList		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < FieldDecList > ::=   BaseType IdList ; FieldDecMore     */
/*                             | ArrayType IdList; FieldDecMore     */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode  FieldDecList()
{
    TreeNode t = newNode("DecK");
    TreeNode p = null;
    if (t != null)
    {
        if ((token.Lex.equals("INTEGER"))||(token.Lex.equals("CHAR")))
        {
                    BaseType(t);
	            IdList(t);
	            match("SEMI");
	            p = FieldDecMore();
        }
	else if (token.Lex.equals("ARRAY")) 
             {
	            ArrayType(t);
	            IdList(t);
	            match("SEMI");
	            p = FieldDecMore();
             }
             else
             {
		    ReadNextToken();
		    syntaxError("type name is expected");
             }
        t.sibling = p;
    }	    
    return t;	
}
/********************************************************************/
/* 函数名 FieldDecMore		 				    */
/* 功  能 类型声明部分的处理函数			            */
/* 产生式 < FieldDecMore > ::=  ε | FieldDecList                   */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode FieldDecMore()
{
    TreeNode t = null;   
    if (token.Lex.equals("INTEGER")||token.Lex.equals("CHAR")                          ||token.Lex.equals("ARRAY"))
	t = FieldDecList();
    else if (token.Lex.equals("END")) {}
	 else
             ReadNextToken();
    return t;	
}
/********************************************************************/
/* 函数名 IdList		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < IdList > ::=  id  IdMore                                */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void IdList(TreeNode  t)
{
    if (token.Lex.equals("ID"))
    {
	t.name[(t.idnum)] = token.Sem;
	t.idnum = t.idnum + 1;
        match("ID");
    }
    IdMore(t);
}

/********************************************************************/
/* 函数名 IdMore		 				    */
/* 功  能 类型声明部分的处理函数				    */
/* 产生式 < IdMore > ::=  ε |  , IdList                            */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void IdMore(TreeNode t)
{
    if (token.Lex.equals("COMMA"))
    {
        match("COMMA");
        IdList(t);
    }
    else if (token.Lex.equals("SEMI")) {}
         else
	     ReadNextToken();	
}

/**************************变量声明部分******************************/
/********************************************************************/
/* 函数名 VarDec		 				    */
/* 功  能 变量声明部分的处理				            */
/* 产生式 < VarDec > ::=  ε |  VarDeclaration                      */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode VarDec()
{
    TreeNode t = null;
    if (token.Lex.equals("VAR"))
        t = VarDeclaration();
    else if ((token.Lex.equals("PROCEDURE"))||(token.Lex.equals("BEGIN")))                    {}
	 else
	     ReadNextToken();
    return t;
}
/********************************************************************/
/* 函数名 VarDeclaration		 			    */
/* 功  能 变量声明部分的处理函数				    */
/* 产生式 < VarDeclaration > ::=  VAR  VarDecList                   */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode VarDeclaration()
{
    match("VAR");
    TreeNode t = VarDecList();
    if (t==null)
	syntaxError("a var declaration is expected!");
    return t;
}

/********************************************************************/
/* 函数名 VarDecList		 				    */
/* 功  能 变量声明部分的处理函数				    */
/* 产生式 < VarDecList > ::=  TypeName VarIdList; VarDecMore        */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode VarDecList()
{
    TreeNode t = newNode("DecK");
    TreeNode p = null;
    if (t != null)
    {
	TypeName(t);
	VarIdList(t);
	match("SEMI");
        p = VarDecMore();
	t.sibling = p;
    }
    return t;
}

/********************************************************************/
/* 函数名 VarDecMore		 				    */
/* 功  能 变量声明部分的处理函数				    */
/* 产生式 < VarDecMore > ::=  ε |  VarDecList                      */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode VarDecMore()
{
    TreeNode t =null;
    if ((token.Lex.equals("INTEGER"))||(token.Lex.equals("CHAR"))                        ||(token.Lex.equals("ARRAY"))||(token.Lex.equals("RECORD"))                       ||(token.Lex.equals("ID")))
	t = VarDecList();
    else if ((token.Lex.equals("PROCEDURE"))||(token.Lex.equals("BEGIN")))
	     {}
	 else
             ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 VarIdList		 				    */
/* 功  能 变量声明部分的处理函数			            */
/* 产生式 < VarIdList > ::=  id  VarIdMore                          */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void VarIdList(TreeNode t)
{
    if (token.Lex.equals("ID"))
    {
        t.name[(t.idnum)] = token.Sem;
	t.idnum = t.idnum + 1;
        match("ID");
    }
    else 
    {
	syntaxError("a varid is expected here!");
	ReadNextToken();
    }
    VarIdMore(t);
}

/********************************************************************/
/* 函数名 VarIdMore		 				    */
/* 功  能 变量声明部分的处理函数				    */
/* 产生式 < VarIdMore > ::=  ε |  , VarIdList                      */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void VarIdMore(TreeNode t)
{
    if (token.Lex.equals("COMMA"))
    {   
        match("COMMA");
        VarIdList(t);
    }
    else if (token.Lex.equals("SEMI"))  {}
         else
             ReadNextToken();	
}
/****************************过程声明部分****************************/
/********************************************************************/
/* 函数名 ProcDec		 		                    */
/* 功  能 函数声明部分的处理					    */
/* 产生式 < ProcDec > ::=  ε |  ProcDeclaration                    */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProcDec()
{
    TreeNode t = null;
    if (token.Lex.equals("PROCEDURE"))
        t = ProcDeclaration();
    else if (token.Lex.equals("BEGIN")) {}
         else
	     ReadNextToken();
    return t;
}
/********************************************************************/
/* 函数名 ProcDeclaration		 			    */
/* 功  能 函数声明部分的处理函数				    */
/* 产生式 < ProcDeclaration > ::=  PROCEDURE ProcName(ParamList);   */
/*                                 ProcDecPart                      */
/*                                 ProcBody                         */
/*                                 ProcDecMore                      *
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProcDeclaration()
{
    TreeNode t = newNode("ProcDecK");
    match("PROCEDURE");
    if (token.Lex.equals("ID"))
    {
        t.name[0] = token.Sem;
	t.idnum = t.idnum+1;
	match("ID");
    }
    match("LPAREN");
    ParamList(t);
    match("RPAREN");
    match("SEMI");
    t.child[1] = ProcDecPart();
    t.child[2] = ProcBody();
    t.sibling = ProcDecMore();
    return t;
}
/********************************************************************/
/* 函数名 ProcDecMore    				            */
/* 功  能 更多函数声明中处理函数        	        	    */
/* 产生式 < ProcDecMore > ::=  ε |  ProcDeclaration                */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProcDecMore()
{
    TreeNode t = null;
    if (token.Lex.equals("PROCEDURE"))
        t = ProcDeclaration();
    else if (token.Lex.equals("BEGIN"))  {}
	 else
             ReadNextToken();
    return t;
}
/********************************************************************/
/* 函数名 ParamList		 				    */
/* 功  能 函数声明中参数声明部分的处理函数	        	    */
/* 产生式 < ParamList > ::=  ε |  ParamDecList                     */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void ParamList(TreeNode t)     
{
    TreeNode p = null;
    if ((token.Lex.equals("INTEGER"))||(token.Lex.equals("CHAR"))||                      (token.Lex.equals("ARRAY"))||(token.Lex.equals("RECORD"))||                       (token.Lex.equals("ID"))||(token.Lex.equals("VAR")))
    {
        p = ParamDecList();
        t.child[0] = p;
    } 
    else if (token.Lex.equals("RPAREN")) {} 
	 else
	     ReadNextToken();
}

/********************************************************************/
/* 函数名 ParamDecList		 			    	    */
/* 功  能 函数声明中参数声明部分的处理函数	        	    */
/* 产生式 < ParamDecList > ::=  Param  ParamMore                    */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ParamDecList()
{
    TreeNode t = Param();
    TreeNode p = ParamMore();
    if (p!=null)
        t.sibling = p;
    return t;
}

/********************************************************************/
/* 函数名 ParamMore		 			    	    */
/* 功  能 函数声明中参数声明部分的处理函数	        	    */
/* 产生式 < ParamMore > ::=  ε | ; ParamDecList                    */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ParamMore()
{
    TreeNode t = null;
    if (token.Lex.equals("SEMI"))
    {
        match("SEMI");
        t = ParamDecList();
	if (t==null)
           syntaxError("a param declaration is request!");
    }
    else if (token.Lex.equals("RPAREN"))  {} 
	 else
	     ReadNextToken();
    return t;
}
/********************************************************************/
/* 函数名 Param		 			    	            */
/* 功  能 函数声明中参数声明部分的处理函数	        	    */
/* 产生式 < Param > ::=  TypeName FormList | VAR TypeName FormList  */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode Param()
{
    TreeNode t = newNode("DecK");
    if ((token.Lex.equals("INTEGER"))||(token.Lex.equals("CHAR"))                        ||(token.Lex.equals("ARRAY"))||(token.Lex.equals("RECORD"))
       || (token.Lex.equals("ID")))
    {
         t.attr.procAttr = new ProcAttr();
         t.attr.procAttr.paramt = "valparamType";
	 TypeName(t);
	 FormList(t);
    }
    else if (token.Lex.equals("VAR"))
         {
             match("VAR");
             t.attr.procAttr = new ProcAttr();
             t.attr.procAttr.paramt = "varparamType";
	     TypeName(t);
	     FormList(t);
	 }
         else
             ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 FormList		 			    	    */
/* 功  能 函数声明中参数声明部分的处理函数	        	    */
/* 产生式 < FormList > ::=  id  FidMore                             */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void FormList(TreeNode t)
{
    if (token.Lex.equals("ID"))
    {
	t.name[(t.idnum)] = token.Sem;
	t.idnum = t.idnum + 1;
	match("ID");
    }
    FidMore(t);   
}

/********************************************************************/
/* 函数名 FidMore		 			    	    */
/* 功  能 函数声明中参数声明部分的处理函数	        	    */
/* 产生式 < FidMore > ::=   ε |  , FormList                        */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
void FidMore(TreeNode t)
{      
    if (token.Lex.equals("COMMA"))
    {
        match("COMMA");
	FormList(t);
    }
    else if ((token.Lex.equals("SEMI"))||(token.Lex.equals("RPAREN")))  
             {}
         else
	     ReadNextToken();	  
}
/********************************************************************/
/* 函数名 ProcDecPart		 			  	    */
/* 功  能 函数中的声明部分的处理函数	             	            */
/* 产生式 < ProcDecPart > ::=  DeclarePart                          */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProcDecPart()
{
    TreeNode t = DeclarePart();
    return t;
}

/********************************************************************/
/* 函数名 ProcBody		 			  	    */
/* 功  能 函数体部分的处理函数	                    	            */
/* 产生式 < ProcBody > ::=  ProgramBody                             */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProcBody()
{
    TreeNode t = ProgramBody();
    if (t==null)
	syntaxError("a program body is requested!");
    return t;
}

/****************************函数体部分******************************/
/********************************************************************/
/********************************************************************/
/* 函数名 ProgramBody		 			  	    */
/* 功  能 程序体部分的处理	                    	            */
/* 产生式 < ProgramBody > ::=  BEGIN  StmList   END                 */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ProgramBody()
{
    TreeNode t = newNode("StmLK");
    match("BEGIN");
    t.child[0] = StmList();
    match("END");
    return t;
}

/********************************************************************/
/* 函数名 StmList		 			  	    */
/* 功  能 语句部分的处理函数	                    	            */
/* 产生式 < StmList > ::=  Stm    StmMore                           */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode StmList()
{
    TreeNode t = Stm();
    TreeNode p = StmMore();
    if (t!=null)
    {
       if (p!=null)
	   t.sibling = p;
    }
    return t;
}

/********************************************************************/
/* 函数名 StmMore		 			  	    */
/* 功  能 语句部分的处理函数	                    	            */
/* 产生式 < StmMore > ::=   ε |  ; StmList                         */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode StmMore()
{
    TreeNode t = null;
    if ((token.Lex.equals("ELSE"))||(token.Lex.equals("FI"))||                        (token.Lex.equals("END"))||(token.Lex.equals("ENDWH"))) {}
    else if (token.Lex.equals("SEMI"))
	 {
             match("SEMI");
	     t = StmList();
	 }
	 else
	     ReadNextToken();
    return t;
}
/********************************************************************/
/* 函数名 Stm   		 			  	    */
/* 功  能 语句部分的处理函数	                    	            */
/* 产生式 < Stm > ::=   ConditionalStm   {IF}                       */
/*                    | LoopStm          {WHILE}                    */
/*                    | InputStm         {READ}                     */
/*                    | OutputStm        {WRITE}                    */
/*                    | ReturnStm        {RETURN}                   */
/*                    | id  AssCall      {id}                       */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode Stm()
{
    TreeNode t = null;
    if (token.Lex.equals("IF"))
        t = ConditionalStm();
    else if (token.Lex.equals("WHILE"))         
	     t = LoopStm();
    else if (token.Lex.equals("READ"))  
	     t = InputStm();	 
    else if (token.Lex.equals("WRITE"))   
	     t = OutputStm();	 
    else if (token.Lex.equals("RETURN"))  
	     t = ReturnStm();	 
    else if (token.Lex.equals("ID"))
         {
             temp_name = token.Sem;
	     match("ID");              
             t = AssCall();
         }
	 else
	     ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 AssCall		 			  	    */
/* 功  能 语句部分的处理函数	                    	            */
/* 产生式 < AssCall > ::=   AssignmentRest   {:=,LMIDPAREN,DOT}     */
/*                        | CallStmRest      {(}                    */  
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode AssCall()
{
    TreeNode t = null;
    if ((token.Lex.equals("ASSIGN"))||(token.Lex.equals("LMIDPAREN"))||                  (token.Lex.equals("DOT")))
	t = AssignmentRest();
    else if (token.Lex.equals("LPAREN"))
	     t = CallStmRest();
	 else 
	     ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 AssignmentRest		 			    */
/* 功  能 赋值语句部分的处理函数	                    	    */
/* 产生式 < AssignmentRest > ::=  VariMore : = Exp                  */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode AssignmentRest()
{
    TreeNode t = newStmtNode("AssignK");
	
    /* 赋值语句节点的第一个儿子节点记录赋值语句的左侧变量名，
    /* 第二个儿子结点记录赋值语句的右侧表达式*/

    /*处理第一个儿子结点，为变量表达式类型节点*/
    TreeNode c = newExpNode("VariK");
    c.name[0] = temp_name;
    c.idnum = c.idnum+1;
    VariMore(c);
    t.child[0] = c;
		
    match("ASSIGN");
	  
    /*处理第二个儿子节点*/
    t.child[1] = Exp(); 
				
    return t;
}

/********************************************************************/
/* 函数名 ConditionalStm		 			    */
/* 功  能 条件语句部分的处理函数	                    	    */
/* 产生式 <ConditionalStm>::=IF RelExp THEN StmList ELSE StmList FI */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ConditionalStm()
{
    TreeNode t = newStmtNode("IfK");
    match("IF");
    t.child[0] = Exp();
    match("THEN");
    if (t!=null)  
        t.child[1] = StmList();
    if(token.Lex.equals("ELSE"))
    {
	match("ELSE");   
	t.child[2] = StmList();
    }
    match("FI");
    return t;
}

/********************************************************************/
/* 函数名 LoopStm          		 			    */
/* 功  能 循环语句部分的处理函数	                    	    */
/* 产生式 < LoopStm > ::=   WHILE RelExp DO StmList ENDWH           */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode LoopStm()
{
    TreeNode t = newStmtNode("WhileK");
    match("WHILE");
    t.child[0] = Exp();
    match("DO");
    t.child[1] = StmList();
    match("ENDWH");
    return t;
}

/********************************************************************/
/* 函数名 InputStm          		     	                    */
/* 功  能 输入语句部分的处理函数	                    	    */
/* 产生式 < InputStm > ::=  READ(id)                                */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode InputStm()
{
    TreeNode t = newStmtNode("ReadK");
    match("READ");
    match("LPAREN");
    if (token.Lex.equals("ID"))	
    {
	t.name[0] = token.Sem;
        t.idnum = t.idnum+1;
    }
    match("ID");
    match("RPAREN");
    return t;
}

/********************************************************************/
/* 函数名 OutputStm          		     	                    */
/* 功  能 输出语句部分的处理函数	                    	    */
/* 产生式 < OutputStm > ::=   WRITE(Exp)                            */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode OutputStm()
{
    TreeNode t = newStmtNode("WriteK");
    match("WRITE");
    match("LPAREN");
    t.child[0] = Exp();
    match("RPAREN");
    return t;
}

/********************************************************************/
/* 函数名 ReturnStm          		     	                    */
/* 功  能 返回语句部分的处理函数	                    	    */
/* 产生式 < ReturnStm > ::=   RETURN(Exp)                           */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ReturnStm()
{
    TreeNode t = newStmtNode("ReturnK");
    match("RETURN");
    return t;
}

/********************************************************************/
/* 函数名 CallStmRest          		     	                    */
/* 功  能 函数调用语句部分的处理函数	                  	    */
/* 产生式 < CallStmRest > ::=  (ActParamList)                       */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode CallStmRest()
{
    TreeNode t=newStmtNode("CallK");
    match("LPAREN");
    /*函数调用时，其子节点指向实参*/
    /*函数名的结点也用表达式类型结点*/
    TreeNode c = newExpNode("VariK"); 
    c.name[0] = temp_name;
    c.idnum = c.idnum+1;
    t.child[0] = c;
    t.child[1] = ActParamList();
    match("RPAREN");
    return t;
}

/********************************************************************/
/* 函数名 ActParamList          		   	            */
/* 功  能 函数调用实参部分的处理函数	                	    */
/* 产生式 < ActParamList > ::=     ε |  Exp ActParamMore           */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ActParamList()
{
    TreeNode t = null;
    if (token.Lex.equals("RPAREN"))  {}
    else if ((token.Lex.equals("ID"))||(token.Lex.equals("INTC")))
	 {
	     t = Exp();
             if (t!=null)
	         t.sibling = ActParamMore();
         }
	 else
             ReadNextToken();
    return t;
}

/********************************************************************/
/* 函数名 ActParamMore          		   	            */
/* 功  能 函数调用实参部分的处理函数	                	    */
/* 产生式 < ActParamMore > ::=     ε |  , ActParamList             */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点  */
/********************************************************************/
TreeNode ActParamMore()
{
    TreeNode t = null;
    if (token.Lex.equals("RPAREN"))  {}
    else if (token.Lex.equals("COMMA"))
         {
	     match("COMMA");
	     t = ActParamList();
	 }
	 else	
	     ReadNextToken();
    return t;
}

/*************************表达式部分********************************/
/*******************************************************************/
/* 函数名 Exp							   */
/* 功  能 表达式处理函数					   */
/* 产生式 Exp ::= simple_exp | 关系运算符  simple_exp              */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/*******************************************************************/
TreeNode Exp()
{
    TreeNode t = simple_exp();

    /* 当前单词token为逻辑运算单词LT或者EQ */
    if ((token.Lex.equals("LT"))||(token.Lex.equals("EQ"))) 
    {
        TreeNode p = newExpNode("OpK");

	/* 将当前单词token(为EQ或者LT)赋给语法树节点p的运算符成员attr.op*/
	p.child[0] = t;
        p.attr.expAttr.op = token.Lex;
        t = p;
 
        /* 当前单词token与指定逻辑运算符单词(为EQ或者LT)匹配 */ 
        match(token.Lex);

        /* 语法树节点t非空,调用简单表达式处理函数simple_exp()	   
           函数返回语法树节点给t的第二子节点成员child[1]  */ 
        if (t!=null)
            t.child[1] = simple_exp();
    }
    return t;
}

/*******************************************************************/
/* 函数名 simple_exp						   */
/* 功  能 表达式处理						   */
/* 产生式 simple_exp ::=   term  |  加法运算符  term               */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/*******************************************************************/
TreeNode simple_exp()
{
    TreeNode t = term();

    /* 当前单词token为加法运算符单词PLUS或MINUS */
    while ((token.Lex.equals("PLUS"))||(token.Lex.equals("MINUS")))
    {
	TreeNode p = newExpNode("OpK");
	p.child[0] = t;
        p.attr.expAttr.op = token.Lex;
        t = p;

        match(token.Lex);

	/* 调用元处理函数term(),函数返回语法树节点给t的第二子节点成员child[1] */
        t.child[1] = term();
    }
    return t;
}

/********************************************************************/
/* 函数名 term						            */
/* 功  能 项处理函数						    */
/* 产生式 < 项 > ::=  factor | 乘法运算符  factor		    */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/********************************************************************/
TreeNode term()
{
    TreeNode t = factor();

    /* 当前单词token为乘法运算符单词TIMES或OVER */
    while ((token.Lex.equals("TIMES"))||(token.Lex.equals("OVER")))
    {
	TreeNode p = newExpNode("OpK");
	p.child[0] = t;
        p.attr.expAttr.op = token.Lex;
        t = p;	
        match(token.Lex);
        p.child[1] = factor();    
    }
    return t;
}

/*********************************************************************/
/* 函数名 factor						     */
/* 功  能 因子处理函数						     */
/* 产生式 factor ::= INTC | Variable | ( Exp )                       */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/*********************************************************************/
TreeNode factor()
{
    TreeNode t = null;
    if (token.Lex.equals("INTC")) 
    {
        t = newExpNode("ConstK");

	/* 将当前单词名tokenString转换为整数赋给t的数值成员attr.val */
        t.attr.expAttr.val = Integer.parseInt(token.Sem);
        match("INTC");
    }
    else if (token.Lex.equals("ID"))  	  
	     t = Variable();
    else if (token.Lex.equals("LPAREN")) 
	 {
             match("LPAREN");					
             t = Exp();
             match("RPAREN");					
         }
         else 			
             ReadNextToken();	  
    return t;
}


/********************************************************************/
/* 函数名 Variable						    */
/* 功  能 变量处理函数						    */
/* 产生式 Variable   ::=   id VariMore                   	    */
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/********************************************************************/
TreeNode Variable()
{
    TreeNode t = newExpNode("VariK");
    if (token.Lex.equals("ID"))
    {
	t.name[0] = token.Sem;
        t.idnum = t.idnum+1;
    }
    match("ID");
    VariMore(t);
    return t;
}

/********************************************************************/
/* 函数名 VariMore						    */
/* 功  能 变量处理						    */
/* 产生式 VariMore   ::=  ε                             	    */
/*                       | [Exp]            {[}                     */
/*                       | . FieldVar       {DOT}                   */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/********************************************************************/	
void VariMore(TreeNode t)
{
        if ((token.Lex.equals("EQ"))||(token.Lex.equals("LT"))||                       (token.Lex.equals("PLUS"))||(token.Lex.equals("MINUS"))||                           (token.Lex.equals("RPAREN"))||(token.Lex.equals("RMIDPAREN"))||                     (token.Lex.equals("SEMI"))||(token.Lex.equals("COMMA"))|| 
           (token.Lex.equals("THEN"))||(token.Lex.equals("ELSE"))||                       (token.Lex.equals("FI"))||(token.Lex.equals("DO"))||(token.Lex.equals           ("ENDWH"))||(token.Lex.equals("END"))||(token.Lex.equals("ASSIGN"))||           (token.Lex.equals("TIMES"))||(token.Lex.equals("OVER")))    {}
	else if (token.Lex.equals("LMIDPAREN"))
             {
	         match("LMIDPAREN");
	         t.child[0] = Exp();
                 t.attr.expAttr.varkind = "ArrayMembV";
	         match("RMIDPAREN");
	     }
	else if (token.Lex.equals("DOT"))
             {
	         match("DOT");
	         t.child[0] = FieldVar();
                 t.attr.expAttr.varkind = "FieldMembV";
	     }
	     else
	         ReadNextToken();
}
/********************************************************************/
/* 函数名 FieldVar						    */
/* 功  能 变量处理函数				                    */
/* 产生式 FieldVar   ::=  id  FieldVarMore                          */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/********************************************************************/
TreeNode FieldVar()
{
    TreeNode t = newExpNode("VariK");
    if (token.Lex.equals("ID"))
    {
	t.name[0] = token.Sem;
        t.idnum = t.idnum+1;
    }	
    match("ID");	
    FieldVarMore(t);
    return t;
}

/********************************************************************/
/* 函数名 FieldVarMore  			                    */
/* 功  能 变量处理函数                                              */
/* 产生式 FieldVarMore   ::=  ε| [Exp]            {[}              */ 
/* 说  明 函数根据文法产生式,调用相应的递归处理函数,生成语法树节点 */
/********************************************************************/
void FieldVarMore(TreeNode t)
{
    if ((token.Lex.equals("ASSIGN"))||(token.Lex.equals("TIMES"))||               (token.Lex.equals("EQ"))||(token.Lex.equals("LT"))||                          (token.Lex.equals("PLUS"))||(token.Lex.equals("MINUS"))||                      (token.Lex.equals("OVER"))||(token.Lex.equals("RPAREN"))||               (token.Lex.equals("SEMI"))||(token.Lex.equals("COMMA"))||               (token.Lex.equals("THEN"))||(token.Lex.equals("ELSE"))||               (token.Lex.equals("FI"))||(token.Lex.equals("DO"))||
       (token.Lex.equals("ENDWH"))||(token.Lex.equals("END")))
       {}
    else if (token.Lex.equals("LMIDPAREN"))
         { 
	     match("LMIDPAREN");
	     t.child[0] = Exp();
             t.child[0].attr.expAttr.varkind = "ArrayMembV";
	     match("RMIDPAREN");
	 }
	 else
	     ReadNextToken();
}

/********************************************************************/
/********************************************************************/
/* 函数名 match							    */
/* 功  能 终极符匹配处理函数				            */
/* 说  明 函数参数expected给定期望单词符号与当前单词符号token相匹配 */
/*        如果不匹配,则报非期望单词语法错误			    */
/********************************************************************/
void match(String expected)
{ 
      if (token.Lex.equals(expected))   
	  ReadNextToken();
      else 
      {
	  syntaxError("not match error ");
	  ReadNextToken();
      }     
}

/************************************************************/
/* 函数名 syntaxError                                       */
/* 功  能 语法错误处理函数		                    */
/* 说  明 将函数参数message指定的错误信息输出               */	
/************************************************************/
void syntaxError(String s)     /*向错误信息.txt中写入字符串*/
{
    serror=serror+"\n>>> ERROR :"+"Syntax error at "                              +String.valueOf(token.lineshow)+": "+s; 

    /* 设置错误追踪标志Error为TRUE,防止错误进一步传递 */
    Error = true;
}

/********************************************************************/
/* 函数名 ReadNextToken                                             */
/* 功  能 从Token序列中取出一个Token				    */
/* 说  明 从文件中存的Token序列中依次取一个单词，作为当前单词       */	
/********************************************************************/ 
void ReadNextToken()
{
    if (fenxi.hasMoreTokens())
    {
        int i=1;
	String stok=fenxi.nextToken();
        StringTokenizer fenxi1=new StringTokenizer(stok,":,");
        while (fenxi1.hasMoreTokens())
        {
            String fstok=fenxi1.nextToken();
            if (i==1)
            {
	        token.lineshow=Integer.parseInt(fstok);
	        lineno=token.lineshow;
            }
            if (i==2)
	        token.Lex=fstok;
            if (i==3)
                token.Sem=fstok;
            i++;
        }
    }    
}

/********************************************************
 *********以下是创建语法树所用的各类节点的申请***********
 ********************************************************/
/********************************************************/
/* 函数名 newNode				        */	
/* 功  能 创建语法树节点函数			        */
/* 说  明 该函数为语法树创建一个新的结点      	        */
/*        并将语法树节点成员赋初值。 s为ProcK, PheadK,  */
/*        DecK, TypeK, VarK, ProcDecK, StmLK	        */
/********************************************************/
TreeNode newNode(String s)
{
    TreeNode t=new TreeNode();
    t.nodekind = s;
    t.lineno = lineno;
    return t;
}
/********************************************************/
/* 函数名 newStmtNode					*/	
/* 功  能 创建语句类型语法树节点函数			*/
/* 说  明 该函数为语法树创建一个新的语句类型结点	*/
/*        并将语法树节点成员初始化			*/
/********************************************************/
TreeNode newStmtNode(String s)
{
    TreeNode t=new TreeNode();
    t.nodekind = "StmtK";
    t.lineno = lineno;
    t.kind = s;
    return t;
}
/********************************************************/
/* 函数名 newExpNode					*/
/* 功  能 表达式类型语法树节点创建函数			*/
/* 说  明 该函数为语法树创建一个新的表达式类型结点	*/
/*        并将语法树节点的成员赋初值			*/
/********************************************************/
TreeNode newExpNode(String s)
{
    TreeNode t=new TreeNode();
    t.nodekind = "ExpK";
    t.kind = s;
    t.lineno = lineno;
    t.attr.expAttr = new ExpAttr();
    t.attr.expAttr.varkind = "IdV";
    t.attr.expAttr.type = "Void";
    return t;
}
/********************************************************************/
/* 函数组                    					    */
/* 功  能 将语法树写入文件				            */
/* 说  明 3个函数将不同的信息写入语法树                             */
/********************************************************************/   
void writeStr(String s)                   /*写入字符串*/
{
    stree=stree+s;
} 

void writeSpace()                            /*写空格*/
{
    stree=stree+"  ";
}

void writeTab(int x)              /*写换行符和4个空格*/
{
    stree=stree+"\n";    
    while (x!=0)
    {  stree=stree+"\t";   x--;  }
}
/******************************************************/
/* 函数名 printTree                                   */
/* 功  能 把语法树输出，显示在文件中                  */
/* 说  明                                             */
/******************************************************/
void printTree(TreeNode t,int l)
{
     TreeNode tree=t;
     while (tree!=null)
     {
         if (tree.nodekind.equals("ProcK"))
         { 
             stree="ProcK";
         }      
         else if (tree.nodekind.equals("PheadK"))
              {
                  writeTab(1); 
                  writeStr("PheadK");
                  writeSpace();
                  writeStr(tree.name[0]);
              } 
         else if (tree.nodekind.equals("DecK")) 
              {
                  writeTab(l);
                  writeStr("DecK");
                  writeSpace();
                  if (tree.attr.procAttr!=null)
		  {
                      if (tree.attr.procAttr.paramt.equals("varparamType"))
                          writeStr("Var param:"); 
                      else if (tree.attr.procAttr.paramt.equals("valparamType"))
                               writeStr("Value param:");
                  }
                  if (tree.kind.equals("ArrayK"))
		  { 
                     writeStr("ArrayK");
                     writeSpace();  
                     writeStr(String.valueOf(tree.attr.arrayAttr.low));
                     writeSpace();
                     writeStr(String.valueOf(tree.attr.arrayAttr.up));
                     writeSpace();
                     if (tree.attr.arrayAttr.childtype.equals("CharK"))
                         writeStr("CharK");
                     else if(tree.attr.arrayAttr.childtype.equals("IntegerK"))
                             writeStr("IntegerK");
                  }
                  else if (tree.kind.equals("CharK"))
                           writeStr("CharK");
                  else if (tree.kind.equals("IntegerK"))
                           writeStr("IntegerK");
                  else if (tree.kind.equals("RecordK"))
                           writeStr("RecordK");
                  else if (tree.kind.equals("IdK"))
                       {
                           writeStr("IdK");
                           writeStr(tree.attr.type_name);
                       }
                       else 
                           syntaxError("error1!");
                  if (tree.idnum !=0)
	              for (int i=0 ; i < (tree.idnum);i++)
		           {  writeSpace();  writeStr(tree.name[i]); }   
                  else
                      syntaxError("wrong!no var!");
              }
         else if (tree.nodekind.equals("TypeK"))
              {
                  writeTab(l);           
                  writeStr("TypeK");
              }
         else if (tree.nodekind.equals("VarK"))
              {
                  writeTab(l);
                  writeStr("VarK");
              }
         else if (tree.nodekind.equals("ProcDecK"))
              {
                  writeTab(l); 
                  writeStr("ProcDecK");
                  writeSpace();
                  writeStr(tree.name[0]);
              } 
         else if (tree.nodekind.equals("StmLK"))
              {
                  writeTab(l);
                  writeStr("StmLK");
              }
         else if (tree.nodekind.equals("StmtK"))
              {
                  writeTab(l);
                  writeStr("StmtK");
                  writeSpace();
                  if (tree.kind.equals("IfK"))
		      writeStr("If");
                  else if (tree.kind.equals("WhileK"))
		           writeStr("While");
                  else if (tree.kind.equals("AssignK"))
		           writeStr("Assign");
                  else if (tree.kind.equals("ReadK"))
                       {
		           writeStr("Read");
                           writeSpace();
                           writeStr(tree.name[0]);
                       }
                  else if (tree.kind.equals("WriteK"))
		           writeStr("Write");
                  else if (tree.kind.equals("CallK"))
		           writeStr("Call");  
                  else if (tree.kind.equals("ReturnK"))
		           writeStr("Return");
                       else 
                           syntaxError("error2!");
              }
         else if (tree.nodekind.equals("ExpK"))
              {
                  writeTab(l);
                  writeStr("ExpK");
                  if (tree.kind.equals("OpK"))
                  {
                      writeSpace();
		      writeStr("Op");
                      writeSpace();
                      if (tree.attr.expAttr.op.equals("EQ"))
                          writeStr("=");
                      else if (tree.attr.expAttr.op.equals("LT"))
                               writeStr("<");
                      else if (tree.attr.expAttr.op.equals("PLUS"))
                               writeStr("+");
                      else if (tree.attr.expAttr.op.equals("MINUS"))
                               writeStr("-");
                      else if (tree.attr.expAttr.op.equals("TIMES"))
                               writeStr("*");
                      else if (tree.attr.expAttr.op.equals("OVER"))
                               writeStr("/");
                           else
                               syntaxError("error3!");
                  }
                  else if (tree.kind.equals("ConstK"))
                       {
                           writeSpace();
		           writeStr("Const");
                           writeSpace();
                           writeStr(String.valueOf(tree.attr.expAttr.val));  
		       }        
                  else if (tree.kind.equals("VariK"))
		       {
                           writeSpace();
		           writeStr("Vari");
                           writeSpace();
                           if (tree.attr.expAttr.varkind.equals("IdV"))
                           {
		               writeStr("Id");
                               writeSpace();
                               writeStr(tree.name[0]);
                           }       
                           else if (tree.attr.expAttr.varkind.equals("FieldMembV"))
                                {
		                   writeStr("FieldMember");
                                   writeSpace();
                                   writeStr(tree.name[0]);
                                }  
                           else if (tree.attr.expAttr.varkind.equals("ArrayMembV"))
                                {
		                   writeStr("ArrayMember");
                                   writeSpace();
                                   writeStr(tree.name[0]);
                                }
                                else
                                    syntaxError("var type error!");  
		      }
                      else 
                          syntaxError("error4!");
              }    
              else 
                  syntaxError("error5!");
            
         /* 对语法树结点tree的各子结点递归调用printTree过程 */
         for (int i=0;i<3;i++)
              printTree(tree.child[i],l+1);
         /* 对语法树结点tree的各兄弟结点递归调用printTree过程 */
         tree=tree.sibling;       
    }             
}

}


