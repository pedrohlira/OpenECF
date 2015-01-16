package br.com.phdss.fiscal;

import br.com.daruma.jna.ECF;
import br.com.daruma.jna.UTIL;
import br.com.phdss.EEstado;
import br.com.phdss.Util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa o ECF da Daruma no sistema e todas suas
 * funcionalidades.
 *
 * @author Pedro H. Lira
 */
public class Daruma extends Impressora {

    private String[] meios;

    /**
     * Construtor padrao.
     */
    public Daruma() {
        super();
        System.loadLibrary("DarumaFramework");
    }

    /**
     * Metodo que faz o tratamento do retorno.
     *
     * @param retorno o inteiro que representado a resposta do ECF.
     * @return um Array com OK ou ERRO, mais o texto do erro.
     */
    private String[] getRetorno(int retorno) {
        return getRetorno(retorno, "");
    }

    /**
     * Metodo que faz o tratamento do retorno.
     *
     * @param retorno o inteiro que representado a resposta do ECF.
     * @param parametro um valor quando o retorno devolve um texto.
     * @return um Array com OK ou ERRO, mais o texto do erro.
     */
    private String[] getRetorno(int retorno, String parametro) {
        if (retorno == 1) {
            return new String[]{OK, parametro.trim()};
        } else {
            int[] iErro = new int[1];
            ECF.rStatusUltimoCmdInt(iErro, new int[1]);

            char[] cIOErro = new char[300];
            ECF.eInterpretarErro(iErro[0], cIOErro);

            String erro = new String(cIOErro);
            return new String[]{ERRO, erro.trim()};
        }
    }

    @Override
    public void ativar() throws Exception {
        char[] sRetorno = new char[300];
        ECF.rLerMeiosPagto(sRetorno);
        meios = new String(sRetorno).trim().split(";");
    }

    @Override
    public void conectar(String porta, int velocidade, int modelo) throws Exception {
        int resp = ECF.eBuscarPortaVelocidade();
        if (resp == 0) {
            LOG.error("Nao foi possivel se conectar ao ECF Daruma: porta = " + porta);
            throw new Exception("Verifique se as configuraõçes estão corretas e se está ativo no sistema.");
        }
    }

    @Override
    public void desativar() {
    }

    @Override
    public EEstado validarEstado() throws Exception {
        EEstado estado;
        int[] iRetorno = new int[1];
        char[] sRetorno = new char[2];

        // valida o status online
        ECF.rConsultaStatusImpressoraInt(11, iRetorno);
        if (iRetorno[0] == 1) {
            estado = EEstado.estNaoInicializada;
        } else {
            // valida se requer Z
            ECF.rConsultaStatusImpressoraInt(5, iRetorno);
            if (iRetorno[0] == 1) {
                estado = EEstado.estBloqueada;
            } else {
                // valida se exite Z pendente
                ECF.rConsultaStatusImpressoraInt(6, iRetorno);
                if (iRetorno[0] == 1) {
                    estado = EEstado.estRequerZ;
                } else {
                    // valida se tem documento aberto
                    ECF.rConsultaStatusImpressoraInt(12, iRetorno);
                    if (iRetorno[0] == 1) {
                        // valida o status de venda
                        ECF.rCFVerificarStatusInt(iRetorno);
                        if (iRetorno[0] == 3) {
                            estado = EEstado.estPagamento;
                        } else if (iRetorno[0] != 0) {
                            estado = EEstado.estVenda;
                        } else {
                            // valida o status de relatorio
                            ECF.rRGVerificarStatus(sRetorno);
                            String rResp = new String(sRetorno).trim();
                            if (rResp.equals("1")) {
                                estado = EEstado.estRelatorio;
                            } else {
                                estado = EEstado.estDesconhecido;
                            }
                        }
                    } else {
                        estado = EEstado.estLivre;
                    }
                }
            }
        }

        return estado;
    }

    @Override
    protected String[] abrirGaveta() {
        ECF.eAbrirGaveta_ECF_Daruma();
        return new String[]{OK, ""};
    }

    @Override
    protected String[] cortarPapel() {
        ECF.eAcionarGuilhotina("1");
        return new String[]{OK, ""};
    }

