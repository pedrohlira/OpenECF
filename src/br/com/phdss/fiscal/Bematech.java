package br.com.phdss.fiscal;

import bemajava.BemaInteger;
import bemajava.BemaString;
import br.com.phdss.EEstado;
import br.com.phdss.Util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa o ECF da Bematech no sistema e todas suas
 * funcionalidades.
 *
 * @author Pedro H. Lira
 */
public class Bematech extends Impressora {

    /**
     * Construtor padrao.
     */
    public Bematech() {
        super();
    }

    /**
     * Metodo que faz o tratamento do retorno.
     *
     * @param retorno o inteiro que representado a resposta do ECF.
     * @return um Array com OK ou ERRO, mais o texto do erro.
     */
    private String[] getRetorno(int retorno) {
        return getRetorno(retorno, new BemaString());
    }

    /**
     * Metodo que faz o tratamento do retorno.
     *
     * @param retorno o inteiro que representado a resposta do ECF.
     * @param parametro um valor quando o retorno devolve um texto.
     * @return um Array com OK ou ERRO, mais o texto do erro.
     */
    private String[] getRetorno(int retorno, BemaString parametro) {
        if (retorno == 1) {
            return new String[]{OK, parametro.getBuffer().trim()};
        } else {
            String erro;
            switch (retorno) {
                case 0:
                    erro = "Erro de comunicação.";
                    break;
                case -2:
                    erro = "Parâmetro inválido na função.";
                    break;
                case -3:
                    erro = "Alíquota não programada.";
                    break;
                case -4:
                    erro = "O arquivo de inicialização BemaFI32.ini não foi encontrado no diretório de sistema do Windows.";
                    break;
                case -5:
                    erro = "Erro ao abrir a porta de comunicação.";
                    break;
                case -24:
                    erro = "Forma de pagamento não programada.";
                    break;
                case -27:
                    erro = "Status da impressora diferente de 6,0,0 (ACK, ST1 e ST2).";
                    break;
                case -36:
                    erro = "Forma de pagamento não finalizada.";
                    break;
                default:
                    erro = "Erro não idenfidicado";
                    break;

            }
            return new String[]{ERRO, erro.trim()};
        }
    }

    @Override
    public void ativar() throws Exception {
        int iRetorno = bemajava.Bematech.AbrePortaSerial();
        if (iRetorno != 1) {
            throw new Exception("Problemas com a comunicacao na porta do ECF.");
        }
    }

    @Override
    public void conectar(String porta, int velocidade, int modelo) throws Exception {
        bemajava.Bematech.HabilitaDesabilitaRetornoEstendidoMFD("1");
    }

    @Override
    public void desativar() {
        bemajava.Bematech.FechaPortaSerial();
    }

    @Override
    public EEstado validarEstado() throws Exception {
        BemaInteger status = new BemaInteger();
        bemajava.Bematech.FlagsFiscais(status);

        EEstado estado;
        switch (status.getNumber()) {
            case 1:
                estado = EEstado.estVenda;
                break;
            case 2:
                estado = EEstado.estPagamento;
                break;
            case 8:
                estado = EEstado.estBloqueada;
                break;
            case 32:
                estado = EEstado.estRequerZ;
                break;
            default:
                estado = EEstado.estLivre;
                break;
        }
        return estado;
    }

    @Override
    protected String[] abrirGaveta() {
        bemajava.Bematech.AcionaGaveta();
        return new String[]{OK, ""};
    }

    @Override
    protected String[] cortarPapel() {
        bemajava.Bematech.AcionaGuilhotinaMFD(1);
        return new String[]{OK, ""};
    }

    @Override
    protected String[] getDataHora() {
        BemaString data = new BemaString();
        BemaString hora = new BemaString();
        BemaString dataHora = new BemaString();
        int iRetorno = bemajava.Bematech.DataHoraImpressora(data, hora);
        dataHora.setBuffer(Util.formataTexto(data.getBuffer() + hora.getBuffer(), "##/##/## ##:##:##"));
        return getRetorno(iRetorno, dataHora);
    }

    @Override
    protected String[] getDataHoraSB() {
        BemaString dataHora = new BemaString();
        int iRetorno = bemajava.Bematech.DataHoraGravacaoUsuarioSWBasicoMFAdicional(new BemaString(), dataHora, new BemaString());
        return getRetorno(iRetorno, dataHora);
    }

    @Override
    protected String[] getVersao() {
        BemaString versao = new BemaString();
        int iRetorno = bemajava.Bematech.VersaoFirmwareMFD(versao);
        return getRetorno(iRetorno, versao);
    }

    @Override
    protected String[] getNumECF() {
        BemaString ecf = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroCaixa(ecf);
        return getRetorno(iRetorno, ecf);
    }

