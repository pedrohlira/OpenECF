package br.com.phdss;

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
     * @param classe o texto que diz qual o tipo de ECF desejado.
     */
    public static void setInstancia(String classe) {
        try {
            Class imp = Class.forName(classe);
            ecf = (IECF) imp.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ecf = null;
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