    @Override
    protected String[] getDataHora() {
        String[] resp = getInfo("66");
        try {
            Date data = new SimpleDateFormat("ddMMyyyyHHmmss").parse(resp[1]);
            resp[1] = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(data);
        } catch (ParseException ex) {
        }
        return resp;
    }

    @Override
    protected String[] getDataHoraSB() {
        String[] resp = getInfo("76");
        try {
            Date data = new SimpleDateFormat("ddMMyyyyHHmmss").parse(resp[1]);
            resp[1] = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(data);
        } catch (ParseException ex) {
        }
        return resp;
    }

    @Override
    protected String[] getVersao() {
        return getInfo("83");
    }

    @Override
    protected String[] getNumECF() {
        return getInfo("107");
    }

    @Override
    protected String[] getNumCCF() {
        return getInfo("30");
    }

    @Override
    protected String[] getNumCupom() {
        return getInfo("26");
    }

    @Override
    protected String[] getNumItem() {
        return getInfo("58");
    }

    @Override
    protected String[] getNumSerie() {
        return getInfo("78");
    }

    @Override
    protected String[] getNumGT() {
        String[] resp = getInfo("1");
        resp[1] = Util.formataNumero(Double.valueOf(resp[1]) / 100.00, 1, 2, false);
        return resp;
    }

    @Override
    protected String[] getNumBruto() {
        String[] resp = getInfo("1");
        double gtFim = Double.valueOf(resp[1]) / 100.00;
        resp = getInfo("2");
        double gtIni = Double.valueOf(resp[1]) / 100.00;
        resp[1] = Util.formataNumero(gtFim - gtIni, 1, 2, false);
        return resp;
    }

    @Override
    protected String[] getNumGNF() {
        return getInfo("28");
    }

    @Override
    protected String[] getNumGRG() {
        return getInfo("33");
    }

    @Override
    protected String[] getNumCDC() {
        return getInfo("45");
    }

    @Override
    protected String[] getNumCRO() {
        return getInfo("23");
    }

    @Override
    protected String[] getNumCRZ() {
        return getInfo("24");
    }

