package br.com.phdss.fiscal;

import br.com.phdss.EComando;
import br.com.phdss.EEstado;
import br.com.phdss.IECF;
import static br.com.phdss.IECF.ERRO;
import static br.com.phdss.IECF.OK;

/**
 * Classe que representa o ECF generico no sistema e todas suas
 * funcionalidiades.
 *
 * @author Pedro H. Lira
 */
public abstract class Impressora implements IECF {

    protected String[] identificado;
    protected String observacoes;

    /**
     * Construtor padrao.
     */
    public Impressora() {
        this.identificado = null;
        this.observacoes = "";
    }

    @Override
    public double validarGT(double gt) throws Exception {
        String[] resp = getNumGT();
        if (OK.equals(resp[0])) {
            try {
                double gt1 = Double.valueOf(resp[1].replace(",", "."));
                return gt1 != gt ? gt1 : 0.00;
            } catch (NumberFormatException ex) {
                throw new Exception(ex.getMessage());
            }
        } else {
            throw new Exception("Erro ao recuperar o GT.");
        }
    }

    @Override
    public void validarSerial(String serie) throws Exception {
        String[] resp = getNumSerie();
        if (OK.equals(resp[0])) {
            if (!serie.contains(resp[1])) {
                throw new Exception("O ECF conectado tem o Número de Série = " + resp[1]
                        + "\nO número de série do ECF autorizado deste PAF é = " + serie);
            }
        } else {
            throw new Exception("Erro ao recuperar o serial.");
        }
    }

    @Override
    public String[] enviar(EComando comando, String... parametros) {
        String[] resp;
        switch (comando) {
            case ECF_AbreGaveta:
                resp = abrirGaveta();
                break;
            case ECF_CortaPapel:
                resp = cortarPapel();
                break;
            case ECF_CorrigeEstadoErro:
                resp = corrigirEstado();
                break;
            case ECF_DataHora:
                resp = getDataHora();
                break;
            case ECF_DataHoraSB:
                resp = getDataHoraSB();
                break;
            case ECF_GrandeTotal:
                resp = getNumGT();
                break;
            case ECF_NumVersao:
                resp = getVersao();
                break;
            case ECF_NumSerie:
                resp = getNumSerie();
                break;
            case ECF_NumECF:
                resp = getNumECF();
                break;
            case ECF_NumCCF:
                resp = getNumCCF();
                break;
            case ECF_NumCupom:
                resp = getNumCupom();
                break;
            case ECF_NumUltItem:
                resp = getNumItem();
                break;
            case ECF_NumGNF:
                resp = getNumGNF();
                break;
            case ECF_NumGRG:
                resp = getGRG();
                break;
            case ECF_NumCDC:
                resp = getCDC();
                break;
            case ECF_AbreRelatorioGerencial:
                resp = abrirRelatorio(parametros[0]);
                break;
            case ECF_LinhaRelatorioGerencial:
                resp = linhaRelatorio(parametros[0]);
                break;
            case ECF_AbreCupomVinculado:
                resp = abrirCupomVinculado(parametros);
                break;
            case ECF_LinhaCupomVinculado:
                resp = linhaCupomVinculado(parametros[0]);
                break;
            case ECF_FechaRelatorio:
                resp = fecharRelatorio();
                break;
            case ECF_PulaLinhas:
                resp = pularLinhas(Integer.valueOf(parametros[0]));
                break;
            case ECF_IdentificaConsumidor:
                this.identificado = parametros;
                resp = new String[]{OK, ""};
                break;
            case ECF_AbreCupom:
                resp = abrirCupom();
                break;
            case ECF_VendeItem:
                resp = adicionarItem(parametros);
                break;
            case ECF_CancelaItemVendido:
                resp = cancelarItem(parametros[0]);
                break;
            case ECF_SubtotalizaCupom:
                this.observacoes = parametros[1].replaceAll("\\" + SL, ENTER);
                resp = totalizarCupom(parametros[0]);
                break;
            case ECF_EfetuaPagamento:
                resp = efetuarPagamento(parametros);
                break;
            case ECF_FechaCupom:
                resp = fecharCupom();
                break;
            case ECF_CancelaCupom:
                resp = cancelarCupom();
                break;
            case ECF_Sangria:
                resp = sangria(parametros[0]);
                break;
            case ECF_Suprimento:
                resp = suprimento(parametros[0]);
                break;
            case ECF_LeituraX:
                resp = leituraX();
                break;
            case ECF_ReducaoZ:
                resp = reducaoZ();
                break;
            case ECF_DadosReducaoz:
            case ECF_DadosUltimaReducaoZ:
                resp = getDadosZ();
                break;
            case ECF_PafMf_Lmfc_Espelho:
                resp = getLMF("c", parametros);
                break;
            case ECF_PafMf_Lmfc_Impressao:
                resp = getLMF("c", parametros);
                break;
            case ECF_PafMf_Lmfs_Espelho:
                resp = getLMF("s", parametros);
                break;
            case ECF_PafMf_Lmfs_Impressao:
                resp = getLMF("s", parametros);
                break;
            case ECF_PafMf_Lmfc_Cotepe1704:
                resp = getMFD("Z", parametros);
                break;
            case ECF_PafMf_Mfd_Cotepe1704:
                resp = getMFD("C", parametros);
                break;
            case ECF_PafMf_Mfd_Espelho:
                resp = getMFD("E", parametros);
                break;
            default:
                resp = new String[]{ERRO, "Comando nao implementado neste sistema."};
                break;
        }
        return resp;
    }

