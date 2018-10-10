package T1;

import LA.LABaseVisitor;
import LA.LAParser;

import java.util.List;

public class AnalisadorSemantico extends LABaseVisitor<Object> {
    private Escopos pilhaDeEscopos;
    private SaidaParser sp;
    private List<String> listaTipos;
    private String bufferTipoExp = "null";
    public AnalisadorSemantico(SaidaParser sp) {
        this.sp = sp;
    }

    //TODO: CTRL+O para dar override nos métodos


    @Override
    public Void visitPrograma(LAParser.ProgramaContext ctx) {
        pilhaDeEscopos = new Escopos(new TabelaDeSimbolos("global"));
        visitDeclaracoes(ctx.declaracoes());
        visitCorpo(ctx.corpo());

        pilhaDeEscopos.desempilhar();
        return null;
    }

    @Override
    public Object visitVariavel(LAParser.VariavelContext ctx) {
        if (!pilhaDeEscopos.escopoAtual().existeSimbolo(ctx.identificador1.getText())) {
            pilhaDeEscopos.escopoAtual().adicionarSimbolo(ctx.identificador1.getText(), ctx.tipo().getText());
            if (!ctx.tipo().getText().equals("literal") && !ctx.tipo().getText().equals("inteiro") && !ctx.tipo().getText().equals("real") && !ctx.tipo().getText().equals("logico")) {
                sp.println("Linha " + ctx.tipo().getStart().getLine() + ": tipo " + ctx.tipo().getText() + " nao declarado");
            }
        } else {
            sp.println("Linha " + ctx.getStart().getLine() + ": identificador " + ctx.identificador1.getText() + " ja declarado anteriormente");
        }
        if (ctx.outrosIdentificadores != null) {
            for (LAParser.IdentificadorContext outrosIdentCtx : ctx.outrosIdentificadores) {
                if (!pilhaDeEscopos.escopoAtual().existeSimbolo(outrosIdentCtx.getText())) {
                    pilhaDeEscopos.escopoAtual().adicionarSimbolo(outrosIdentCtx.getText(), ctx.tipo().getText());
                    if (!ctx.tipo().getText().equals("literal") && !ctx.tipo().getText().equals("inteiro") && !ctx.tipo().getText().equals("real") && !ctx.tipo().getText().equals("logico")) {
                        sp.println("Linha " + ctx.tipo().getStart().getLine() + ": tipo " + ctx.tipo().getText() + " nao declarado");
                    }
                } else {
                    sp.println("Linha " + outrosIdentCtx.getStart().getLine() + ": identificador " + outrosIdentCtx.getText() + " ja declarado anteriormente");
                }
            }
        }
        return null;
    }

    @Override
    public Object visitIdentificador(LAParser.IdentificadorContext ctx) {
        if(!pilhaDeEscopos.existeSimbolo(ctx.ident1.getText())){
            sp.println("Linha " + ctx.getStart().getLine() + ": identificador " + ctx.ident1.getText() + " nao declarado");
        }
        return null;
    }

    @Override
    public Object visitCmdAtribuicao(LAParser.CmdAtribuicaoContext ctx) {
        String tipoVariavel = pilhaDeEscopos.getSimboloTipo(ctx.identificador().getText());
       bufferTipoExp = "null";
        visitExpressao(ctx.expressao());

        if(bufferTipoExp.equals("inteiro") && tipoVariavel.equals("real")){
            bufferTipoExp = "real";
        }
        if(!tipoVariavel.equals(bufferTipoExp)){
            sp.println("Linha "+ ctx.identificador().getStart().getLine() + ": atribuicao nao compativel para " + ctx.identificador().getText());
        }
        return null;
    }

    /*
    @Override
    public String visitExpressao(LAParser.ExpressaoContext ctx) {
        if(ctx.termo_logico() != null){
            visitTermo_logico(ctx.termo_l1);
            */
/*for(LAParser.Termo_logicoContext outroTermo: ctx.outrosTermos){
                if(!visitTermo_logico(outroTermo).equals(tipo))
            }*//*

        }
        return tipo;
    }

    @Override
    public String visitTermo_logico(LAParser.Termo_logicoContext ctx) {
        String tipo = "null";
        if(ctx.fator_logico() != null){
            tipo = visitFator_logico(ctx.fator_logico(0));
        }
        return tipo;
    }
*/

/*    @Override
    public String visitFator_logico(LAParser.Fator_logicoContext ctx) {
        String tipo = "null";
        if(ctx.parcela_logica() != null){
            tipo = visitParcela_logica(ctx.parcela_logica());
        }
        return tipo;
    }

    @Override
    public String visitParcela_logica(LAParser.Parcela_logicaContext ctx) {
        String tipo = "logico";
        if(ctx.exp_relacional() != null){
            tipo = visitExp_relacional(ctx.exp_relacional());
        }

        return tipo;
    }
*/
    @Override
    public Object visitExp_relacional(LAParser.Exp_relacionalContext ctx) {
        if(ctx.exp_a1 != null){
            visitExp_aritimetica(ctx.exp_a1);
        }
        if(ctx.op_relacional != null){
            bufferTipoExp = "logico";
        }
        return null;
    }
/*
    @Override
    public String visitExp_aritimetica(LAParser.Exp_aritimeticaContext ctx) {
        String tipo = "null";
        if(ctx.termo1 != null){
            tipo = visitTermo(ctx.termo1);
            //todo for
        }
        return tipo;
    }

    @Override
    public String visitTermo(LAParser.TermoContext ctx) {
        String tipo = "null";
        if(ctx.fator1 != null){
            tipo = visitFator(ctx.fator1);
            //todo for
        }
        return tipo;
    }

    @Override
    public String visitFator(LAParser.FatorContext ctx) {
        String tipo = "null";
        if(ctx.parcela1 != null) {
            tipo = visitParcela(ctx.parcela1);
            //todo implementar o for
        }
        return tipo;
    }

    @Override
    public String visitParcela(LAParser.ParcelaContext ctx) {
        String tipo = "null";

        if(ctx.parcela_unario() != null){
            tipo = (String) visit(ctx.parcela_unario());
            //return "real";
        }
        return tipo;
    }*/


    @Override
    public Object visitParcela_unario_id(LAParser.Parcela_unario_idContext ctx) {
        visitIdentificador(ctx.identificador());
        bufferTipoExp = pilhaDeEscopos.getSimboloTipo(ctx.identificador().getText());
        return null;
    }

    @Override
    public Object visitParcela_unario_inteiro(LAParser.Parcela_unario_inteiroContext ctx) {
        bufferTipoExp = "inteiro";
        return null;
    }

    @Override
    public Object visitParcela_unario_real(LAParser.Parcela_unario_realContext ctx) {
        bufferTipoExp = "real";
        return null;
    }

    @Override
    public Object visitParcela_nao_unario_cadeia(LAParser.Parcela_nao_unario_cadeiaContext ctx) {
        bufferTipoExp = "literal";
        return null;
    }
}