    private String[] getInfo(String indice) {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao(indice, sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] abrirRelatorio(String rel) {
        int iRetorno = ECF.iRGAbrirIndice(Integer.valueOf(rel));
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] linhaRelatorio(String texto) {
        String[] linhas = texto.split("\\" + SL);
        for (String linha : linhas) {
            ECF.iRGImprimirTexto(linha.replaceAll("N>", "b>"));
        }
        return getRetorno(1);
    }

    @Override
    protected String[] abrirCupomVinculado(String[] params) {
        int iRetorno = ECF.iCCDAbrirSimplificado(params[1], "", params[0], params[3]);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] linhaCupomVinculado(String texto) {
        texto = texto.replaceAll("\\" + SL, ENTER);
        int iRetorno = ECF.iCCDImprimirTexto(texto);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] fecharRelatorio() {
        ECF.iRGFechar_ECF_Daruma();
        ECF.iCCDFechar_ECF_Daruma();
        return new String[]{OK, ""};
    }

    @Override
    protected String[] pularLinhas(Integer linhas) {
        for (int i = 0; i < linhas; i++) {
            ECF.iRGImprimirTexto(ENTER);
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

        int iRetorno = ECF.iCFAbrir(cpf, nome, end);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] adicionarItem(String[] params) {
        String aliq = params[2].replace(".", ",");
        if (aliq.endsWith("T")) {
            aliq = "T" + aliq.replace("T", "");
        } else if (aliq.endsWith("S")) {
            aliq = "S" + aliq.replace("S", "");
        }
        int iRetorno = ECF.iCFVender(aliq, Util.formataNumero(params[3], 1, 0, false), params[4].replace(".", ","), "D$", "0", params[0], params[6], params[1]);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] cancelarItem(String item) {
        int iRetorno = ECF.iCFCancelarItem(item);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] totalizarCupom(String asc_desc) {
        String modo = asc_desc.contains("-") ? "D$" : "A$";
        int iRetorno = ECF.iCFTotalizarCupom(modo, asc_desc.replace(".", ","));
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] efetuarPagamento(String[] params) {
        int iRetorno = ECF.iCFEfetuarPagamento(meios[Integer.valueOf(params[0]) - 1], params[1].replace(".", ","), "");
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] fecharCupom() {
        int iRetorno = ECF.iCFEncerrarConfigMsg(observacoes);
        this.identificado = null;
        this.observacoes = "";
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] cancelarCupom() {
        int iRetorno = ECF.iCFCancelar_ECF_Daruma();
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] sangria(String valor) {
        int iRetorno = ECF.iSangria(valor.replace(".", ","), "");
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] suprimento(String valor) {
        int iRetorno = ECF.iSuprimento(valor.replace(".", ","), "");
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] leituraX() {
        int iRetorno = ECF.iLeituraX_ECF_Daruma();
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] reducaoZ() {
        int iRetorno = ECF.iReducaoZ("", "");
        return getRetorno(iRetorno);
    }

    @Override
    public Map<String, Object> getDadosZ() {
        char[] sRetorno = new char[1209];
        int iRetorno = ECF.rRetornarDadosReducaoZ(sRetorno);
        if (iRetorno == 1) {
            try {
                Map<String, Object> mapa = new HashMap<>();
                // pega os dados
                String[] dados = new String(sRetorno).split(";");
                // ECF
                mapa.put("NumCRZ", Integer.valueOf(dados[28]));
                mapa.put("NumCOO", Integer.valueOf(dados[30]));
                mapa.put("NumCRO", Integer.valueOf(dados[27]));
                mapa.put("DataMovimento", new SimpleDateFormat("ddMMyyyy").parse(dados[0]));
                // Totalizadores
                double gtIni = Double.valueOf(dados[2]) / 100.00;
                double gtFim = Double.valueOf(dados[1]) / 100.00;
                mapa.put("VendaBruta", gtFim - gtIni);
                mapa.put("GrandeTotal", gtFim);
                Map<String, Double> totalizadores = new HashMap<>();
                // Aliquotas
                for (int i = 0; i < 16; i++) {
                    String aliq = dados[26].substring(i * 5, i * 5 + 5);
                    if (!aliq.equals("00000")) {
                        aliq = Util.formataNumero(i + 1, 2, 0, false) + (aliq.startsWith("1") ? "T" : "S") + aliq.substring(1);
                        String trib = dados[9].substring(i * 14, i * 14 + 14);
                        if (!trib.equals("00000000000000")) {
                            totalizadores.put(aliq, Double.valueOf(trib) / 100.00);
                        }
                    } else {
                        break;
                    }
                }
                // Outros ICMS
                String[] outros = new String[]{"F1", "F2", "I1", "I2", "N1", "N2", "FS1", "FS2", "IS1", "IS2", "NS1", "NS2"};
                for (int i = 10; i <= 21; i++) {
                    if (!dados[i].equals("00000000000000")) {
                        totalizadores.put(outros[i - 10], Double.valueOf(dados[i]) / 100.00);
                    }
                }
                // Extras
                String[] extras = new String[]{"DT", "DS", "Can-T", "Can-S", "AT", "AS"};
                for (int i = 3; i <= 8; i++) {
                    if (!dados[i].equals("00000000000000")) {
                        totalizadores.put(extras[i - 3], Double.valueOf(dados[i]) / 100.00);
                    }
                }
                // Operacao Nao Fiscal
                double valor = 0.00;
                for (int i = 0; i < 20; i++) {
                    String nf = dados[22].substring(i * 14, i * 14 + 14);
                    if (!nf.equals("00000000000000")) {
                        valor += Double.valueOf(nf) / 100.00;
                    }
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
        // configura se completa ou simplificada
        UTIL.regAlterarValor("ECF\\LMFCompleta", tipo.equals("c") ? "1" : "0");
        int iRetorno = ECF.iMFLer(params[0].replace("/", ""), params[1]);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] getArqMF(String path) {
        int iRetorno = ECF.rEfetuarDownloadMF(path);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] getArqMFD(String path) {
        int iRetorno = ECF.rEfetuarDownloadMFD("DATAM", "01012000", Util.formataData(new Date(), "ddMMyyyy"), path);
        return getRetorno(iRetorno);
    }
}
