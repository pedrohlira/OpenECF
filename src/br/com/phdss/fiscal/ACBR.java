package br.com.phdss.fiscal;

import br.com.phdss.EComando;
import br.com.phdss.EEstado;
import br.com.phdss.IECF;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Classe que representa o ECF via ACBR no sistema e todas suas funcionalidades.
 *
 * @author Pedro H. Lira
 */
public class ACBR implements IECF {

    private Socket acbr;
    private PrintWriter saida;
    private DataInputStream entrada;

    /**
     * Construtor padrao.
     */
    public ACBR() {
        this.acbr = null;
        this.saida = null;
        this.entrada = null;
    }

    @Override
    public void ativar() throws Exception {
        String[] resp = enviar(EComando.ECF_Ativar);
        if (ACBR.ERRO.equals(resp[0])) {
            throw new Exception(resp[1]);
        }
    }

    @Override
    public void conectar(String porta, int velocidade, int modelo) throws Exception {
        try {
            SocketAddress url = new InetSocketAddress(InetAddress.getLocalHost(), Integer.valueOf(porta));
            acbr = new Socket();
            acbr.connect(url, velocidade);
            saida = new PrintWriter(acbr.getOutputStream());
            entrada = new DataInputStream(acbr.getInputStream());
            lerDados();
        } catch (IOException ex) {
            LOG.error("Nao foi possivel se conectar ao ACBrMonitor", ex);
            throw new Exception("Verifique se as configuraõçes estão corretas e se está ativo no sistema.");
        }
    }

    @Override
    public void desativar() {
        enviar(EComando.ECF_Desativar);
    }

    @Override
    public String[] enviar(EComando comando, String... parametros) {
        return enviar(comando.toString(), parametros);
    }

    /**
     * @see #enviar(br.com.phdss.EComando, java.lang.String...)
     */
    private String[] enviar(String comando, String... parametros) {
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
            LOG.error("Nao foi possivel enviar ou receber comando ao ECF" + acao.toString(), ex);
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
    private String lerDados() {
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

    @Override
    public EEstado validarEstado() throws Exception {
        String[] resp = enviar(EComando.ECF_Estado);
        if (ACBR.OK.equals(resp[0])) {
            return EEstado.valueOf(resp[1]);
        } else {
            throw new Exception(resp[1]);
        }
    }

    @Override
    public double validarGT(double gt) throws Exception {
        String[] resp = enviar(EComando.ECF_GrandeTotal);
        if (ACBR.OK.equals(resp[0])) {
            try {
                double gt1 = Double.valueOf(resp[1].replace(",", "."));
                return gt1 != gt ? gt1 : 0.00;
            } catch (NumberFormatException ex) {
                throw new Exception(ex.getMessage());
            }
        } else {
            throw new Exception(resp[1]);
        }
    }

    @Override
    public void validarSerial(String serie) throws Exception {
        String[] resp = enviar(EComando.ECF_NumSerie);
        if (ACBR.OK.equals(resp[0])) {
            if (!serie.contains(resp[1])) {
                throw new Exception("O ECF conectado tem o Número de Série = " + resp[1]
                        + "\nO número de série do ECF autorizado deste PAF é = " + serie);
            }
        } else {
            throw new Exception(resp[1]);
        }
    }
}
