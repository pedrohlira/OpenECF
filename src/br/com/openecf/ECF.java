package br.com.openecf;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import org.apache.log4j.Logger;

/**
 * Classe que representa o ECF no sistema e todas suas funcionalidiades.
 *
 * @author Pedro H. Lira
 */
public final class ECF {

    /**
     * Numero de colunas.
     */
    public static int COL;
    /**
     * Linha Simples [---].
     */
    public static String LS;
    /**
     * Linha Dupla [===].
     */
    public static String LD;
    /**
     * Separador de linhas
     */
    public static final String SL = "|";
    /**
     * Expressao OK.
     */
    public static final String OK = "OK";
    /**
     * Expressao ERRO.
     */
    public static final String ERRO = "ERRO";
    private static Logger log;
    private static Socket acbr;
    private static PrintWriter saida = null;
    private static DataInputStream entrada = null;

    /**
     * Construtor padrao.
     */
    private ECF() {
    }

    /**
     * Metodo que realiza a conexao com o ECF.
     *
     * @param servidor a URL ou IP do servidor.
     * @param porta o numero da porta de comunicacao. milisegundos.
     * @throws Exception dispara um excecao caso nao cosiga.
     */
    public static void conectar(String servidor, int porta) throws Exception {
        log = Logger.getLogger(ECF.class);

        try {
            InetAddress ip = InetAddress.getByName(servidor);
            SocketAddress url = new InetSocketAddress(ip, porta);
            acbr = new Socket();
            acbr.connect(url, 10000);
            saida = new PrintWriter(acbr.getOutputStream());
            entrada = new DataInputStream(acbr.getInputStream());

            lerDados();
        } catch (IOException ex) {
            log.error("Nao foi possivel se conectar ao ACBrMonitor", ex);
            throw new Exception("Verifique se as configuraõçes estão corretas e se está ativo no sistema.");
        }
    }

    /**
     * Metodo que envia um comando ao ACBr que repassa para a ECF.
     *
     * @param comando um EComandoECF que representa um comando aceito.
     * @param parametros um sequencia de parametros, opcionais usado somente em
     * alguns comandos.
     */
    public static String[] enviar(EComandoECF comando, String... parametros) {
        return enviar(comando.toString(), parametros);
    }

    /**
     * Metodo que envia um comando ao ECF que repassa para a ECF.
     *
     * @param comando uma String que representa um comando aceito.
     * @param parametros um sequencia de parametros, opcionais usado somente em
     * alguns comandos.
     */
    private static String[] enviar(String comando, String... parametros) {
        String[] resp = new String[]{"", ""};
        StringBuilder acao = new StringBuilder(comando);

        if (parametros != null && parametros.length > 0) {
            acao.append("(");
            for (String param : parametros) {
                acao.append(param).append(",");
            }
            acao.deleteCharAt(acao.length() - 1).append(")");
        }

        try {
            saida.print(acao.toString() + "\r\n.\r\n");
            saida.flush();

            String dados = lerDados();
            if ("".equals(dados)) {
                resp[0] = OK;
            } else if (dados.contains(":")) {
                String[] ret = dados.split(":", 2);
                resp[0] = ret[0].trim();
                resp[1] = ret[1].trim();
            } else {
                resp[0] = OK;
                resp[1] = dados.trim();
            }
        } catch (Exception ex) {
            log.error("Nao foi possivel enviar ou receber comando ao ECF" + acao.toString(), ex);
            resp[0] = ERRO;
            resp[1] = "Nao foi possivel enviar ou receber comando ao ECF";
        }
        return resp;
    }

    /**
     * Metodo que faz a leitura do retorno do ECF.
     *
     * @return uma String da resposta.
     */
    private static String lerDados() {
        StringBuilder sb = new StringBuilder();
        try {
            byte b;
            while ((b = (byte) entrada.read()) != 3) {
                sb.append(new String(new byte[]{b}));
            }
            return sb.toString();
        } catch (IOException ex) {
            return ERRO + ":" + ex.getMessage();
        }
    }