    @Override
    protected String[] getNumCCF() {
        BemaString ccf = new BemaString();
        int iRetorno = bemajava.Bematech.ContadorCupomFiscalMFD(ccf);
        return getRetorno(iRetorno, ccf);
    }

    @Override
    protected String[] getNumCupom() {
        BemaString coo = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroCupom(coo);
        return getRetorno(iRetorno, coo);
    }

    @Override
    protected String[] getNumItem() {
        BemaString item = new BemaString();
        int iRetorno = bemajava.Bematech.UltimoItemVendido(item);
        return getRetorno(iRetorno, item);
    }

    @Override
    protected String[] getNumSerie() {
        BemaString serie = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroSerieMFD(serie);
        return getRetorno(iRetorno, serie);
    }

    @Override
    protected String[] getNumGT() {
        BemaString gt = new BemaString();
        int iRetorno = bemajava.Bematech.GrandeTotal(gt);
        return getRetorno(iRetorno, gt);
    }

    @Override
    protected String[] getNumBruto() {
        BemaString bruto = new BemaString();
        int iRetorno = bemajava.Bematech.VendaBruta(bruto);
        return getRetorno(iRetorno, bruto);
    }

    @Override
    protected String[] getNumGNF() {
        BemaString gnf = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroOperacoesNaoFiscais(gnf);
        return getRetorno(iRetorno, gnf);
    }

    @Override
    protected String[] getNumGRG() {
        BemaString grg = new BemaString();
        int iRetorno = bemajava.Bematech.ContadorRelatoriosGerenciaisMFD(grg);
        return getRetorno(iRetorno, grg);
    }

    @Override
    protected String[] getNumCDC() {
        BemaString cdc = new BemaString();
        int iRetorno = bemajava.Bematech.ContadorComprovantesCreditoMFD(cdc);
        return getRetorno(iRetorno, cdc);
    }

    @Override
    protected String[] getNumCRO() {
        BemaString cro = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroIntervencoes(cro);
        return getRetorno(iRetorno, cro);
    }

    @Override
    protected String[] getNumCRZ() {
        BemaString crz = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroReducoes(crz);
        return getRetorno(iRetorno, crz);
    }

    @Override
    protected String[] abrirRelatorio(String rel) {
        int iRetorno = bemajava.Bematech.AbreRelatorioGerencialMFD(rel);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] linhaRelatorio(String texto) {
        String[] linhas = texto.split("\\" + SL);
        for (String linha : linhas) {
            linha = linha.replaceAll("<N>", new String(new byte[]{27}) + new String(new byte[]{69})).replaceAll("</N>", new String(new byte[]{27}) + new String(new byte[]{70}));
            bemajava.Bematech.UsaRelatorioGerencialMFD(linha + ENTER);
        }
        return getRetorno(1);
    }

    @Override
    protected String[] abrirCupomVinculado(String[] params) {
        int iRetorno = bemajava.Bematech.AbreComprovanteNaoFiscalVinculadoMFD(params[1], Util.formataNumero(params[3], 1, 2, false), params[0], "", "", "");
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] linhaCupomVinculado(String texto) {
        String[] linhas = texto.split("\\" + SL);
        for (String linha : linhas) {
            bemajava.Bematech.UsaComprovanteNaoFiscalVinculado(linha + ENTER);
        }
        return getRetorno(1);
    }

    @Override
    protected String[] fecharRelatorio() {
        bemajava.Bematech.FechaRelatorioGerencial();
        bemajava.Bematech.FechaComprovanteNaoFiscalVinculado();
        return new String[]{OK, ""};
    }

    @Override
    protected String[] pularLinhas(Integer linhas) {
        for (int i = 0; i < linhas; i++) {
            bemajava.Bematech.UsaRelatorioGerencialMFD(ENTER);
        }
        return getRetorno(1);
    }

