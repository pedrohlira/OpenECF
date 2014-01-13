package br.com.phdss.fiscal;

import br.com.daruma.jna.ECF;
import br.com.daruma.jna.UTIL;
import br.com.phdss.EEstado;
import static br.com.phdss.IECF.ENTER;
import static br.com.phdss.IECF.ERRO;
import static br.com.phdss.IECF.LOG;
import static br.com.phdss.IECF.SL;

/**
 * Classe que representa o ECF da Daruma no sistema e todas suas
 * funcionalidiades.
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
        System.loadLibrary("DarumaFramwork");
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
            return new String[]{OK, parametro};
        } else {
            int[] iErro = new int[1];
            ECF.rStatusUltimoCmdInt(iErro, new int[1]);

            char[] cIOErro = new char[300];
            ECF.eInterpretarErro(iErro[0], cIOErro);

            String erro = new String(cIOErro).trim();
            return new String[]{ERRO, erro};
        }
    }

    @Override
    public void ativar() throws Exception {
        char[] sRetorno = new char[300];
        ECF.rLerMeiosPagto(sRetorno);
        meios = new String(sRetorno).split(",");
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("66", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getDataHoraSB() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("76", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getVersao() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("83", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumECF() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("107", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumCCF() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("30", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumCupom() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("26", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumItem() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("58", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumSerie() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("78", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumGT() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("1", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getNumGNF() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("28", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getGRG() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("33", sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getCDC() {
        char[] sRetorno = new char[30];
        int iRetorno = ECF.rRetornarInformacao("45", sRetorno);
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
        StringBuilder sb = new StringBuilder();
        for (String linha : linhas) {
            if (linha.contains("<N>")) {
                linha = linha.replace("<N>", "<b>").replace("</N>", "</b>");
            }
            sb.append(linha).append(ENTER);
        }
        int iRetorno = ECF.iRGImprimirTexto(texto);
        return getRetorno(iRetorno);
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linhas; i++) {
            sb.append(ENTER);
        }
        int iRetorno = ECF.iRGImprimirTexto(sb.toString());
        return getRetorno(iRetorno);
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
        int iRetorno = ECF.iCFVender(aliq, params[3], params[4].replace(".", ","), "D$", "0", params[0], params[6], params[1]);
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
        int iRetorno = ECF.iCFEfetuarPagamento(meios[Integer.valueOf(params[0]) - 1], params[1].replace(".", ","), params[2]);
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
    protected String[] getDadosZ() {
        //TODO fazer o parser dos dados para ficar que nem o ini do acbr
        char[] sRetorno = new char[3000];
        int iRetorno = ECF.rRetornarDadosReducaoZ(sRetorno);
        return getRetorno(iRetorno, new String(sRetorno));
    }

    @Override
    protected String[] getLMF(String tipo, String[] params) {
        // configura se completa ou simplificada
        UTIL.regAlterarValor("ECF\\LMFCompleta", tipo.equals("c") ? "1" : "0");
        int iRetorno;
        if (params[2] == null) {
            iRetorno = ECF.iMFLer(params[0].replace("/", ""), params[1]);
        } else {
            iRetorno = ECF.iMFLerSerial(params[0].replace("/", ""), params[1]);
            //TODO identificar onde e qual nome gerado do arquivo e copiar pra o lugar do param[2]
        }
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] getMFD(String tipo, String[] params) {
        int iRetorno;
        if (tipo.equals("C") || tipo.equals("Z")) {
            if (params[0].contains("/")) {
                iRetorno = ECF.rGerarMFD("DATAM", params[0], params[1]);
            } else {
                iRetorno = ECF.rGerarMFD(tipo.equals("C") ? "COO" : "CRZ", params[0], params[1]);
            }
        } else {
            if (params[0].contains("/")) {
                iRetorno = ECF.rGerarEspelhoMFD("DATAM", params[0], params[1]);
            } else {
                iRetorno = ECF.rGerarEspelhoMFD("COO", params[0], params[1]);
            }
        }
        //TODO identificar onde e qual nome gerado do arquivo e copiar pra o lugar do param[2]
        return getRetorno(iRetorno);
    }

}
