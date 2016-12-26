package jisisgroovy

import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.IField;

def TestFunc() {

     IRecord rec = binding.getVariable("record");
     def nfields = rec.getFieldCount()
      def sbuf = '<hr /><p style="text-align:justify; font-family:arial;font-size:20px;color:Navy;fmargin-left:20px;margin-right:40px">'
      sbuf = sbuf+ 'Test Format Exit with Groovy Functions June 2013 </p> <hr />';
      sbuf = sbuf + '<b> record(<i>' +rec.getMfn() + '</i>' + ')</b><br/>\n';
      
      sbuf = sbuf + '<table>\n<tbody>\n'
      for (int i = 0; i < nfields; i++) {
         def f = rec.getFieldByIndex(i);
         def  nocc = f.getOccurrenceCount();
         def tag = f.getTag() 
         for (int j = 0; j < nocc; j++) {
            def ss =   '<tr><td>'
            ss = ss + tag 
            ss = ss + ': </td><td>' 
            ss = ss + ' &lt&lt' + '<i>'
            ss = ss + f.getOccurrence(j) 
            ss = ss + '</i>' + '&gt&gt</td></tr>\n';
            sbuf = sbuf +  ss
         }
      }
       sbuf = sbuf + '</tbody>\n</table>\n';
  return sbuf.toString();    
}
TestFunc()