    @Override
    protected String[] abrirCupom() {
        String cpf = "";
        String nome = "";
        String end = "";
        if (identificado != null) {
            cpf = identificado[0];
            nome = identificado[1];
            end = identificado[2];
        }

        int iRetorno = bemajava.Bematech.AbreCupomMFD(cpf, nome, end);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] adicionarItem(String[] params) {
        String aliq = params[2].replace(".", ",").replace("T", "").replace("S", "");
        String qtd = Util.formataNumero(params[3], 1, 3, false);
        String valor = Util.formataNumero(params[4], 1, 3, false);
        int iRetorno = bemajava.Bematech.VendeItemDepartamento(params[0], params[1], aliq, valor, qtd, "0", "0", "01", params[6]);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] cancelarItem(String item) {
        int iRetorno = bemajava.Bematech.CancelaItemGenerico(item);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] totalizarCupom(String asc_desc) {
        String modo = asc_desc.contains("-") ? "D" : "A";
        int iRetorno = bemajava.Bematech.IniciaFechamentoCupomMFD(modo, "$", asc_desc.replace(".", ","), asc_desc.replace(".", ","));
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] efetuarPagamento(String[] params) {
        int iRetorno = bemajava.Bematech.EfetuaFormaPagamentoIndice(params[0], params[1].replace(".", ","));
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] fecharCupom() {
        int iRetorno = bemajava.Bematech.TerminaFechamentoCupom(this.observacoes);
        this.identificado = null;
        this.observacoes = "";
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] cancelarCupom() {
        String cpf = "";
        String nome = "";
        String end = "";
        if (identificado != null) {
            cpf = identificado[0];
            nome = identificado[1];
            end = identificado[2];
        }

        this.identificado = null;
        this.observacoes = "";
        int iRetorno = bemajava.Bematech.CancelaCupomMFD(cpf, nome, end);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] sangria(String valor) {
        int iRetorno = bemajava.Bematech.Sangria(Util.formataNumero(valor, 1, 2, false));
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] suprimento(String valor) {
        int iRetorno = bemajava.Bematech.Suprimento(Util.formataNumero(valor, 1, 2, false), "");
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] leituraX() {
        int iRetorno = bemajava.Bematech.LeituraX();
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] reducaoZ() {
        int iRetorno = bemajava.Bematech.ReducaoZ("", "");
        return getRetorno(iRetorno);
    }

    @Override
    public Map<String, Object> getDadosZ() {
        BemaString sRetorno = new BemaString();
        int iRetorno = bemajava.Bematech.DadosUltimaReducaoMFD(sRetorno);
        if (iRetorno == 1) {
            try {
                Map<String, Object> mapa = new HashMap<>();
                // pega os dados
                String[] dados = sRetorno.getBuffer().split(",");
                // ECF
                mapa.put("NumCRZ", Integer.valueOf(dados[2]));
                mapa.put("NumCOO", Integer.valueOf(dados[3]));
                mapa.put("NumCRO", Integer.valueOf(dados[1]));
                mapa.put("DataMovimento", new SimpleDateFormat("ddMMyy").parse(dados[36]));
                // Totalizadores
                bemajava.Bematech.VendaBruta(sRetorno);
                mapa.put("VendaBruta", Double.valueOf(sRetorno.getBuffer()) / 100.00);
                mapa.put("GrandeTotal", Double.valueOf(dados[15]) / 100.00);
                Map<String, Double> totalizadores = new HashMap<>();
                // Aliquotas
                for (int i = 0; i < 16; i++) {
                    String aliq = "T" + dados[35].substring(i * 4, i * 4 + 4);
                    if (!aliq.equals("T0000")) {
                        String trib = dados[16].substring(i * 14, i * 14 + 14);
                        if (!trib.equals("00000000000000")) {
                            totalizadores.put(aliq, Double.valueOf(trib) / 100.00);
                        }
                    } else {
                        break;
                    }
                }
                // Outros ICMS
                String[] outros = new String[]{"I1", "N1", "F1", "IS1", "NS1", "FS1", "DT", "DS", "AT", "AS", "Can-T", "Can-S"};
                for (int i = 17; i <= 28; i++) {
                    if (!dados[i].equals("00000000000000")) {
                        totalizadores.put(outros[i - 17], Double.valueOf(dados[i]) / 100.00);
                    }
                }
                // Operacao Nao Fiscal
                double valor = 0.00;
                if (!dados[30].equals("00000000000000")) {
                    valor += Double.valueOf(dados[30]) / 100.00;
                }
                if (!dados[31].equals("00000000000000")) {
                    valor += Double.valueOf(dados[31]) / 100.00;
                }
                if (valor > 0.00) {
                    totalizadores.put("OPNF", valor);
                }
                mapa.put("Totalizadores", totalizadores);
                return mapa;
            } catch (ParseException | NumberFormatException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected String[] getLMF(String tipo, String[] params) {
        int iRetorno;
        if (params[0].contains("/")) {
            iRetorno = bemajava.Bematech.LeituraMemoriaFiscalDataMFD(params[0], params[1], tipo);
        } else {
            iRetorno = bemajava.Bematech.LeituraMemoriaFiscalReducaoMFD(params[0], params[1], tipo);
        }
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] getArqMF(String path) {
        int iRetorno = bemajava.Bematech.DownloadMF(path);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] getArqMFD(String path) {
        int iRetorno = bemajava.Bematech.DownloadMFD(path, "0", "", "", "");
        return getRetorno(iRetorno);
    }
}
