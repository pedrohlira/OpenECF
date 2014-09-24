package br.com.phdss.fiscal;

import bemajava.BemaInteger;
import bemajava.BemaString;
import br.com.phdss.EEstado;
import br.com.phdss.Util;
import java.io.File;

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
        BemaInteger ack = new BemaInteger();
        BemaInteger st1 = new BemaInteger();
        BemaInteger st2 = new BemaInteger();
        BemaInteger st3 = new BemaInteger();
        bemajava.Bematech.RetornoImpressoraMFD(ack, st1, st2, st3);

        EEstado estado;
        switch (st3.getNumber()) {
            case 2:
                estado = EEstado.estDesconhecido;
                break;
            case 7:
                estado = EEstado.estVenda;
                break;
            case 9:
                estado = EEstado.estRelatorio;
                break;
            case 43:
                estado = EEstado.estNaoInicializada;
                break;
            case 63:
                estado = EEstado.estBloqueada;
                break;
            case 64:
                estado = EEstado.estPagamento;
                break;
            case 65:
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
    protected String[] getNumGNF() {
        BemaString gnf = new BemaString();
        int iRetorno = bemajava.Bematech.NumeroOperacoesNaoFiscais(gnf);
        return getRetorno(iRetorno, gnf);
    }

    @Override
    protected String[] getGRG() {
        BemaString grg = new BemaString();
        int iRetorno = bemajava.Bematech.ContadorRelatoriosGerenciaisMFD(grg);
        return getRetorno(iRetorno, grg);
    }

    @Override
    protected String[] getCDC() {
        BemaString cdc = new BemaString();
        int iRetorno = bemajava.Bematech.ContadorComprovantesCreditoMFD(cdc);
        return getRetorno(iRetorno, cdc);
    }

    @Override
    protected String[] abrirRelatorio(String rel) {
        int iRetorno = bemajava.Bematech.AbreRelatorioGerencialMFD(rel);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] linhaRelatorio(String texto) {
        String[] linhas = texto.split("\\" + SL);
        StringBuilder sb = new StringBuilder();
        for (String linha : linhas) {
            if (linha.contains("<N>")) {
                linha = linha.replace("<N>", new String(new byte[]{27}) + new String(new byte[]{69})).replace("</N>", new String(new byte[]{27}) + new String(new byte[]{70}));
            }
            sb.append(linha).append(ENTER);
        }
        int iRetorno = bemajava.Bematech.UsaRelatorioGerencialMFD(sb.toString());
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] abrirCupomVinculado(String[] params) {
        int iRetorno = bemajava.Bematech.AbreComprovanteNaoFiscalVinculadoMFD(params[1], Util.formataNumero(params[3], 1, 2, false), params[0], "", "", "");
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] linhaCupomVinculado(String texto) {
        texto = texto.replaceAll("\\" + SL, ENTER);
        int iRetorno = bemajava.Bematech.UsaComprovanteNaoFiscalVinculado(texto);
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] fecharRelatorio() {
        bemajava.Bematech.FechaRelatorioGerencial();
        bemajava.Bematech.FechaComprovanteNaoFiscalVinculado();
        return new String[]{OK, ""};
    }

    @Override
    protected String[] pularLinhas(Integer linhas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linhas; i++) {
            sb.append(ENTER);
        }
        int iRetorno = bemajava.Bematech.UsaRelatorioGerencialMFD(sb.toString());
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
        int iRetorno = bemajava.Bematech.EfetuaFormaPagamentoIndiceMFD(params[0], params[1].replace(".", ","), "0", "");
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
    protected String[] getDadosZ() {
        //TODO fazer o parser dos dados para ficar que nem o ini do acbr
        BemaString z = new BemaString();
        int iRetorno = bemajava.Bematech.DadosUltimaReducaoMFD(z);
        String[] resp = z.getBuffer().split(",");
        for (String r : resp) {
            LOG.debug(r);
        }
        return getRetorno(iRetorno, z);
    }

    @Override
    protected String[] getLMF(String tipo, String[] params) {
        int iRetorno;
        if (params[2] == null) {
            if (params[0].contains("/")) {
                iRetorno = bemajava.Bematech.LeituraMemoriaFiscalDataMFD(params[0], params[1], tipo);
            } else {
                iRetorno = bemajava.Bematech.LeituraMemoriaFiscalReducaoMFD(params[0], params[1], tipo);
            }
        } else {
            if (params[0].contains("/")) {
                iRetorno = bemajava.Bematech.LeituraMemoriaFiscalSerialDataMFD(params[0], params[1], tipo);
            } else {
                iRetorno = bemajava.Bematech.LeituraMemoriaFiscalSerialReducaoMFD(params[0], params[1], tipo);
            }
            try {
                Util.assinarArquivoEAD("C:\\Retorno.txt");
                File arquivo = new File("C:\\Retorno.txt");
                arquivo.renameTo(new File(params[2]));
            } catch (Exception ex) {
                LOG.error("Problemas ao assinar ou mover o arquivo gerado.", ex);
                return new String[]{ERRO, "Problemas ao assinar ou mover o arquivo gerado."};
            }
        }
        return getRetorno(iRetorno);
    }

    @Override
    protected String[] getMFD(String tipo, String[] params) {
        int iRetorno;
        if (tipo.equals("C") || tipo.equals("Z")) {
            if (params[0].contains("/")) {
                iRetorno = bemajava.Bematech.ArquivoMFD("", params[0], params[1], "D", "01", 1, "", "", 1);
            } else {
                iRetorno = bemajava.Bematech.ArquivoMFD("", params[0], params[1], tipo, "01", 1, "", "", 1);
            }
        } else {
            if (params[0].contains("/")) {
                iRetorno = bemajava.Bematech.EspelhoMFD("", params[0], params[1], "D", "01", "", "");
            } else {
                iRetorno = bemajava.Bematech.EspelhoMFD("", params[0], params[1], "C", "01", "", "");
            }
        }
        try {
            Util.assinarArquivoEAD("C:\\Retorno.txt");
            File arquivo = new File("C:\\Retorno.txt");
            arquivo.renameTo(new File(params[2]));
        } catch (Exception ex) {
            LOG.error("Problemas ao assinar ou mover o arquivo gerado.", ex);
            return new String[]{ERRO, "Problemas ao assinar ou mover o arquivo gerado."};
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
