//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.12.02 at 05:28:47 PM CST 
//


package mx.gob.sat.sitio_internet.cfd.catalogos.cartaporte;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for c_TipoDeTrafico.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="c_TipoDeTrafico">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;whiteSpace value="collapse"/>
 *     &lt;enumeration value="TT01"/>
 *     &lt;enumeration value="TT02"/>
 *     &lt;enumeration value="TT03"/>
 *     &lt;enumeration value="TT04"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "c_TipoDeTrafico", namespace = "http://www.sat.gob.mx/sitio_internet/cfd/catalogos/CartaPorte")
@XmlEnum
public enum CTipoDeTrafico {

    @XmlEnumValue("TT01")
    TT_01("TT01"),
    @XmlEnumValue("TT02")
    TT_02("TT02"),
    @XmlEnumValue("TT03")
    TT_03("TT03"),
    @XmlEnumValue("TT04")
    TT_04("TT04");
    private final String value;

    CTipoDeTrafico(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CTipoDeTrafico fromValue(String v) {
        for (CTipoDeTrafico c: CTipoDeTrafico.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