    /**
     * Metodo que faz a validacao se o sistema consegue ativar a ECF.
     *
     * @throws Exception dispara uma excecao caso nao consiga ativar.
     */
    public static void ativar() throws Exception {
        String[] resp = enviar(EComandoECF.ECF_Ativar);
        if (ECF.ERRO.equals(resp[0])) {
            throw new Exception(resp[1]);
        }
        // pega as colunas e forma as linhas
        resp = enviar(EComandoECF.ECF_Colunas);
        ECF.COL = ECF.OK.equals(resp[0]) ? Integer.valueOf(resp[1]) : 48;
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < ECF.COL; i++) {
            sb1.append("-");
            sb2.append("=");
        }

        ECF.LS = sb1.toString();
        ECF.LD = sb2.toString();
    }

    /**
     * Metodo que desativa o acesso ao ECF
     */
    public static void desativar() {
        ECF.enviar(EComandoECF.ECF_Desativar);
        ECF.enviar(EComandoECF.ECF_CorrigeEstadoErro);
    }

    /**
     * Metodo que retorna o estado do ECF.
     *
     * @return o tipo do estado do ECF.
     * @throws OpenPdvException dispara uma excecao caso nao consiga executar.
     */
    public static EEstadoECF validarEstado() throws Exception {
        String[] resp = enviar(EComandoECF.ECF_Estado);
        if (ECF.OK.equals(resp[0])) {
            return EEstadoECF.valueOf(resp[1]);
        } else {
            throw new Exception(resp[1]);
        }
    }

    /**
     * Metodo que faz a validacao se o ECF conectado e o autorizado pelo
     * sistema.
     *
     * @param serie o numero de serie do ECF registrado no arquivo do PAF.
     * @throws OpenPdvException dispara uma excecao caso nao seja o ECF
     * esperado.
     */
    public static void validarSerial(String serie) throws Exception {
        String[] resp = enviar(EComandoECF.ECF_NumSerie);
        if (ECF.OK.equals(resp[0])) {
            if (!serie.contains(resp[1])) {
                throw new Exception("O ECF conectado tem o Número de Série = " + resp[1]
                        + "\nO número de série do ECF autorizado deste PAF é = " + serie);
            }
        } else {
            throw new Exception(resp[1]);
        }
    }

    /**
     * Metodo que faz a validacao se o ultimo numero do GT e o mesmo do ECF,
     * caso nao seja fara as tentativas de arrumar.
     *
     * @param gt o valor do grande total registrado no arquivo do PAF.
     * @param cro o valor do CRO registrado no arquivo do PAF.
     * @return retorna o valor do GT novo caso o cro seja adicionado, ou zero se
     * tudo ok.
     * @throws OpenPdvException dispara uma excecao caso nao consiga executar.
     */
    public static double validarGT(double gt, int cro) throws Exception {
        String[] resp = enviar(EComandoECF.ECF_GrandeTotal);
        if (ECF.OK.equals(resp[0])) {
            try {
                double gt1 = Double.valueOf(resp[1].replace(",", "."));

                // se diferente faz novas analises
                if (gt1 != gt) {
                    resp = enviar(EComandoECF.ECF_NumCRO);
                    if (ECF.OK.equals(resp[0])) {
                        try {
                            int cro1 = Integer.valueOf(resp[1]);

                            // se houve incremento do cro, somente recompoen o GT no arquivo
                            if (cro1 > cro) {
                                return gt1;
                            } else {
                                throw new Exception("O ECF conectado tem o Grande Total = " + gt1 + "\nO arquivo criptografado tem o Grande Total = " + gt);
                            }
                        } catch (Exception ex) {
                            throw new Exception(ex.getMessage());
                        }
                    } else {
                        throw new Exception(resp[1]);
                    }
                } else {
                    return 0;
                }
            } catch (Exception ex) {
                throw new Exception(ex.getMessage());
            }
        } else {
            throw new Exception(resp[1]);
        }
    }
}
