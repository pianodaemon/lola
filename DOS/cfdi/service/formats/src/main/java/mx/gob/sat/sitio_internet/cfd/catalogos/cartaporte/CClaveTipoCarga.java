//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.09.06 at 01:43:49 PM CDT 
//


package mx.gob.sat.sitio_internet.cfd.catalogos.cartaporte;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for c_ClaveTipoCarga.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="c_ClaveTipoCarga"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;whiteSpace value="collapse"/&gt;
 *     &lt;enumeration value="CGS"/&gt;
 *     &lt;enumeration value="CGC"/&gt;
 *     &lt;enumeration value="GMN"/&gt;
 *     &lt;enumeration value="GAG"/&gt;
 *     &lt;enumeration value="OFL"/&gt;
 *     &lt;enumeration value="PYD"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "c_ClaveTipoCarga", namespace = "http://www.sat.gob.mx/sitio_internet/cfd/catalogos/CartaPorte")
@XmlEnum
public enum CClaveTipoCarga {

    CGS,
    CGC,
    GMN,
    GAG,
    OFL,
    PYD;

    public String value() {
        return name();
    }

    public static CClaveTipoCarga fromValue(String v) {
        return valueOf(v);
    }

}