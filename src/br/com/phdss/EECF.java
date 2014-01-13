package br.com.phdss;

/**
 * Enumerador que representa os tipos de ECF no sistema suportados.
 *
 * @author Pedro H. Lira
 */
public enum EECF {

    /**
     * Aplicativo que se conecta a varios modelos de impressoras.
     */
    ACBR,
    /**
     * Impressoras modelo Bematech da 2100 até 4200.
     */
    BEMATECH,
    /**
     * Todas as impressoras modelo Bematech não fiscal.
     */
    BEMATECH_NF,
    /**
     * Impressoras modelo Daruma da 600 até Mach 2.
     */
    DARUMA,
    /**
     * Todas as impressoras modelo Daruma não fiscal.
     */
    DARUMA_NF
}
