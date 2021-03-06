grammar LA;

@lexer::members {
   void erroLexico(String mensagem) {
      throw new ParseCancellationException(mensagem);
   }
}

fragment LETRA: [a-zA-Z];
fragment ALGARISMO: [0-9];

WS:   (' ') -> skip;
ENDL:  ([\n] | [\t] | [\r]) -> skip;

NUM_INT: (ALGARISMO)+;

NUM_REAL: (ALGARISMO)+ '.' (ALGARISMO)+;

CADEIA: ([\\'] (~[\\'])* [\\']) | ('"' (~'"')* '"');

IDENT: (LETRA|'_') ('_'|ALGARISMO|LETRA)*;

COMENTARIO: '{' ~('}'|'\n'|'\r')* '}' -> skip;

COMENTARIO_NAO_FECHADO: '{' ~('}'|'\n'|'\r')* '\n' { { erroLexico("Linha "+getLine()+": comentario nao fechado"); }; };

ERRO: . { erroLexico("Linha "+getLine()+": "+getText()+" - simbolo nao identificado"); };

/* Programas são constituidos de declarações e um corpo do código */
programa : declaracoes 'algoritmo' corpo 'fim_algoritmo';

/* Declarações podem ser globais ou locais e se referem a variáveis, constantes e seus
respectivos tipos */
declaracoes : (decl_local_global)*;
decl_local_global : declaracao_local | declaracao_global;
declaracao_local : 	'declare' variavel
				 |	'constante' IDENT ':' tipo_basico '=' valor_constante
				 |	'tipo' IDENT ':' tipo;

/* Variáveis são constituidas de identificadores */
variavel : identificador1=identificador (',' outrosIdentificadores+=identificador)* ':' tipo ;
identificador : ident1=IDENT ('.' outrosIdent+=IDENT)* dimensao ;
dimensao : ('[' exp_aritimetica ']')* ;

/* Cada variável (ou constante) possui seu tipo especificado */
tipo : 	registro
	 |	tipo_estendido ;

tipo_basico : 'literal' | 'inteiro' | 'real' | 'logico' ;
tipo_basico_ident : tipo_basico | IDENT ;
tipo_estendido : ('^')? tipo_basico_ident ;
valor_constante : CADEIA | NUM_INT | NUM_REAL | 'verdadeiro' | 'falso' ;
registro : 'registro' (variavel)* 'fim_registro' ;

/* As declarações globais indicam funções ou procedimentos, onde estão explicitados seus
parâmetros e comandos */
declaracao_global : 'procedimento' IDENT '(' (parametros)? ')' (listaDL+=declaracao_local)* (listaComandos+=cmd)* 'fim_procedimento' # declaracao_global_procedimento
				  | 'funcao' IDENT '(' (parametros)? ')' ':' tipo_estendido (listaDL+=declaracao_local)* (listaComandos+=cmd)* 'fim_funcao' # declaracao_global_funcao
				  ;
parametro : ('var')? identificador1=identificador (',' outrosIdentificadores+=identificador)* ':' tipo_estendido ;
parametros : parametro1=parametro (',' outrosParametros+=parametro)* ;
corpo : (listaDL+=declaracao_local)* (listaComandos+=cmd)* ;

/* Comandos definem qualquer tipo de chamada do programa */

cmd : cmdLeia | cmdEscreva | cmdSe | cmdCaso | cmdPara | cmdEnquanto | cmdFaca |
	  cmdAtribuicao | cmdChamada | cmdRetorne ;

cmdLeia : 'leia' '(' ('^')? id1=identificador (',' ('^')? outrosIds+=identificador)* ')' ;
cmdEscreva : 'escreva' '(' exp1=expressao (',' outrasExp+=expressao)* ')' ;
cmdSe : 'se' expressao 'entao' (cmdEntao+=cmd)* ('senao' (cmdSenao+=cmd)*)? 'fim_se' ;
cmdCaso : 'caso' exp_aritimetica 'seja' selecao ('senao' (cmd)*)? 'fim_caso' ;
cmdPara : 'para' IDENT '<-'exp_aritmetica1=exp_aritimetica 'ate' exp_aritmetica2=exp_aritimetica 'faca' (cmd)* 'fim_para' ;
cmdEnquanto :  'enquanto' expressao 'faca' (cmd)* 'fim_enquanto' ;
cmdFaca : 'faca' (cmd)* 'ate' expressao ;
cmdAtribuicao : ('^')? identificador '<-' expressao ;
cmdChamada : IDENT '(' exp1=expressao (',' outrasExp+=expressao)* ')' ;
cmdRetorne : 'retorne' expressao ;

/* Regras utilizadas para o cmdCaso */
selecao : (item_selecao)* ;
item_selecao : constantes ':' (cmd)* ;
constantes : numero_intervalo1=numero_intervalo (',' outrosNumero_intervalo+=numero_intervalo)* ;
numero_intervalo : (op_unario1=op_unario)? ni1=NUM_INT ('..' (op_unario2+=op_unario)? ni2=NUM_INT)? ;

/* Precedência de operadores */
op_unario : '-' ;
exp_aritimetica : termo1=termo (ops+=op1 outrosTermos+=termo)* ;
termo : fator1=fator (ops+=op2 outrosFatores+=fator)* ;
fator : parcela1=parcela (ops+=op3 parcela)* ;
op1 : '+' | '-' ;
op2 : '*' | '/' ;
op3 : '%' ;

parcela : (op_unario)? parcela_unario | parcela_nao_unario ;
parcela_unario : ('^')? identificador # parcela_unario_id
			   | IDENT '(' expressao (',' expressao)* ')' # parcela_unario_chamada
			   | NUM_INT # parcela_unario_inteiro
			   | NUM_REAL # parcela_unario_real
			   | '(' expressao ')' # parcela_unario_expr
			   ;

parcela_nao_unario : '&' identificador # parcela_nao_unario_id
                   | CADEIA # parcela_nao_unario_cadeia
                   ;
exp_relacional : exp_a1=exp_aritimetica (op_rs+=op_relacional outrosExp_a+=exp_aritimetica)? ;
op_relacional : '=' | '<>' | '>=' | '<=' | '>' | '<' ;
expressao : termo_l1=termo_logico (op_ls+=op_logico_1 outrosTermos+=termo_logico)* ;
termo_logico : fator_l1=fator_logico (op_ls+=op_logico_2 outrosFatores+=fator_logico)* ;
fator_logico : ('nao')? parcela_logica ;
parcela_logica : ( 'verdadeiro' | 'falso') | exp_relacional ;
op_logico_1 : 'ou' ;
op_logico_2 : 'e' ;

ERROR: . { throw new ParseCancellationException("Linha "+getLine()+": "+getText()+" - simbolo nao identificado"); };
