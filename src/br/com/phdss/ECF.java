package br.com.phdss;

import br.com.phdss.fiscal.ACBR;
import br.com.phdss.fiscal.Bematech;
import br.com.phdss.fiscal.Daruma;
import br.com.phdss.naofiscal.BematechNF;
import br.com.phdss.naofiscal.DarumaNF;

/**
 * Classe que abstrai a criação do objet que acessa o ECF para impressão.
 *
 * @author Pedro H. Lira
 */
public class ECF {

    private static IECF ecf = null;

    /**
     * Construtor padrao.
     */
    private ECF() {
    }

    /**
     * Metodo que seta o objeto de ECF de acordo com o configurado.
     *
     * @param tipo o texto que diz qual o tipo de ECF desejado.
     */
    public static void setInstancia(String tipo) {
        ECF.setInstancia(EECF.valueOf(tipo));
    }

    /**
     * Metodo que seta o objeto de ECF de acordo com o configurado.
     *
     * @param tipo o enum que diz qual o tipo de ECF desejado.
     */
    public static void setInstancia(EECF tipo) {
        switch (tipo) {
            case BEMATECH:
                ecf = new Bematech();
                break;
            case BEMATECH_NF:
                ecf = new BematechNF();
                break;
            case DARUMA:
                ecf = new Daruma();
                break;
            case DARUMA_NF:
                ecf = new DarumaNF();
                break;
            default:
                ecf = new ACBR();
                break;
        }
    }

    /**
     * Metodo que constroi um objeto do tipo IECF e devolve para o solicitante.
     *
     * @return o objeto instaciado solicitado.
     */
    public static IECF getInstancia() {
        return ecf;
    }
}
