package br.com.phdss;

/**
 * Enumerador que define os tipos de estados que o ECF pode se encontrar.
 *
 * @author Pedro H. Lira
 */
public enum EEstado {

    /**
     * Porta Serial ainda não foi aberta.
     */
    estNaoInicializada,
    /**
     * Porta aberta, mas estado ainda nao definido.
     */
    estDesconhecido,
    /**
     * Impressora Livre, sem nenhum cupom aberto pronta para nova venda, Redução
     * Z e Leitura X ok, pode ou não já ter ocorrido 1ª venda no dia...
     */
    estLivre,
    /**
     * Cupom de Venda Aberto com ou sem venda do 1º Item.
     */
    estVenda,
    /**
     * Iniciado Fechamento de Cupom com Formas Pagamento pode ou não ter
     * efetuado o 1º pagamento. Não pode mais vender itens, ou alterar
     * Sub-total.
     */
    estPagamento,
    /**
     * Imprimindo Cupom Fiscal Vinculado ou Relatório Gerencial.
     */
    estRelatorio,
    /**
     * Redução Z já emitida, bloqueada até as 00:00.
     */
    estBloqueada,
    /**
     * Redução Z do dia anterior ainda não foi emitida. Emitir agora.
     */
    estRequerZ,
    /**
     * Esta impressora requer Leitura X todo inicio de dia. É necessário
     * imprimir uma Leitura X para poder vender.
     */
    estRequerX
}
