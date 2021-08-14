package builders

import (
	"io/ioutil"

	"encoding/json"
	"encoding/xml"
)

type FacturaCfdi struct {
	XMLName           xml.Name `xml:"cfdi:Comprobante"`
	Version           string   `xml:"Version,attr"`
	Serie             string   `xml:"Serie,attr"`
	Folio             string   `xml:"Folio,attr"`
	Fecha             string   `xml:"Fecha,attr"`
	FormaPago         string   `xml:"FormaPago,attr"`
	LugarExpedicion   string   `xml:"LugarExpedicion,attr"`
	SubTotal          string   `xml:"SubTotal,attr"`
	Total             string   `xml:"Total,attr"`
	TipoCambio        string   `xml:"TipoCambio,attr"`
	TipoDeComprobante string   `xml:"TipoDeComprobante,attr"`
	MetodoPago        string   `xml:"MetodoPago,attr"`
	Moneda            string   `xml:"Moneda,attr"`
	XmlnsXsi          string   `xml:"xmlns:xsi,attr"`
	XsiSchemaLocation string   `xml:"xsi:schemaLocation,attr"`
	XmlnsCfdi         string   `xml:"xmlns:cfdi,attr"`
}

func (self *FacturaCfdi) PopulateFromJSON(byteValue []byte) error {

	type FacturaInput struct {
		Comprobante struct {
			Emisor struct {
				Nombre        string `json:"_Nombre"`
				RegimenFiscal string `json:"_RegimenFiscal"`
				Rfc           string `json:"_Rfc"`
				Prefix        string `json:"__prefix"`
			} `json:"Emisor"`
			Receptor struct {
				Nombre  string `json:"_Nombre"`
				Rfc     string `json:"_Rfc"`
				UsoCFDI string `json:"_UsoCFDI"`
				Prefix  string `json:"__prefix"`
			} `json:"Receptor"`
			Conceptos struct {
				Concepto []struct {
					Impuestos struct {
						Traslados struct {
							Traslado struct {
								Base       string `json:"_Base"`
								Importe    string `json:"_Importe"`
								Impuesto   string `json:"_Impuesto"`
								TasaOCuota string `json:"_TasaOCuota"`
								TipoFactor string `json:"_TipoFactor"`
								Prefix     string `json:"__prefix"`
							} `json:"Traslado"`
							Prefix string `json:"__prefix"`
						} `json:"Traslados"`
						Prefix string `json:"__prefix"`
					} `json:"Impuestos"`
					Cantidad         string `json:"_Cantidad"`
					ClaveProdServ    string `json:"_ClaveProdServ"`
					ClaveUnidad      string `json:"_ClaveUnidad"`
					Descripcion      string `json:"_Descripcion"`
					Importe          string `json:"_Importe"`
					NoIdentificacion string `json:"_NoIdentificacion"`
					ValorUnitario    string `json:"_ValorUnitario"`
					Prefix           string `json:"__prefix"`
				} `json:"Concepto"`
				Prefix string `json:"__prefix"`
			} `json:"Conceptos"`
			Impuestos struct {
				Traslados struct {
					Traslado struct {
						Importe    string `json:"_Importe"`
						Impuesto   string `json:"_Impuesto"`
						TasaOCuota string `json:"_TasaOCuota"`
						TipoFactor string `json:"_TipoFactor"`
						Prefix     string `json:"__prefix"`
					} `json:"Traslado"`
					Prefix string `json:"__prefix"`
				} `json:"Traslados"`
				TotalImpuestosTrasladados string `json:"_TotalImpuestosTrasladados"`
				Prefix                    string `json:"__prefix"`
			} `json:"Impuestos"`
			XmlnsCfdi         string `json:"_xmlns:cfdi"`
			XmlnsXsi          string `json:"_xmlns:xsi"`
			Certificado       string `json:"_Certificado"`
			Fecha             string `json:"_Fecha"`
			Folio             string `json:"_Folio"`
			FormaPago         string `json:"_FormaPago"`
			LugarExpedicion   string `json:"_LugarExpedicion"`
			MetodoPago        string `json:"_MetodoPago"`
			Moneda            string `json:"_Moneda"`
			NoCertificado     string `json:"_NoCertificado"`
			Sello             string `json:"_Sello"`
			Serie             string `json:"_Serie"`
			SubTotal          string `json:"_SubTotal"`
			TipoCambio        string `json:"_TipoCambio"`
			TipoDeComprobante string `json:"_TipoDeComprobante"`
			Total             string `json:"_Total"`
			Version           string `json:"_Version"`
			XsiSchemaLocation string `json:"_xsi:schemaLocation"`
			Prefix            string `json:"__prefix"`
		} `json:"Comprobante"`
	}

	var facIn FacturaInput
	var err error

	json.Unmarshal(byteValue, &facIn)

	self.Version = "3.3"
	self.Fecha = facIn.Comprobante.Fecha
	self.Serie = facIn.Comprobante.Serie
	self.Folio = facIn.Comprobante.Folio
	self.FormaPago = facIn.Comprobante.FormaPago
	self.LugarExpedicion = facIn.Comprobante.LugarExpedicion
	self.SubTotal = facIn.Comprobante.SubTotal
	self.Total = facIn.Comprobante.Total
	self.MetodoPago = facIn.Comprobante.MetodoPago
	self.Moneda = facIn.Comprobante.Moneda
	self.TipoCambio = facIn.Comprobante.TipoCambio
	self.TipoDeComprobante = facIn.Comprobante.TipoDeComprobante
	self.XmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance"
	self.XsiSchemaLocation = "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd"
	self.XmlnsCfdi = "http://www.sat.gob.mx/cfd/3"

	return err
}
