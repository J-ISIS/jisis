/*
 * PrintFormatter.java
 *
 * Created on 11 settembre 2007, 10.00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.jisiscore.client;

import java.util.List;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.jisisutils.proxy.GuiGlobal;



/**
 *
 * @author triverio
 */
public class PrintFormatter {
    
    /** Creates a new instance of PrintFormatter */
    public PrintFormatter() {
    }
    
    
    String header = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>Export from Jisis</title></head><body>";
    String footer = "</body></html>";
    
    public String getHtmlFormat(String mfn, String text){
        String toPrint = header + "MFN: " + mfn  + " <br /><pre>" + text +"</pre><br />" + footer;
        return toPrint;
    }

    public String getHtmlFormat(IDatabase db, List recs, String format) {

        // Parse format and build imtermediate language
        ISISFormatter formatter = ISISFormatter.getFormatter(format);
        if (formatter == null) {
            GuiGlobal.output(ISISFormatter.getParsingError());
            return "**Format Error**";
        } else if (formatter.hasParsingError()) {
            GuiGlobal.output(ISISFormatter.getParsingError());
            return "**Format Error**";
        }

        String toPrint = header;
        for (int i = 0; i < recs.size(); i++) {
            IRecord rec = (IRecord) recs.get(i);
            formatter.setRecord(db, rec);
            formatter.eval();
            toPrint += "<b>MFN: " + rec.getMfn() + "</b><br /><pre>" + formatter.getText() + "</pre><br /><br />";
        }
        toPrint += footer;
        return toPrint;
    }
//
//    public String getHtmlFormat(long[] mfns, String string) {
//        String toPrint = header;
//        for (int i = 0; i < new Long(mfns.length).intValue(); i++){
//            IRecord rec = db.getRecord(mfns[i]);
//            ISISFormatter formatter = ISISFormatter.getFormatter(format);
//            formatter.setRecord(rec);
//            formatter.eval();
//            toPrint += "<b>MFN: " + rec.getMfn() + "</b><br /><pre>" + formatter.getText() +"</pre><br /><br />";
//        }
//        toPrint += footer;
//        return toPrint;
//    }
}
