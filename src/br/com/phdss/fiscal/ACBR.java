package br.com.phdss.fiscal;

import br.com.phdss.EComando;
import br.com.phdss.EEstado;
import br.com.phdss.IECF;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.ini4j.Wini;

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
        if (OK.equals(resp[0])) {
            enviar(EComando.ECF_IdentificaPAF, "", "");
        } else {
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
        try {
            enviar(EComando.ECF_Desativar);
            acbr.close();
        } catch (IOException ex) {
            LOG.error("Nao foi possivel se desconectar ao ACBrMonitor", ex);
        }
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
                if(ERRO.equals(resp[0]) && (resp[1].contains("reset") || resp[1].contains("timeout"))){
                    resp[1] += this.reativar();
                }
            } else {
                resp[0] = OK;
                resp[1] = dados.trim();
            }
        } catch (Exception ex) {
            resp[0] = ERRO;
            resp[1] = "Nao foi possivel enviar ou receber comando ao ECF.";
            if (!comando.equals(EComando.ECF_Ativar.toString()) && !comando.equals(EComando.ECF_Desativar.toString())) {
                resp[1] += this.reativar();
            }
            LOG.error(resp[1] + acao.toString(), ex);
        }
        return resp;
    }

    private String reativar() {
        String resp;
        try {
            this.desativar();
            this.ativar();
            this.conectar("3434", 10000, 0);
            resp = "\nO sistema refez a conexão com o ACBR, tente novamente.";
        } catch (Exception e) {
            resp = "\nO sistema não consegue se conecar ao ACBR, avise o administrador.";
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
        if (OK.equals(resp[0])) {
            return EEstado.valueOf(resp[1]);
        } else {
            throw new Exception(resp[1]);
        }
    }

    @Override
    public double validarGT(double gt) throws Exception {
        String[] resp = enviar(EComando.ECF_GrandeTotal);
        if (OK.equals(resp[0])) {
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
    public boolean validarGT(int crz, int cro, double bruto) throws Exception {
        // pega os dados
        Map<String, Object> dados = getDadosZ();
        int ecfCRZ = (int) dados.get("NumCRZ");
        int ecfCRO = (int) dados.get("NumCRO");
        double ecfBruto = (double) dados.get("VendaBruta");
        return crz == ecfCRZ && cro == ecfCRO && bruto == ecfBruto;
    }

    @Override
    public void validarSerial(String serie) throws Exception {
        String[] resp = enviar(EComando.ECF_NumSerie);
        if (OK.equals(resp[0])) {
            if (!serie.contains(resp[1])) {
                throw new Exception("O ECF conectado tem o Número de Série = " + resp[1]
                        + "\nO número de série do ECF autorizado deste PAF é = " + serie);
            }
        } else {
            throw new Exception(resp[1]);
        }
    }

    @Override
    public Map<String, Object> getDadosZ() {
        try {
            Map<String, Object> mapa = new HashMap<>();
            // pega os dados
            String[] dados = enviar(EComando.ECF_DadosUltimaReducaoZ);
            InputStream stream = new ByteArrayInputStream(dados[1].replace(",", ".").getBytes("UTF-8"));
            Wini ini = new Wini(stream);
            // ECF
            mapa.put("NumCRZ", ini.get("ECF", "NumCRZ", int.class));
            mapa.put("NumCOO", ini.get("ECF", "NumCOO", int.class));
            mapa.put("NumCRO", ini.get("ECF", "NumCRO", int.class));
            String movimento = ini.get("ECF", "DataMovimento");
            mapa.put("DataMovimento", movimento.equals("00/00/00") ? new Date() : new SimpleDateFormat("dd/MM/yy").parse(movimento));
            // Totalizadores
            mapa.put("VendaBruta", ini.get("Totalizadores", "VendaBruta", double.class));
            mapa.put("GrandeTotal", ini.get("Totalizadores", "GrandeTotal", double.class));
            Map<String, Double> totalizadores = new HashMap<>();
            // Aliquotas
            Map<String, String> aliq = ini.get("Aliquotas");
            double valor;
            for (String chave : aliq.keySet()) {
                valor = Double.valueOf(aliq.get(chave));
                if (valor > 0.00 && !totalizadores.containsKey(chave)) {
                    totalizadores.put(chave, valor);
                }
            }
            // Outros ICMS
            aliq = ini.get("OutrasICMS");
            for (String chave : aliq.keySet()) {
                // valida qual o tipo
                String codigo = "";
                if (chave.contains("Substituicao")) {
                    codigo = "F";
                } else if (chave.contains("NaoTributado")) {
                    codigo = "N";
                } else if (chave.contains("Isencao")) {
                    codigo = "I";
                }
                // se achou um tipo valido adiciona
                if (!codigo.equals("")) {
                    valor = Double.valueOf(aliq.get(chave));
                    codigo += chave.contains("ISSQN") ? "S1" : "1";
                    if (valor > 0.00 && !totalizadores.containsKey(codigo)) {
                        totalizadores.put(codigo, valor);
                    }
                }
            }
            // Operacao Nao Fiscal
            valor = ini.get("Totalizadores", "TotalNaoFiscal", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("OPNF")) {
                totalizadores.put("OPNF", valor);
            }
            // Descontos
            valor = ini.get("Totalizadores", "TotalDescontos", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("DT")) {
                totalizadores.put("DT", valor);
            }
            valor = ini.get("Totalizadores", "TotalDescontosISSQN", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("DS")) {
                totalizadores.put("DS", valor);
            }
            // Acrescimos
            valor = ini.get("Totalizadores", "TotalAcrescimos", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("AT")) {
                totalizadores.put("AT", valor);
            }
            valor = ini.get("Totalizadores", "TotalAcrescimosISSQN", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("AS")) {
                totalizadores.put("AS", valor);
            }
            // Cancelamentos
            valor = ini.get("Totalizadores", "TotalCancelamentos", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("Can-T")) {
                totalizadores.put("Can-T", valor);
            }
            valor = ini.get("Totalizadores", "TotalCancelamentosISSQN", double.class);
            if (valor > 0.00 && !totalizadores.containsKey("Can-S")) {
                totalizadores.put("Can-S", valor);
            }
            mapa.put("Totalizadores", totalizadores);
            return mapa;
        } catch (IOException | ParseException | NumberFormatException ex) {
            return null;
        }
    }
}
