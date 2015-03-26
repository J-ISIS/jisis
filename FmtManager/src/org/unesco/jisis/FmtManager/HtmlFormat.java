/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.FmtManager;

import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.util.StringUtils;


/**
 *
 * @author jc_dauphin
 */
public class HtmlFormat {
   private final static String htmlField    =
      "if p(%v0) then \"<I>%name </I>(%0): \",%v0,'<BR>'/fi,\n";
   private final static String htmlRepField =
      "if p(%v0) then \"<I>%name </I>(%0): \",%v0+|; |'<BR>',fi,/\n";

   private final static String htmlTableField    =
     "if p(%v0) then '<TR><TD WIDTH=\"30%\"><I>%name </I>(%0)</TD><TD>',%v0,'</TD></TR>'/fi,\n";
   private final static String htmlTableRepField =
      "if p(%v0) then '<TR><TD WIDTH=\"30%\"><I>USE </I>(%0)</TD><TD>',%v0+|; |'</TD></TR>',fi,/\n";
   public static String normalHtmlFormat(FieldDefinitionTable fdt) {
      if (fdt == null) {
         return null;
      }

      StringBuffer stringBuffer = new StringBuffer();

      stringBuffer.append("mhl,\n");

      int fieldCount = fdt.getFieldsCount();

      for (int i = 0; i < fieldCount; i++) {
         FieldDefinition fd   = fdt.getFieldByIndex(i);
         String          name = fd.getName();
         /* replace apostrophes by escaped apostrophe */
         name = StringUtils.fastReplaceAll(name, "'", "\\\\'");
         String          vt   = "v" + fd.getTag();
         String          s    = (fd.isRepeatable())
                                ? new String(htmlRepField)
                                : new String(htmlField);

         s = s.replaceAll("%v0", vt);
         s = s.replaceAll("%name", name);
         s = s.replaceAll("%0", "" + (i + 1));
         stringBuffer.append(s);
      }
      
      return stringBuffer.toString();
   }
   public static String tableHtmlFormat(FieldDefinitionTable fdt) {
      if (fdt == null) {
         return null;
      }

      StringBuffer stringBuffer = new StringBuffer();

      stringBuffer.append(" mhl,'<TABLE WIDTH=\"100%\" BORDER=\"0\" bgcolor=\"#aabbcc\">'"
              + "mhl,'<TR><TD WIDTH=\"100%\"><strong>Record N. ',mfn(9),"
              + "'</strong></TD></TR></TABLE>'"
              + "mhl,'<TABLE WIDTH=\"100%\" BORDER bgcolor=\"#c9d7f1\">'\n");

      int fieldCount = fdt.getFieldsCount();

      for (int i = 0; i < fieldCount; i++) {
         FieldDefinition fd   = fdt.getFieldByIndex(i);
         String          name = fd.getName();
         name = StringUtils.fastReplaceAll(name, "'", "\\\\'");
         
         String          vt   = "v" + fd.getTag();
         String          s    = (fd.isRepeatable())
                                ? new String(htmlTableRepField)
                                : new String(htmlTableField);

        
         s = s.replaceAll("%v0", vt);
         s = s.replaceAll("%name", name);
         s = s.replaceAll("%0", "" + (i + 1));
         stringBuffer.append(s);
      }
      stringBuffer.append(",'</TABLE>'");

      return stringBuffer.toString();
   }
   
   

}
