/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.printsort;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.BarcodeEANSUPP;
import com.itextpdf.text.pdf.BarcodeInter25;
import com.itextpdf.text.pdf.BarcodePostnet;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 *
 * @author jc Dauphin
 */

public
class BarCodeGenerator {
PdfContentByte contentByte;
 
public void generateBarCode() {
    /** Step 1: Create a Document*/
    Document document = new Document(PageSize.A4, 50, 50, 50, 50);
 
    try {
 
        /** Step 2: Create PDF Writer*/
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream("sanjaalDotCom_BarCode1.pdf"));
 
        /** Step 3: Open the document so that we can write over it.*/
        document.open();
 
        /** Step 4: We have to create a set of contents.*/
        contentByte = writer.getDirectContent();
 
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.getDefaultCell().setFixedHeight(70);
 
        String myText = "www.sanjaal.com"; // Text to encode
 
        table.addCell("CODE 39");
        table.addCell(new Phrase(new Chunk(createBarCode39(myText
                .toUpperCase()), 0, 0)));
 
        table.addCell("CODE 39 EXTENDED");
        table.addCell(new Phrase(new Chunk(createBarcode39Extended(myText),
                0, 0)));
 
        table.addCell("CODE 128");
        table.addCell(new Phrase(new Chunk(createBarcode128(myText), 0,
                        0)));
 
        table.addCell("CODE INTERLEAVED");
        String myTextNum = "12345";
        table.addCell(new Phrase(new Chunk(createBarcodeInter25(myTextNum),
                0, 0)));
 
        table.addCell("CODE POSTNET");
        table.addCell(new Phrase(new Chunk(createBarcodePostnet(myTextNum),
                0, 0)));
 
        table.addCell("CODE PLANET");
        table.addCell(new Phrase(new Chunk(
                createBarcodePostnetPlanet(myTextNum), 0, 0)));
 
        String myTextEAN13 = "1234567890123";
        table.addCell("CODE EAN");
        table.addCell(new Phrase(new Chunk(createBarcodeEAN(myTextEAN13),
                0, 0)));
 
        table.addCell("CODE EAN\nWITH\nSUPPLEMENTAL 5");
        table.addCell(new Phrase(new Chunk(createBARCodeEANSUPP(
                myTextEAN13, "12345"), 0, 0)));
 
        document.add(table);
    } catch (Exception de) {
        de.printStackTrace();
    }
 
    // step 5: we close the document
    document.close();
}
 
public static void main(String args[]) {
    new BarCodeGenerator().generateBarCode();
}
 
/**
 * Method to create barcode image of type Barcode39 for mytext
 */
public Image createBarCode39(String myText) {
    /**
     * Code 39 character set consists of barcode symbols representing
     * characters 0-9, A-Z, the space character and the following symbols:
     * - . $ / + %
     */
 
    Barcode39 myBarCode39 = new Barcode39();
    myBarCode39.setCode(myText);
    myBarCode39.setStartStopText(false);
    Image myBarCodeImage39 = myBarCode39.createImageWithBarcode(
            contentByte, null, null);
    return myBarCodeImage39;
}
 
/**Creating a barcode image using Barcode39 extended type for myText*/
public Image createBarcode39Extended(String myText) {
    Barcode39 myBarCode39extended = new Barcode39();
    myBarCode39extended.setCode(myText);
    myBarCode39extended.setStartStopText(false);
    myBarCode39extended.setExtended(true);
    Image myBarCodeImage39Extended = myBarCode39extended
            .createImageWithBarcode(contentByte, null, null);
    return myBarCodeImage39Extended;
}
 
/** Creating a barcode image using Barcode 128 for myText*/
public Image createBarcode128(String myText) {
    Barcode128 code128 = new Barcode128();
    code128.setCode(myText);
    Image myBarCodeImage128 = code128.createImageWithBarcode(contentByte,
            null, null);
    return myBarCodeImage128;
}
 
/** Creating a barcode image using BarcodeEAN for myText*/
public Image createBarcodeEAN(String myText) {
    BarcodeEAN myBarcodeEAN = new BarcodeEAN();
    myBarcodeEAN.setCodeType(Barcode.EAN13); // 13 characters.
    myBarcodeEAN.setCode(myText);
    Image myBarCodeImageEAN = myBarcodeEAN.createImageWithBarcode(
            contentByte, null, null);
    return myBarCodeImageEAN;
}
 
/** creating a barcode image using BarCodeInter25 for myText*/
public Image createBarcodeInter25(String myText) {
    BarcodeInter25 myBarcode25 = new BarcodeInter25();
    myBarcode25.setGenerateChecksum(true);
    myBarcode25.setCode(myText);
    Image myBarCodeImageInter25 = myBarcode25.createImageWithBarcode(
            contentByte, null, null);
    return myBarCodeImageInter25;
}
 
/**creating a barcode image using BarcodePostnet for myText*/
public Image createBarcodePostnet(String myText) {
    BarcodePostnet myBarcodePostnet = new BarcodePostnet();
    myBarcodePostnet.setCode(myText);
    Image myBarcodeImagePostnet = myBarcodePostnet.createImageWithBarcode(
            contentByte, null, null);
    return myBarcodeImagePostnet;
}
 
/** creating a barcode image using BarCodeInter25 */
public Image createBarcodePostnetPlanet(String myText) {
    BarcodePostnet myBarCodePostnetPlanet = new BarcodePostnet();
    myBarCodePostnetPlanet.setCode(myText);
    myBarCodePostnetPlanet.setCodeType(Barcode.PLANET);
    Image myBarCodeImagePostntPlanet = myBarCodePostnetPlanet
            .createImageWithBarcode(contentByte, null, null);
    return myBarCodeImagePostntPlanet;
}
 
public Image createBARCodeEANSUPP(String myTextPrimary,
        String myTextSupplementary5) {
    PdfTemplate pdfTemplate = contentByte.createTemplate(0, 0);
    BarcodeEAN myBarcodeEAN = new BarcodeEAN();
    myBarcodeEAN.setCodeType(Barcode.EAN13);
    myBarcodeEAN.setCode(myTextPrimary);
    PdfTemplate ean = myBarcodeEAN.createTemplateWithBarcode(contentByte,
            null, BaseColor.BLUE);
    BarcodeEAN codeSUPP = new BarcodeEAN();
    codeSUPP.setCodeType(Barcode.SUPP5);
    codeSUPP.setCode(myTextSupplementary5);
    codeSUPP.setBaseline(-2);
    BarcodeEANSUPP eanSupp = new BarcodeEANSUPP(myBarcodeEAN, codeSUPP);
    Image imageEANSUPP = eanSupp.createImageWithBarcode(contentByte, null,
            BaseColor.BLUE);
    return imageEANSUPP;
}
}