    /**
     * Metodo que abre a gaveta conectada ao ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] abrirGaveta();

    /**
     * Metodo que corta o papel no ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] cortarPapel();

    /**
     * Metodo que corrige o estado de erro, tentando deixar o ECF livre.
     *
     * @return um Array com OK ou ERRO.
     */
    protected String[] corrigirEstado() {
        try {
            EEstado estado = validarEstado();
            switch (estado) {
                case estPagamento:
                case estVenda:
                    cancelarCupom();
                    break;
                case estRelatorio:
                    fecharRelatorio();
                    break;
            }
            return new String[]{OK, ""};
        } catch (Exception ex) {
            return new String[]{ERRO, ex.getMessage()};
        }
    }

    /**
     * Metodo que retorna a data/hora do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getDataHora();

    /**
     * Metodo que retorna a data/hora do software basico do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getDataHoraSB();

    /**
     * Metodo que retorna a versao do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getVersao();

    /**
     * Metodo que retorna a numero do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumECF();

    /**
     * Metodo que retorna o contador de cupom fiscal do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumCCF();

    /**
     * Metodo que retorna o contador de ordem de operacao do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumCupom();

    /**
     * Metodo que retorna o ultimo item vendido do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumItem();

    /**
     * Metodo que retorna a serie do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumSerie();

    /**
     * Metodo que retorna o gande total do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumGT();

    /**
     * Metodo que retorna o geral nao fiscal do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getNumGNF();

    /**
     * Metodo que retorna o geral de relatorio geral do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getGRG();

    /**
     * Metodo que retorna o contador de debito ou credito do ECF.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getCDC();

    /**
     * Matodo que abre um relatorio nao fiscal.
     *
     * @param rel o indice do relatorio no ECF.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] abrirRelatorio(String rel);

    /**
     * Metodo que imprime um texto no relatorio.
     *
     * @param texto o texto a ser impresso, ver formatacao do ECF usado.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] linhaRelatorio(String texto);

    /**
     * Metodo que abre um relatorio vinculado tipo TEF.
     *
     * @param params dados para enviar ao ECF.
     * @see EComando.ECF_AbreCupomVinculado
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] abrirCupomVinculado(String[] params);

    /**
     * Metodo que imprime um texto no relatorio vinculado.
     *
     * @param texto o texto a ser impresso, ver formatacao do ECF usado.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] linhaCupomVinculado(String texto);

    /**
     * Metodo que fecha um relario aberto.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] fecharRelatorio();

    /**
     * Metod que pula linhas dentro de um relatorio.
     *
     * @param linhas quantidade de linhas que ira pular.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] pularLinhas(Integer linhas);

    /**
     * Metodo que abre um cupom fiscal.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] abrirCupom();

    /**
     * Metodo que adiciona um item ao cupom fiscal.
     *
     * @param params dados para enviar ao ECF.
     * @see EComando.ECF_VendeItem
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] adicionarItem(String[] params);

    /**
     * Metodo que cancela um item vendido.
     *
     * @param item o indice do item a ser cancelado.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] cancelarItem(String item);

    /**
     * Metodo que totaliza o cupom fiscal.
     *
     * @param asc_desc valor de acrescimo ou desconto.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] totalizarCupom(String asc_desc);

    /**
     * Metodo que efetua o pagamento no cupom fiscal.
     *
     * @param params dados para enviar ao ECF.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] efetuarPagamento(String[] params);

    /**
     * Metodo que fecha o cupom fiscal.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] fecharCupom();

    /**
     * Metodo que cancela o cupom fiscal, o atual aberto ou o ultimo.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] cancelarCupom();

    /**
     * Metodo que realiza uma operacao nao fiscal de sangria.
     *
     * @param valor o valor de saida do caixa.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] sangria(String valor);

    /**
     * Metodo que realiza uma operacao nao fiscal de suprimento.
     *
     * @param valor o valor de entrada no caixa.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] suprimento(String valor);

    /**
     * Metodo que emite uma leitura X.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] leituraX();

    /**
     * Metodo que emite a reducao Z.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] reducaoZ();

    /**
     * Metodo que recupera os dados da ultima reducao Z emitida.
     *
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getDadosZ();

    /**
     * Metodo que emite uma Leitura de Memoria Fiscal.
     *
     * @param tipo o tipo da leitura solicitado.
     * @param params datas / contadores de inicio e fim.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getLMF(String tipo, String[] params);

    /**
     * Metodo que emite uma Memoria Fiscal Detalhe em arquivo.
     *
     * @param tipo o tipo da leitura solicitado.
     * @param params datas / contadores de inicio e fim.
     * @return um Array com OK ou ERRO.
     */
    protected abstract String[] getMFD(String tipo, String[] params);

}
