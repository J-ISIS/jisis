import java_cup.runtime.*;

%%

%public
%class Scanner
%implements sym

%unicode

%line
%column

%cup
%cupdebug

%{
  StringBuffer string = new StringBuffer();
  
  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }
 
%}


LineTerminator = \r|\n|\r\n
WhiteSpace=[\ \t\b\f\r\n]+
StringCharacter = [^\r\n\"\\]

Number = [1-9][0-9]*

Comma = \,

Punct = [\(\)\{G}\{F}\.\$\^\+\*\"\/\#]

TermU = [^\ \t\b\f\r\n\(\)\{G}\{F}\.\,\$\^\+\*\"\/\#]+

Term = ({TermU} {WhiteSpace})* ({TermU} {WhiteSpace})* ({TermU})+


TermR ={Term}[\$]

G_OP = [(][Gg][)]

F_OP = [(][Ff][)]


%state STRING

%%

<YYINITIAL> {
	
  {Number}			{ return symbol(NUMBER,new Integer(yytext()));}
  
  {Comma}				 { return symbol (COMMA);}

  {WhiteSpace}			{ /*Ignore*/}

 
  {TermR}		         { return symbol(TERM,yytext()); }
  

  {Term}	                 { return symbol(TERM,yytext()); }
  
  "$"				 { return symbol (AMP);}
  "."				 { return symbol (DOT);}
  "+"				 { return symbol (OR);}
  "*"				 { return symbol (AND);}
  "^"				 { return symbol (NOT);}
  "("				 { return symbol (LPAREN);}
  ")"				 { return symbol (RPAREN);}
  "/"				 { return symbol (SLASH);}
  "ANY"                          { return symbol (ANY);}
  {G_OP}				 { return symbol (G_OP);}
  {F_OP}				 { return symbol (F_OP);}

   \"                            { yybegin(STRING); string.setLength(0); }

  
  
 }

 <STRING> {
  \"                             { yybegin(YYINITIAL); return symbol(TERM, string.toString());}
  
  {StringCharacter}+             { string.append( yytext() );  }
}

/* error fallback */
.|\n                             { throw new RuntimeException("Illegal character \""+yytext()+
                                                              "\" at line "+yyline+", column "+yycolumn); }
<<EOF>>                          { return symbol(EOF); }

