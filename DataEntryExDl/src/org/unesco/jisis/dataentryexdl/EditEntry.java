/*
 * EditEntry.java
 *
 * Created on July 12, 2006, 8:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.MSOffice;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.DefaultParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.apache.tika.sax.XHTMLContentHandler;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.unesco.jisis.corelib.common.FieldDefinitionTable;
import org.unesco.jisis.corelib.common.FieldDefinitionTable.FieldDefinition;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.common.WorksheetDef;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.corelib.exceptions.FormattingException;
import org.unesco.jisis.corelib.exceptions.GeneralDatabaseException;
import org.unesco.jisis.corelib.pft.ISISFormatter;
import org.unesco.jisis.corelib.picklist.PickListData;
import org.unesco.jisis.corelib.picklist.ValidationData;
import org.unesco.jisis.corelib.record.IField;
import org.unesco.jisis.corelib.record.IRecord;
import org.unesco.jisis.corelib.record.Record;
import org.unesco.jisis.jisisutil.history.ListSelectorDialog;
import org.unesco.jisis.jisiscore.client.ClientDatabaseProxy;
import org.unesco.jisis.jisiscore.client.GuiGlobal;
import org.unesco.jisis.jisisutils.threads.GuiExecutor;
import org.unesco.jisis.jisisutils.threads.IdeCursor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * An EditEntry Panel contains all data entry occurrences of a particular field
 *
 * @author rustam
 * 
 * Focus events are fired whenever a component gains or loses the keyboard focus.
 * This is true whether the change in focus occurs through the mouse, the 
 * keyboard, or programmatically.
 * 
 * An EditEntry instance is a FocusListener on the RepeatableField objects
 */
public class EditEntry extends JPanel implements ActionListener, FocusListener, OccurrenceEditEvent {

   static final String ADD_PATH = "org/unesco/jisis/dataentryexdl/plus.gif";
   static final String DEL_PATH = "org/unesco/jisis/dataentryexdl/minus.gif";
   static final String PICK_LIST_PATH = "org/unesco/jisis/dataentryexdl/PickList.png";
   static final String GET_FILE_CONTENT = "org/unesco/jisis/dataentryexdl/get-file-content-16x16.png";
   private IField field_ = null;
   private WorksheetDef.WorksheetField wksFld_ = null;
   private WorksheetDef wksDef_ = null;
   private IRecord record_ = null;
   private IDatabase db_ = null;
   private FieldDefinitionTable fdt_ = null;
   private Object fieldValue_ = null;
   private List<RepeatableField> fieldEntries_ = null;
   private List<PickListData> pickListDataList_;
   private List<ValidationData> validationDataList_;
   private PickListData pickListData_ = null;
   private ComponentOrientation orientation_ = ComponentOrientation.LEFT_TO_RIGHT;
   public static Logger logger = null;
   final ExecutorService executorService = Executors.newSingleThreadExecutor();
   final static int FIRST_FIELD_OCCURRENCE_INDEX = 0; // Where we store the text for DOC fields
   final static int SECOND_FIELD_OCCURRENCE_INDEX = 1; // Where we store the url for DOC fields

   final static int DEFAULT_HEIGHT_LINES = 8;

   

   static {
      logger = Logger.getRootLogger();
      logger.setLevel(Level.OFF);

   }
   /**
    * Captures requested embedded images
    */
   private final ImageSavingParser imageParser = null;
   private ClientDatabaseProxy dbp_;  //?
   private boolean recordModified = false;
   private UndoRedo.Manager undoRedoManager_;
   private OccurrenceEditEvent occurrenceEditEvent_;
   
   private FontSize fontSize_;
   
   /**
    * Creates a new instance of EditEntry
    * @param occurrenceEditEvent
    * @param wd
    * @param wf
    * @param field
    * @param pickListDataList
    * @param validationData
    * @param rec
    * @param db
    * @param fdt
    * @param undoRedoManager
    * @param fontSize
    */
    public EditEntry(OccurrenceEditEvent occurrenceEditEvent, // Call back EditPanel refrence
        WorksheetDef wd, 
        WorksheetDef.WorksheetField wf, 
        IField field,
        List<PickListData> pickListDataList, 
        List<ValidationData> validationData,
        IRecord rec,
        IDatabase db, 
        FieldDefinitionTable fdt, 
        UndoRedo.Manager undoRedoManager,
        FontSize fontSize) {
        
        occurrenceEditEvent_ = occurrenceEditEvent;
        field_ = field;
        wksFld_ = wf;
        wksDef_ = wd;
        pickListDataList_ = pickListDataList;
        validationDataList_ = validationData;
        record_ = rec;
        db_ = db;
        fdt_ = fdt;
        //FieldFactory.setDatabase(db);
        fieldEntries_ = new ArrayList<RepeatableField>();
        recordModified = false;
             
       pickListData_ = null;
       for (PickListData pickList : pickListDataList_) {
          if (Integer.valueOf(pickList.getTag()) == wksFld_.getTag() &&
                  (pickList.getSubfieldCode() == null || "".equals(pickList.getSubfieldCode())) ) {
             pickListData_ = pickList;
             break;
          }
       }

        dbp_ = (ClientDatabaseProxy) db;
        undoRedoManager_ = undoRedoManager;
        fontSize_ = fontSize;
        redraw();
        EditEntry.this.setUnModified();

    }

   
    
    class TreeViewAction extends AbstractAction {
       
       private DataEntryTopComponent topComponent_;
       private IDatabase db_;
       private WorksheetDef.WorksheetField wksField_;
       private List<PickListData> pickListDataList_;
       private List<ValidationData> validationDataList_;
           
       public TreeViewAction(DataEntryTopComponent topComponent,
           IDatabase db,
           WorksheetDef.WorksheetField wksField,
           List<PickListData> pickListDataList,
           List<ValidationData> validationDataList)  {
          
           topComponent_ = topComponent;
           db_ = db;
           wksField_ = wksField;
           pickListDataList_ = pickListDataList;
           validationDataList_ = validationDataList;
          
       }
       
        @Override
         public void actionPerformed(ActionEvent ae) {
            /**
             * Get the field occurrence from where the TreeView action was triggered
             */
            RepeatableField repeatableField = (RepeatableField) ae.getSource();
            
            
            FieldDialog dlg = new FieldDialog(topComponent_,
                    db_,
                    wksField_,
                    record_,
                    pickListDataList_,
                    validationDataList_);
            dlg.setResizable(true);
            dlg.setTitle(wksField_.getDescription());
            dlg.setModal(true);
            dlg.setLocationRelativeTo(null);
            dlg.setVisible(true);
            
            
           
            if (dlg.getDialogStatus() == FieldDialog.CANCEL ||
                    dlg.getDialogStatus() == FieldDialog.ABORT) {
               dlg.setVisible(false);
               return;
            }
            Object fieldValue = dlg.getFieldValue();
          try {
             /**
              * If fieldValue is a string, the occurrences are separated by the 
              * occurrence delimiters
              */
             if (fieldValue instanceof String) {
                field_.setFieldValue(fieldValue);
             } else {
                /**
                 * In case it is a BLOB field, we get the occurrences into an
                 * array, thus we must add occurrence by occurrence
                 */
                ArrayList<Object> blobArray = (ArrayList<Object>) (fieldValue);
                field_.clear();
                for (int i = 0; i < blobArray.size(); i++) {
                   field_.setOccurrence(i, blobArray.get(i));
                }
                
             }
          } catch (DbException ex) {
             Exceptions.printStackTrace(ex);
          }
          redraw();
           if (fontSize_.getFontSize() != DataEntryTopComponent.DEFAULT_FONT_SIZE) {
              changeFontSize(fontSize_.getFontSize());
           }
            

         }
    }

   
   private String doExtractText(final File f) {

      final TextExtractSwingWorker worker = new TextExtractSwingWorker(f);

      String text = "";
      // set status text
      final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Extracting text from document, please wait...");
      RepaintManager.currentManager(mainWin).paintDirtyRegions();
      try {
         // Start the worker. Note that control is 
         // returned immediately  
         worker.execute();
         text = worker.get();
      } catch (InterruptedException ex) {
         Exceptions.printStackTrace(ex);
      } catch (ExecutionException ex) {
         Exceptions.printStackTrace(ex);
      }
      
      GuiExecutor.instance().execute(new Runnable() {
         @Override
         public void run() {
            // set status text 
            StatusDisplayer.getDefault().setStatusText("");
            RepaintManager.currentManager(mainWin).paintDirtyRegions();

         }
      });

      return text;
   }
   
    private Metadata doExtractMetadata(final File f) {

      final MetadataExtractSwingWorker worker = new MetadataExtractSwingWorker(f);

      Metadata metadata = null;
      // set status text
      final JFrame mainWin = (JFrame) WindowManager.getDefault().getMainWindow();
      StatusDisplayer.getDefault().setStatusText("Extracting text from document, please wait...");
      RepaintManager.currentManager(mainWin).paintDirtyRegions();
      try {
         // Start the worker. Note that control is 
         // returned immediately  
         worker.execute();
         metadata = worker.get();
      } catch (InterruptedException ex) {
         Exceptions.printStackTrace(ex);
      } catch (ExecutionException ex) {
         Exceptions.printStackTrace(ex);
      }
      
      GuiExecutor.instance().execute(new Runnable() {
         @Override
         public void run() {
            // set status text 
            StatusDisplayer.getDefault().setStatusText("");
            RepaintManager.currentManager(mainWin).paintDirtyRegions();

         }
      });

      return metadata;
   }
   
    private void delOccurrence(ActionEvent e) {
        try {
            DeleteButton de = (DeleteButton) e.getSource();
            if (field_.getOccurrenceCount() > 0) {
                field_.removeOccurrence(de.getID());
                recordModified = true;
                redraw();
            }
        } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
        }
        GuiExecutor.instance().execute(new Runnable() {
            @Override
            public void run() {
                EditEntry.this.applyComponentOrientation(orientation_);
            }
        });
    }
    private void addOccurrence() {
        try {
            if (field_.getType() == Global.FIELD_TYPE_BLOB) {
                RepeatableField rp = new RepeatableField(this, 0);
                Object value = rp.getValue();
                field_.setOccurrence(field_.getOccurrenceCount(), (byte[]) value);
            } else {
                String val = "";
                if (wksFld_.getDefaultValue() != null) {
                    String defaultValue = wksFld_.getDefaultValue();
                    // Execute the PFT
                    String value = fieldDefaultValue(defaultValue);
                    if (value != null) {
                        val = value;
                    }
                }

                field_.setOccurrence(field_.getOccurrenceCount(), val);
            }
            GuiExecutor.instance().execute(new Runnable() {
                @Override
                public void run() {
                    recordModified = true;
                    EditEntry.this.redraw();
                    EditEntry.this.applyComponentOrientation(orientation_);
                }
            });
        } catch (DbException ex) {
            new GeneralDatabaseException(ex).displayWarning();
        }

    }
    private void addFromPicklist(ActionEvent e) {
       
        if (pickListData_ != null) {
            List<String> labels = pickListData_.getLabels();
            List<String> codes = pickListData_.getCodes();
            JXList jxList = new JXList((String[]) labels.toArray(new String[labels.size()]));
            ColorHighlighter colorHighlighter = new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, Color.CYAN, Color.WHITE);
            jxList.addHighlighter(colorHighlighter);
            jxList.setRolloverEnabled(true);
            if (pickListData_.isMultiChoice()) {
                jxList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
                jxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            final ListSelectorDialog jd = new ListSelectorDialog(WindowManager.getDefault().getMainWindow(),
                pickListData_.getDialogTitle(), jxList);
            jd.setLocationRelativeTo(null);
            int result = jd.showDialog();
            if (result == ListSelectorDialog.APPROVE_OPTION) {
                try {
                    int[] selected = jxList.getSelectedIndices();
                    PickListButton pickListButton = (PickListButton) e.getSource();
                    StringBuilder sb = new StringBuilder();

                    if (pickListData_.isRepeat()) {
                        // Build a new field occurrence from each selected item 
                        for (int i = 0; i < selected.length; i++) {
                            sb = new StringBuilder();
                            if (pickListData_.isLtGt()) {
                                sb.append("<");
                            } else if (pickListData_.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (pickListData_.isFirstDescribe()) {
                           // The first is what the user sees on the list. 
                                // The second is what it will be really inserted in the
                                // field. This is useful to mask codes with 
                                // human-readable descriptions.
                                sb.append(codes.get(selected[i]));
                            } else {
                                sb.append(labels.get(selected[i]));
                            }
                            if (pickListData_.isLtGt()) {
                                sb.append(">");
                            } else if (pickListData_.isSlashSlash()) {
                                sb.append("/");
                            }
                            Object obj = field_.getOccurrenceValue(pickListButton.getID());
                            boolean startOnCurrentOccurrence = false;
                            if (obj == null) {
                                startOnCurrentOccurrence = true;
                            } else if (obj instanceof String) {
                                String s = (String) obj;
                                if (s.equals("") || s.length() == 0 || s.trim().length() == 0) {
                                    startOnCurrentOccurrence = true;
                                }
                            }
                            if (startOnCurrentOccurrence) {
                                // Current occurrence is empty thus start from there
                                field_.setOccurrence(pickListButton.getID(), sb.toString());
                            } else {
                                field_.setOccurrence(field_.getOccurrenceCount(), sb.toString());
                            }
                        }
                    } else {
                        // We work on the current occurrence
                        if (pickListData_.isAdd()) {
                            //New selected items' text will be added to the text already in the field.
                            Object obj = field_.getOccurrenceValue(pickListButton.getID());
                            if (obj == null || ((String) obj).length() == 0) {
                                // Do nothing
                            } else {
                                // Add a blank
                                sb.append((String) obj).append(" ");
                            }

                        }
                        for (int i = 0; i < selected.length; i++) {
                            if (pickListData_.isLtGt()) {
                                sb.append("<");
                            } else if (pickListData_.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (pickListData_.isFirstDescribe()) {
                           // The first is what the user sees on the list. 
                                // The second is what it will be really inserted in the
                                // field. This is useful to mask codes with 
                                // human-readable descriptions.
                                sb.append(codes.get(selected[i]));
                            } else {
                                sb.append(labels.get(selected[i]));
                            }
                            if (pickListData_.isLtGt()) {
                                sb.append(">");
                            } else if (pickListData_.isSlashSlash()) {
                                sb.append("/");
                            }
                            if (i < selected.length - 1) {
                                sb.append(" ");
                            }
                        }
                        field_.setOccurrence(pickListButton.getID(), sb.toString());
                    }
                    GuiExecutor.instance().execute(new Runnable() {
                        @Override
                        public void run() {
                            recordModified = true;
                            redraw();
                           if (fontSize_.getFontSize() != DataEntryTopComponent.DEFAULT_FONT_SIZE) {
                              changeFontSize(fontSize_.getFontSize());
                           }
                        }
                    });
                } catch (DbException ex) {
                    Exceptions.printStackTrace(ex);
                }

            } else {
                System.err.println("Cancelled");
            }
        }
    }
   /**
    * Helper method to process Digital Document content
    */
    private void addDocument(final File f) {

        if (f != null) {
            final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
            StatusDisplayer.getDefault().setStatusText("Extracting Text ...");
            RepaintManager.currentManager(frame).paintDirtyRegions();
            IdeCursor.changeCursorWaitStatus(true);
            Date start = new Date();
            String text = doExtractText(f);
            Date end = new Date();
            System.out.println(Long.toString(end.getTime() - start.getTime())
                + " milliseconds to extract text");
            IdeCursor.changeCursorWaitStatus(false);
            try {
                // Store the document content in the 1st field occurrence
                field_.setOccurrence(FIRST_FIELD_OCCURRENCE_INDEX, text);

                String path = f.getAbsolutePath();
                /**
                 * Save the path to the document for copying the document only when the record is saved
                 */
                field_.setOccurrence(SECOND_FIELD_OCCURRENCE_INDEX, path);

                GuiExecutor.instance().execute(new Runnable() {
                    @Override
                    public void run() {
                        redraw();
                        recordModified = true;
                        System.out.println("Before applyComponentOrientation");
                        if (orientation_ == ComponentOrientation.RIGHT_TO_LEFT) {
                            applyComponentOrientation(orientation_);
                        }
                        System.out.println("After applyComponentOrientation");
                    }
                });
            } catch (DbException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
     private void addMetadata(final File f) {

       HashMap<String, String> metadataMap = new HashMap<String, String>();
        if (f != null) {
            Metadata metadata = doExtractMetadata(f);
            
            //Dublin Core metadata http://TikaCoreProperties.org/documents/dces/
            
            /**
             * CONTRIBUTOR - Responsible for making contributions to the resource.
             * 
             * Examples of a Contributor include a person, an organization, or a service. Typically, the name
             * of a Contributor should be used to indicate the entity.
             */           
            String contributor = metadata.get(TikaCoreProperties.CONTRIBUTOR);
            metadataMap.put("contributor", contributor);
            
             /**
             * COVERAGE - The spatial or temporal topic of the resource, the spatial applicability of 
             * the resource, or the jurisdiction under which the resource is relevant.
             * 
             * Spatial topic and spatial applicability may be a named place or a location specified by its
             * geographic coordinates. Temporal topic may be a named period, date, or date range. 
             * A jurisdiction may be a named administrative entity or a geographic place to which the 
             * resource applies. Recommended best practice is to use a controlled vocabulary such as 
             * the Thesaurus of Geographic Names [TGN]. 
             * Where appropriate, named places or time periods can be used in preference to numeric 
             * identifiers such as sets of coordinates or date ranges.
             * [TGN] http://www.getty.edu/research/tools/vocabulary/tgn/index.html
             */
            String coverage = metadata.get(TikaCoreProperties.COVERAGE);
             metadataMap.put("coverage", coverage);
            /**
             *  CREATOR - Primarily responsible for making the resource
             * 
             *  Examples of a Creator include a person, an organization, or a service. Typically, the name of
             *  a Creator should be used to indicate the entity.
             */
            String creator = metadata.get(TikaCoreProperties.CREATOR);
             metadataMap.put("creator", creator);
            
            /**
             * DATE - A point or period of time associated with an event in the lifecycle of the resource.
             * 
             * Date may be used to express temporal information at any level of granularity. Recommended best
             * practice is to use an encoding scheme, such as the W3CDTF profile of ISO 8601 [W3CDTF].
             * http://www.w3.org/TR/NOTE-datetime
             */
            String date = metadata.get(TikaCoreProperties.METADATA_DATE);
             metadataMap.put("date", date);
           
            
            /** 
             * DESCRIPTION - An account of the resource.
             * 
             *  Description may include but is not limited to: an abstract, a table of contents,
             *  a graphical representation, or a free-text account of the resource.
             */
            String description = metadata.get(TikaCoreProperties.DESCRIPTION);
             metadataMap.put("description", description);
            
            /** 
             *  FORMAT - The file format, physical medium, or dimensions of the resource.
             * 
             *  Recommended best practice is to use a controlled vocabulary such as the list of 
             *  Internet Media Types [MIME].
             * http://www.iana.org/assignments/media-types/
             */          
            String format = metadata.get(TikaCoreProperties.FORMAT);
             metadataMap.put("format", format);
            
             /**
             * IDENTIFIER - An unambiguous reference to the resource within a given context.
             * 
             * Recommended best practice is to identify the resource by means of a string conforming to a 
             * formal identification system.
             */
            String identifier = metadata.get(TikaCoreProperties.IDENTIFIER);
             metadataMap.put("identifier", identifier);
            
            /**
             *  LANGUAGE - A language of the resource.
             * 
             *  Recommended best practice is to use a controlled vocabulary 
             *  such as RFC 4646 [RFC4646].  http://www.ietf.org/rfc/rfc4646.txt
             */
            String language = metadata.get(TikaCoreProperties.LANGUAGE);
             metadataMap.put("language", language);
            
            /**
             * PUBLISHER - Responsible for making the resource available.
             * 
             * Examples of a Publisher include a person, an organization, or a service. Typically, the name 
             * of a Publisher should be used to indicate the entity.
             */
            String publisher = metadata.get(TikaCoreProperties.PUBLISHER);
             metadataMap.put("publisher", publisher);
            
            /**
             * RELATION - A related resource. 
             * 
             * Recommended best practice is to identify the related resource by means of a
             * string conforming to a formal identification system.
             */
            String relation = metadata.get(TikaCoreProperties.RELATION);
             metadataMap.put("relation", relation);
             
             /**
             * RIGHTS - Information about rights held in and over the resource.
             * 
             * Typically, rights information includes a statement about various property rights associated 
             * with the resource, including intellectual property rights.
             */
            String rights = metadata.get(TikaCoreProperties.RIGHTS);
             metadataMap.put("rights", rights);
            
            /**
             * SOURCE - A related resource from which the described resource is derived.
             * 
             * The described resource may be derived from the related resource in whole or in part. 
             * Recommended best practice is to identify the related resource by means of a string conforming 
             * to a formal identification system.
             */
            String source = metadata.get(TikaCoreProperties.SOURCE);
             metadataMap.put("source", source);
            
            /** 
             * SUBJECT - The topic of the resource.
             * 
             * Typically, the subject will be represented using keywords, key phrases, or classification codes.
             * Recommended best practice is to use a controlled vocabulary.
             */
            String subject = metadata.get(TikaCoreProperties.COMMENTS);
             metadataMap.put("subject", subject);
            
             /**
             * TITLE - A name given to the resource.
             * 
             * Typically, a Title will be a name by which the resource is formally known.
             */
            String title = metadata.get(TikaCoreProperties.TITLE);
             metadataMap.put("title", title);
           
            /**
             * TYPE - The nature or genre of the resource. 
             * 
             * Recommended best practice is to use a controlled
             * vocabulary such as the DCMI Type Vocabulary [DCMITYPE]. To describe the file format, physical
             * medium, or dimensions of the resource, use the Format element.
             * [DCMITYPE] http://TikaCoreProperties.org/documents/dcmi-type-vocabulary/
             */
            String type = metadata.get(TikaCoreProperties.TYPE);
             metadataMap.put("type", type);
             
             TreeMap sortedHashMap = new TreeMap(metadataMap);     
            System.out.println("Sorted HashMap: " + sortedHashMap); 
            
            for (String key :metadata.names()) {
                String[] values = metadata.getValues(key);
                for (String val : values) {
                    System.out.println("key="+key+" value="+val);
                }
            }
            
        }
     }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String actCmd = e.getActionCommand();
        if (actCmd.equals("actDel")) {
            delOccurrence(e);

        } else if (actCmd.equals("actAdd")) {
            addOccurrence();

        } else if (actCmd.equals("actPickList")) {
            addFromPicklist(e);
        } else if (actCmd.equals("actUrl")) {
            final File f = getUrlFile();
            addDocument(f);
            addMetadata(f);
        }
        System.out.println("actionPerformed end of method reached");
    }

   public String convertDocumentToHtml(File f) {
      Logger.getRootLogger().setLevel(Level.INFO);
      Tika tika = new Tika();
      tika.setMaxStringLength(-1);

      TikaConfig tikaConfig = TikaConfig.getDefaultConfig();

      Metadata metadata = new Metadata();

      InputStream stream = null;
      try {
         stream = TikaInputStream.get(f);
         //stream = new FileInputStream(f);
      } catch (final FileNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      }

      Parser parser = new AutoDetectParser(tikaConfig);

      StringWriter sw = null;

      /*
       * HTML Output handler
       */
      ContentHandler bodyHandler = null;
      TransformerHandler transformerHandler = null;
      SAXTransformerFactory factory =
              (SAXTransformerFactory) TransformerFactory.newInstance();
      try {
         bodyHandler = factory.newTransformerHandler();
         transformerHandler = (TransformerHandler) bodyHandler;


         transformerHandler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
         transformerHandler.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
         transformerHandler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "utf-8");

         // We will write the HTML output to a StringWriter
         sw = new StringWriter();
         transformerHandler.setResult(new StreamResult(sw));

      } catch (TransformerConfigurationException e1) {
         bodyHandler = new BodyContentHandler();
      }
      try {
         parser.parse(stream, transformerHandler, metadata,
                 new ParseContext());
         stream.close();

      } catch (IOException e) {
         Exceptions.printStackTrace(e);

      } catch (SAXException e) {
         Exceptions.printStackTrace(e);

      } catch (TikaException e) {
         Exceptions.printStackTrace(e);

      }
      // Get the ouput html
      String html = sw.toString();
      //System.out.println("tika html output" + html);
      return html;
   }

   public static String extractTextFromDocument(InputStream in) {

      Logger.getRootLogger().setLevel(Level.INFO);
      Tika tika = new Tika();
      tika.setMaxStringLength(-1);

      String text = null;
      try {
         text = tika.parseToString(in);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      } catch (TikaException ex) {
         Exceptions.printStackTrace(ex);
      }

      return text;
   }

   public void setOrientation(ComponentOrientation orientation) {
      orientation_ = orientation;
   }
   
   void changeFontSize(int fontSize) {

      int n = fieldEntries_.size();

      for (int i = 0; i < n; ++i) {
         RepeatableField rf = fieldEntries_.get(i);
         Font font = rf.getFont();
         font = font.deriveFont((float) fontSize);
         rf.setJTextPaneFont(font);
         rf.updateUI();
      }
   }

   void setJTextPanetFont(Font font) {
      int n = fieldEntries_.size();

      for (int i = 0; i < n; ++i) {
         RepeatableField rf = fieldEntries_.get(i);
         rf.setJTextPaneFont(font);
         rf.updateUI();
      }

   }

   private boolean fieldValidation() {
      try {
         String valFormat = wksFld_.getValidationFormat();
         if ((valFormat != null) && (valFormat.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(valFormat);
            if (formatter == null) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return false;
            } else if (formatter.hasParsingError()) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return false;
            }

            formatter.setRecord(db_, record_);
            formatter.eval();
            String result = formatter.getText();
            if (result.length() > 0) {
               new ValidationFailedException(result).displayWarning();
               return false;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } catch (RuntimeException re) {
         new FormattingException(re.getMessage()).displayWarning();
         return false;
      }
   }

   private String fieldDefaultValue(String defaultValue) {
      try {

         if ((defaultValue != null) && (defaultValue.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(defaultValue);
            if (formatter == null) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return null;
            } else if (formatter.hasParsingError()) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return null;
            }
            IRecord record = Record.createRecord();
            formatter.setRecord(db_, record);
            formatter.eval();
            String result = formatter.getText();

            return result;

         } else {
            return null;
         }
      } catch (RuntimeException re) {
         new DefaultFormattingException(re.getMessage()).displayWarning();
         return null;
      }
   }

   /**
    * Called just after the listened-to component gets the focus.
    *
    * @param e
    */
   @Override
   public void focusGained(FocusEvent e) {
      if (field_.hasOccurrences()) {
         RepeatableField source = (RepeatableField) e.getSource();
         fieldValue_ = source.getValue();
      } else {
         JTextPane source = (JTextPane) e.getSource();
         fieldValue_ = source.getText();
      }
   }

   /**
    * Called just after the listened-to component loses the focus.
    *
    * @param e
    */
   @Override
   public void focusLost(FocusEvent e) {
      try {
         if (field_.hasOccurrences()) {
            RepeatableField source = (RepeatableField) e.getSource();
            switch (source.getType()) {
               case Global.FIELD_TYPE_ALPHABETIC:
               case Global.FIELD_TYPE_ALPHANUMERIC:
               case Global.FIELD_TYPE_NUMERIC:
               case Global.FIELD_TYPE_PATTERN:
               case Global.FIELD_TYPE_STRING:
               case Global.FIELD_TYPE_BOOLEAN:
               case Global.FIELD_TYPE_CHAR:
               case Global.FIELD_TYPE_BYTE:
               case Global.FIELD_TYPE_SHORT:
               case Global.FIELD_TYPE_INT:
               case Global.FIELD_TYPE_FLOAT:
               case Global.FIELD_TYPE_LONG:
               case Global.FIELD_TYPE_DOUBLE:
               case Global.FIELD_TYPE_DATE:
               case Global.FIELD_TYPE_URL:// new added field type
               case Global.FIELD_TYPE_DOC:// new added field type
               case Global.FIELD_TYPE_TIME:

                  String newValue = source.getText();
                  //if (!fieldValue_.equals(newValue) && fieldValidation() && recordValidation()) {
                  field_.setOccurrence(source.getID(), source.getText());
                  //}
                  break;
               case Global.FIELD_TYPE_BLOB:

                  field_.setOccurrence(source.getID(), source.getValue());
                  break;
            }
         } else {
            // No Occurrences
            JTextArea source = (JTextArea) e.getSource();
            String newValue = source.getText();
            if (!fieldValue_.equals(newValue) && fieldValidation() && recordValidation()) {
               field_.setFieldValue(source.getText());
            }
         }
      } catch (DbException ex) {
         new GeneralDatabaseException(ex).displayWarning();
      }
   }

   private boolean recordValidation() {
      try {
         String valFormat = wksDef_.getRecordValidationFormat();
         if ((valFormat != null) && (valFormat.length() > 0)) {
            ISISFormatter formatter = ISISFormatter.getFormatter(valFormat);
            if (formatter == null) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return false;
            } else if (formatter.hasParsingError()) {
               GuiGlobal.output(ISISFormatter.getParsingError());
               return false;
            }

            formatter.setRecord(db_, record_);
            formatter.eval();
            String result = formatter.getText();
            if (result.length() > 0) {
               new ValidationFailedException(result).displayWarning();
               return false;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } catch (RuntimeException re) {
         new FormattingException(re.getMessage()).displayWarning();
         return false;
      }
   }

   /**
    * Redraw the data entry panel
    */
   private void redraw() {
      // fieldEntries will contain a List of Repeatable Field with data for this
      // specific field
      fieldEntries_.clear();
      ClientDatabaseProxy database = (ClientDatabaseProxy) db_;
      //------------------------------------------
      // Build an array of the occurrences values
      //-------------------------------------------
      Object values[] = {""};
      if (field_ == null || field_.getOccurrenceCount() == 0) {
         // The field is empty, just one occurrence
         values[0] = "";
         if (wksFld_.getDefaultValue() != null && wksFld_.getDefaultValue().length() > 0) {
            String defaultValue = wksFld_.getDefaultValue();
            // Execute the PFT
            String val = fieldDefaultValue(defaultValue);
            if (val != null) {
               values[0] = val;
               try {
                  field_.setFieldValue(val);
               } catch (DbException ex) {
                  Exceptions.printStackTrace(ex);
               }
            }
         }

      } else {
         // We are updating a field that already contains value(s)
         if (!field_.hasOccurrences()) {
            // Only one occurrence, put the data in the 1st value item
            values[0] = field_.getFieldValue();
         } else {
            // More than one occurrence, get number of occurrences
            int repCount = field_.getOccurrenceCount();
            // Allocate an array of objects that will contain references to
            // the occurrence data
            Object[] repData = new Object[repCount];
            for (int i = 0; i < repCount; i++) {
               repData[i] = field_.getOccurrenceValue(i);
            }
            values = repData;
         }
      }
      int tag = wksFld_.getTag();
      // Get the number of lines to display
      String size = wksFld_.getSize();
      int nLines = 0;
      if (size == null || size.length() == 0) {
          // Do nothing
      } else { 
         nLines = Integer.parseInt(size);
      }

      // Get the field type from the FDT
      FieldDefinition fieldDefinition = fdt_.getFieldByTag(tag);
      int type = fieldDefinition.getType();
      boolean isRepeatitive = fieldDefinition.isRepeatable();

      //---------------------------------------
      // The Field Data Entry Panel (EditEntry)
      //---------------------------------------
      // 1) Remove components from the JPanel container
      this.removeAll();
      // 2) Create a border around the field panel
      this.setBorder(BorderFactory.createEtchedBorder());
      /**
       *  The container has 2 components:
       *     - The field label on the left
       *     - The field occurrences panel on the right
       */
      // Create a grid bag layout manager instance
      this.setLayout(new GridBagLayout());
      // And an associate constraints object
      GridBagConstraints gridBagConstraints = new GridBagConstraints();
      // initialize the constraints parameters
      gridBagConstraints.gridx = 0; // Column 0
      gridBagConstraints.gridy = 0; // Row 0
      //gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
      // Get the worksheet Field label
      StringBuilder sb = new StringBuilder();
      sb.append("<html><b>").append(wksFld_.getDescription()).append("</b><br>")
                         .append("(").append(wksFld_.getTag()).append(")")
                         .append("</html>");
      JLabel fieldDesc = new JLabel(sb.toString());
      fieldDesc.setPreferredSize(new Dimension(150, 50));
      this.add(fieldDesc, gridBagConstraints);

      //-------------------------------------------
      // Build the Field data entry occurences panel
      //-------------------------------------------
      // Create the panel to contain all occurrences
      JPanel fieldPanel = new JPanel();
      // panel.setPreferredSize(new Dimension(300, 25));
      fieldPanel.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      // initialize the constraints parameters
      // Grid cell location where the component will be placed
      gbc.gridy = 1; // Row 1
      gbc.gridx = 1; // Column 1
      gbc.anchor = GridBagConstraints.FIRST_LINE_START;
      gbc.fill = GridBagConstraints.NONE;
      // The insets constraint adds an invisible exterior padding around the
      // component
      gbc.insets = new Insets(5, 5, 5, 5);

      Dimension buttonDimension = new Dimension(20, 20);

      switch (type) {
         case Global.FIELD_TYPE_ALPHABETIC:
         case Global.FIELD_TYPE_ALPHANUMERIC:
         case Global.FIELD_TYPE_NUMERIC:
         case Global.FIELD_TYPE_PATTERN:
         case Global.FIELD_TYPE_STRING:
         case Global.FIELD_TYPE_BOOLEAN:
         case Global.FIELD_TYPE_CHAR:
         case Global.FIELD_TYPE_BYTE:
         case Global.FIELD_TYPE_SHORT:
         case Global.FIELD_TYPE_INT:
         case Global.FIELD_TYPE_FLOAT:
         case Global.FIELD_TYPE_LONG:
         case Global.FIELD_TYPE_DOUBLE:
         case Global.FIELD_TYPE_DATE:
         case Global.FIELD_TYPE_TIME:
         case Global.FIELD_TYPE_URL:// new field type  
            // The value will be assumed to be of String type for these field types
            for (int i = 0; i < values.length; i++) {
               // We create a RepeatableField object
               RepeatableField repeatableField = new RepeatableField(this, (String) values[i], i, type);
               //
               repeatableField.getDocument().addUndoableEditListener(undoRedoManager_);
               // Change the font if needed
               if (Global.getApplicationFont() != null) {
                  repeatableField.setJTextPaneFont(Global.getApplicationFont());
               }
               /**
                * Tree View action
                */
               TreeViewAction treeViewAction = new TreeViewAction(null,
                       db_,
                       wksFld_,
                       pickListDataList_,
                       validationDataList_);

               repeatableField.getInputMap().put(KeyStroke.getKeyStroke("F10"),
                            "TreeView"+tag);
               repeatableField.getActionMap().put("TreeView"+tag,
                             treeViewAction);
               
               fieldEntries_.add(repeatableField);
               
//            TextDataEntryDocument doc = new TextDataEntryDocument();
//            doc.addDocumentListener(fieldEntry);
//            fieldEntry.setDocument(doc);
//            fieldEntry.setValue((String)value[i]);
               repeatableField.addFocusListener(this);
               // fieldEntry.setPreferredSize(new Dimension(325, 100));
               // Add a scroll bar to the JTextPane derived Repeatable component
               JScrollPane scrollPane = new JScrollPane(repeatableField);
               int fontLineHeight = repeatableField.getFontLineHeight();
               int height = (nLines ==0) ?(DEFAULT_HEIGHT_LINES*fontLineHeight) : nLines*fontLineHeight;
               Dimension dimension = new Dimension(650, height+10);
               scrollPane.setPreferredSize(dimension);
               
               //scrollPane.setPreferredSize(new Dimension(650, 175));
               // fieldEntry.setPreferredSize(new Dimension(100, 50));
               gbc.gridx = 1; // Column 1 for the occurrence
               // Add the scrollable JTextPane derived Repeatable component
               fieldPanel.add(scrollPane /*
                        * fieldEntry
                        */, gbc);
               if (isRepeatitive && field_.getOccurrenceCount()>1 && i>0) {
                  // Create the Delete (-) button
                  DeleteButton btnDel = new DeleteButton(i);
                  btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
                  btnDel.setActionCommand("actDel");
                  btnDel.addActionListener(this);
                  btnDel.setPreferredSize(buttonDimension);
                  btnDel.setToolTipText(NbBundle.getMessage(EditEntry.class, "MSG_Delete_Occurrence"));
                  gbc.gridx = 2; // Column 2 for the Delete button
                  fieldPanel.add(btnDel, gbc);
               } else {
                  gbc.gridx = 2; // Fill the gap of the Delete button
                  fieldPanel.add(Box.createHorizontalStrut(buttonDimension.width), gbc);
               }
               if (pickListData_ != null) {

                  PickListButton btnPick = new PickListButton(i, tag);
                  btnPick.setIcon(new ImageIcon(ImageUtilities.loadImage(PICK_LIST_PATH, true)));
                  btnPick.setActionCommand("actPickList");
                  btnPick.addActionListener(this);
                  btnPick.setPreferredSize(buttonDimension);

                  btnPick.setToolTipText(NbBundle.getMessage(EditEntry.class, "MSG_Access_PickList"));

                  gbc.gridx = 0; // Column 0 for the PickList button
                  fieldPanel.add(btnPick, gbc);
                  if (pickListData_.isNoType()) {
                     repeatableField.setEditable(false);
                     repeatableField.setBackground(Color.LIGHT_GRAY);
                  }
               } else {
                  gbc.gridx = 0; // Fill the gap of the PickList button
                  fieldPanel.add(Box.createHorizontalStrut(buttonDimension.width), gbc);
               }
               gbc.gridy++; // Increment the row 
            }
            // Put the occurrence panel in column 3
            gridBagConstraints.gridx = 2;
            this.add(fieldPanel, gridBagConstraints);

            if (isRepeatitive) {
               gridBagConstraints.gridwidth = 1;
               gridBagConstraints.gridy++;
               // Put the Add button at the same level than the Occurrence Panel
               gridBagConstraints.gridx = 2;  // Column
               // Create the Add(+) occurrence button
               JButton btnAdd = new JButton();
               btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(ADD_PATH, true)));
               btnAdd.setActionCommand("actAdd");
               btnAdd.addActionListener(this);
               btnAdd.setPreferredSize(buttonDimension);

               btnAdd.setToolTipText(NbBundle.getMessage(EditEntry.class, "MSG_Add_Occurrence"));
               gridBagConstraints.anchor = GridBagConstraints.LINE_START;
               this.add(btnAdd, gridBagConstraints);
               gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
            }
            
            // Increment the row for the Help Message
            if ((wksFld_.getHelpMessage() != null) && (wksFld_.getHelpMessage().length() > 0)) {
               gridBagConstraints.gridy++;
               gridBagConstraints.gridx = 0; // Column 0
             
               gridBagConstraints.gridwidth = 5; // Spans across 3 columns
               gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
               sb = new StringBuilder();
               sb.append("<html><font color=\"#0000FF\"><i>").append(wksFld_.getHelpMessage())                       
                         .append("</i></font></html>");
               JLabel helpMsg = new JLabel(sb.toString());
               helpMsg.setPreferredSize(new Dimension(700, 50));
               this.add(helpMsg, gridBagConstraints);
               gridBagConstraints.gridwidth = 1;
            }
            break;
         case Global.FIELD_TYPE_BLOB:
            for (int i = 0; i < values.length; i++) {
               RepeatableField repeatableField = new RepeatableField(this, i);
               if (Global.getApplicationFont() != null) {
                  repeatableField.setJTextPaneFont(Global.getApplicationFont());
               }
               /**
                * Tree View action
                */
               TreeViewAction treeViewAction = new TreeViewAction(null,
                       db_,
                       wksFld_,
                       pickListDataList_,
                       validationDataList_);

               repeatableField.getInputMap().put(KeyStroke.getKeyStroke("F10"),
                            "TreeView"+tag);
               repeatableField.getActionMap().put("TreeView"+tag,
                             treeViewAction);
               fieldEntries_.add(repeatableField);
               //fieldEntries_.add(repeatableField);
               byte[] bytes = null;
               if (values[i] instanceof String) {
                  bytes = ((String) values[i]).getBytes();
               } else {
                  bytes = (byte[]) values[i];
               }
               repeatableField.setValue(bytes);

               repeatableField.addFocusListener(this);
               // fieldEntry.setPreferredSize(new Dimension(325, 100));
               JScrollPane scrollPane = new JScrollPane(repeatableField);
               scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               scrollPane.setPreferredSize(new Dimension(650, 250));
               // fieldEntry.setPreferredSize(new Dimension(100, 50));
               gbc.gridx = 1; // Column 1
               fieldPanel.add(scrollPane /*
                        * fieldEntry
                        */, gbc);
               DeleteButton btnDel = new DeleteButton(i);
               btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
               btnDel.setActionCommand("actDel");
               btnDel.addActionListener(this);
               btnDel.setPreferredSize(new Dimension(25, 25));
               gbc.gridx++;
               fieldPanel.add(btnDel, gbc);
               gbc.gridy++;
            }
            gridBagConstraints.gridx++;
            this.add(fieldPanel, gridBagConstraints);
            gridBagConstraints.gridy++;
            gridBagConstraints.gridx = 2;
            // Create the Add(+) occurrence button
            JButton btnAdd = new JButton();
            btnAdd.setIcon(new ImageIcon(ImageUtilities.loadImage(ADD_PATH, true)));
            btnAdd.setActionCommand("actAdd");
            btnAdd.addActionListener(this);
            btnAdd.setPreferredSize(new Dimension(25, 25));

//                c.gridx = 100;
//                JButton btnUrl = new JButton();
//                btnUrl = new JButton();
//                btnUrl.setIcon(new ImageIcon(ImageUtilities.loadImage(ADD_PATH, true)));
//                btnUrl.setActionCommand("actUrl");
//                btnUrl.addActionListener(this);
//                btnUrl.setPreferredSize(new Dimension(25, 25));
//                this.add(btnUrl, c);
            break;
         //new field type
         case Global.FIELD_TYPE_DOC:
            for (int i = 0; i < values.length; i++) {
               String s = (String) values[i];
               //System.out.println("DOC Create Repeatable Field for String with length:"+s.length());
               RepeatableField fieldEntry = new RepeatableField(this, s, i, type);
               //System.out.println("DOC RepeatableField OK i="+i);
               if (Global.getApplicationFont() != null) {
                  fieldEntry.setJTextPaneFont(Global.getApplicationFont());
                  System.out.println("DOC fieldEntry.setJTextPaneFont( OK i="+i);
               }
               fieldEntries_.add(fieldEntry);
               //System.out.println("DOC Before fieldEntry.addFocusListener OK i="+i);
               fieldEntry.addFocusListener(this);
               //System.out.println("DOC After fieldEntry.addFocusListener OK i="+i);
               JScrollPane scrollPane = new JScrollPane(fieldEntry);
                int fontLineHeight = fieldEntry.getFontLineHeight();
               int height = (nLines ==0) ?(DEFAULT_HEIGHT_LINES*fontLineHeight) : nLines*fontLineHeight;
               Dimension dimension = new Dimension(650, height+10);
               scrollPane.setPreferredSize(dimension);
               gbc.gridx = 1;
               fieldPanel.add(scrollPane /*
                        * fieldEntry
                        */, gbc);
               //System.out.println("DOC After scrollPane OK i="+i);
               DeleteButton btnDel = new DeleteButton(i);
               btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
               btnDel.setActionCommand("actDel");
               btnDel.addActionListener(this);
               btnDel.setPreferredSize(new Dimension(25, 25));
               gbc.gridx++;
               fieldPanel.add(btnDel, gbc);
               gbc.gridy++;
            }
            gridBagConstraints.gridx++;
            this.add(fieldPanel, gridBagConstraints);
            gridBagConstraints.gridx = 100;
            JButton btnUrl = new JButton();
            btnUrl = new JButton();
            btnUrl.setIcon(new ImageIcon(ImageUtilities.loadImage(GET_FILE_CONTENT, true)));
            btnUrl.setActionCommand("actUrl");
            btnUrl.addActionListener(this);
            btnUrl.setPreferredSize(new Dimension(25, 25));
            this.add(btnUrl, gridBagConstraints);
            break;

//            case Global.FIELD_TYPE_URL:// new field type
//
//                for (int i = 0; i < value.length; i++) {
//                    RepeatableField fieldEntry = new RepeatableField((String) value[i], i);
//                    // Change the font if needed
//                    if (Global.getApplicationFont() != null) {
//                        fieldEntry.setJTextPaneFont(Global.getApplicationFont());
//                    }
//                    fieldEntries_.add(fieldEntry);
//                    fieldEntry.addFocusListener(this);
//                    JScrollPane scrollPane = new JScrollPane(fieldEntry);
//                    scrollPane.setPreferredSize(new Dimension(650, 100));
//                    gbc.gridx = 1;
//                    panel.add(scrollPane /*
//                             * fieldEntry
//                             */, gbc);
//                    DeleteButton btnDel = new DeleteButton(i);
//                    btnDel.setIcon(new ImageIcon(ImageUtilities.loadImage(DEL_PATH, true)));
//                    btnDel.setActionCommand("actDel");
//                    btnDel.addActionListener(this);
//                    btnDel.setPreferredSize(new Dimension(25, 25));
//                    gbc.gridx++;
//                    panel.add(btnDel, gbc);
//                    gbc.gridy++;
//                }
//                c.gridx++;
//                this.add(panel, c);
//
//                c.gridx = 100;
//                btnUrl = new JButton();
//                btnUrl.setIcon(new ImageIcon(ImageUtilities.loadImage(GET_FILE_CONTENT, true)));
//                btnUrl.setActionCommand("actUrl");
//                btnUrl.addActionListener(this);
//                btnUrl.setPreferredSize(new Dimension(25, 25));
//                this.add(btnUrl, c);
//                break;

         default:
            JTextArea fieldEntry = new JTextArea();
            fieldEntry.setText((values == null)
                    ? ""
                    : values.toString());
            JScrollPane scrollPane = new JScrollPane(fieldEntry);
            // fieldEntry.setPreferredSize(new Dimension(350, 100));
            scrollPane.setPreferredSize(new Dimension(650, 100));
            fieldEntry.addFocusListener(this);
            gridBagConstraints.gridx++;
            this.add(scrollPane /*
                     * fieldEntry
                     */, gridBagConstraints);
            break;
      }
     
       //System.out.println("EditEntry before updateUI");
      this.updateUI();
      //System.out.println("EditEntry after updateUI");
      
   }

   public void refresh() {

      int ncomponents = this.getComponentCount();
      for (int i = 0; i < ncomponents; ++i) {
         Component c = this.getComponent(i);

         if (c instanceof RepeatableField) {
            RepeatableField rf = (RepeatableField) c;
            rf.updateUI();
         }

      }
   }
   
    public void setUnModified() {
      for (RepeatableField repeatableField : fieldEntries_) {
         repeatableField.setModified(false);
      }
   }

   public boolean isModified() {

      for (RepeatableField repeatableField : fieldEntries_) {
         if (repeatableField.isModified()) {
            return true;
         }
      }
      return (recordModified) ? true : false;

   }

   void desableEdit() {
      for (RepeatableField repeatableField : fieldEntries_) {
         repeatableField.setEnabled(false);
      }
   }

    @Override
    public void notifyCaller(IField field, int occurrence, String event) {
        occurrenceEditEvent_.notifyCaller(field_, occurrence, event);
    }

   static class CancellableProgress implements Cancellable {

      private boolean cancelled = false;

      @Override
      public boolean cancel() {
         cancelled = true;
         return true;
      }

      public boolean cancelRequested() {
         return cancelled;
      }
   }

   private File getUrlFile() {

      File f = null;
      String lastDir = Global.prefs_.get("DATA_ENTRY_DOC_DIR", "");
      JFileChooser fileChooser = new javax.swing.JFileChooser(lastDir);
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("doc", "(MS Word Document)"));
      FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("pdf", "(Adobe Portable Document Format)");
      fileChooser.addChoosableFileFilter(pdfFilter);
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("docx", "MS docxfiles"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("xls", "(MS Excel Document)"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ppt", "(MS PowerPoint Document)"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("rtf", "(Rich Text Format)"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("html", "(HTML Format)"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("xhtml", "(XHTML Format)"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("odf", "(OpenDocument)"));
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("txt", "(Plain Text)"));

//Image Formats 	.bmp
//  	.gif
//  	.png
//  	.jpeg
//  	.tiff
//Audio Formats 	.mp3
//  	.aiff
//  	.au
//  	.midi
//  	.wav
//Misc Formats 	.pst (Outlook mail)
//  	.xml
//  	.class (Java class files)

      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setDialogTitle("Select the file to load");
      //fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setFileFilter(pdfFilter);

      Dimension dialogSize = fileChooser.getPreferredSize();
      Dimension frameSize = getSize();
      Point loc = getLocation();
      fileChooser.setLocation((frameSize.width - dialogSize.width) / 2 + loc.x, (frameSize.height - dialogSize.height) / 2 + loc.y);
      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
         f = fileChooser.getSelectedFile();
         Global.prefs_.put("DATA_ENTRY_DOC_DIR", f.getAbsolutePath());
      }
      return f;

   }
   @SuppressWarnings("unused")
   private static String TYPE_UNKNOWN = "Type Unknown";
   @SuppressWarnings("unused")
   private static String HIDDEN_FILE = "Hidden File";

   private class FileNameExtensionFilter extends FileFilter {

      private Map<String, FileNameExtensionFilter> filters = null;
      private String description = null;
      private String fullDescription = null;
      private boolean useExtensionsInDescription = true;

      public FileNameExtensionFilter() {
         this.filters = new HashMap<String, FileNameExtensionFilter>();
      }

      public FileNameExtensionFilter(String extension) {
         this(extension, null);
      }

      public FileNameExtensionFilter(String extension, String description) {
         this();
         if (extension != null) {
            addExtension(extension);
         }
         if (description != null) {
            setDescription(description);
         }
      }

      public FileNameExtensionFilter(String[] filters) {
         this(filters, null);
      }

      public FileNameExtensionFilter(String[] filters, String description) {
         this();
         for (String filter : filters) {
            // add filters one by one
            addExtension(filter);
         }
         if (description != null) {
            setDescription(description);
         }
      }

      @Override
      public boolean accept(File f) {
         if (f != null) {
            if (f.isDirectory()) {
               return true;
            }
            String extension = getExtension(f);
            if (extension != null && filters.get(getExtension(f)) != null) {
               return true;
            }
         }
         return false;
      }

      public String getExtension(File f) {
         if (f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if (i > 0 && i < filename.length() - 1) {
               return filename.substring(i + 1).toLowerCase();
            }
         }
         return null;
      }

      public void addExtension(String extension) {
         if (filters == null) {
            filters = new HashMap<String, FileNameExtensionFilter>(5);
         }
         filters.put(extension.toLowerCase(), this);
         fullDescription = null;
      }

      @Override
      public String getDescription() {
         if (fullDescription == null) {
            if (description == null || isExtensionListInDescription()) {
               fullDescription = description == null ? "(" : description
                       + " (";
               // build the description from the extension list
               Set<String> extensions = filters.keySet();
               if (extensions != null) {
                  Iterator<String> it = extensions.iterator();
                  fullDescription += "*." + it.next();
                  while (it.hasNext()) {
                     fullDescription += ", *." + it.next();
                  }
               }
               fullDescription += ")";
            } else {
               fullDescription = description;
            }
         }
         return fullDescription;
      }

      String[] getExtensions() {
         String ext[] = new String[filters.size()];
         int i = 0;
         for (String str : filters.keySet()) {
            String[] split = str.split(" ");
            ext[i] = split[0];
            i++;
         }
         return ext;
      }

      public void setDescription(String description) {
         this.description = description;
         fullDescription = null;
      }

      public void setExtensionListInDescription(boolean b) {
         useExtensionsInDescription = b;
         fullDescription = null;
      }

      public boolean isExtensionListInDescription() {
         return useExtensionsInDescription;
      }
   }

   /**
    * Create ContentHandler that transforms SAX events into textual HTML output,
    * and writes it out to <writer> - typically this is a StringWriter.
    *
    * @param writer Where to write resulting HTML text.
    * @return ContentHandler suitable for passing to parse() methods.
    * @throws Exception
    */
   private ContentHandler makeHtmlTransformer(Writer writer) throws Exception {
      SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
      TransformerHandler handler = factory.newTransformerHandler();
      handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
      handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
      handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "utf-8");
      handler.setResult(new StreamResult(writer));
      return handler;
   }

   /**
    * Creates and returns a content handler that turns XHTML input to simplified
    * HTML output that can be correctly parsed and displayed by
    * {@link JEditorPane}. <p> The returned content handler is set to output
    * <code>html</code> to the given writer. The XHTML namespace is removed from
    * the output to prevent the serializer from using the &lt;tag/&gt; empty
    * element syntax that causes extra "&gt;" characters to be displayed. The
    * &lt;head&gt; tags are dropped to prevent the serializer from generating a
    * &lt;META&gt; content type tag that makes {@link JEditorPane} fail thinking
    * that the document character set is inconsistent. <p> Additionally, it will
    * use ImageSavingParser to re-write embedded:(image) image links to be
    * file:///(temporary file) so that they can be loaded.
    *
    * @param writer output writer
    * @return HTML content handler
    * @throws TransformerConfigurationException if an error occurs
    */
   private ContentHandler getHtmlHandler(Writer writer)
           throws TransformerConfigurationException {
      SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
      TransformerHandler handler = factory.newTransformerHandler();
      handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
      handler.setResult(new StreamResult(writer));
      return new ContentHandlerDecorator(handler) {
         @Override
         public void startElement(
                 String uri, String localName, String name, Attributes atts)
                 throws SAXException {
            if (XHTMLContentHandler.XHTML.equals(uri)) {
               uri = null;
            }
            if (!"head".equals(localName)) {
               if ("img".equals(localName)) {
                  AttributesImpl newAttrs;
                  if (atts instanceof AttributesImpl) {
                     newAttrs = (AttributesImpl) atts;
                  } else {
                     newAttrs = new AttributesImpl(atts);
                  }

                  for (int i = 0; i < newAttrs.getLength(); i++) {
                     if ("src".equals(newAttrs.getLocalName(i))) {
                        String src = newAttrs.getValue(i);
                        if (src.startsWith("embedded:")) {
                           String filename = src.substring(src.indexOf(':') + 1);
                           try {
                              File img = imageParser.requestSave(filename);
                              String newSrc = img.toURI().toString();
                              newAttrs.setValue(i, newSrc);
                           } catch (IOException e) {
                              System.err.println("Error creating temp image file " + filename);
                              // The html viewer will show a broken image too to alert them
                           }
                        }
                     }
                  }
                  super.startElement(uri, localName, name, newAttrs);
               } else {
                  super.startElement(uri, localName, name, atts);
               }
            }
         }

         @Override
         public void endElement(String uri, String localName, String name)
                 throws SAXException {
            if (XHTMLContentHandler.XHTML.equals(uri)) {
               uri = null;
            }
            if (!"head".equals(localName)) {
               super.endElement(uri, localName, name);
            }
         }

         @Override
         public void startPrefixMapping(String prefix, String uri) {
         }

         @Override
         public void endPrefixMapping(String prefix) {
         }
      };
   }

   /**
    * A recursive parser that saves certain images into the temporary directory,
    * and delegates everything else to another downstream parser.
    */
   private static class ImageSavingParser extends DefaultParser {

      private Map<String, File> wanted = new HashMap<String, File>();
      private Parser downstreamParser;
      private File tmpDir;

      private ImageSavingParser(Parser downstreamParser) {
         this.downstreamParser = downstreamParser;

         try {
            File t = File.createTempFile("tika", ".test");
            tmpDir = t.getParentFile();
         } catch (IOException e) {
         }
      }

      public File requestSave(String embeddedName) throws IOException {
         String suffix = embeddedName.substring(embeddedName.lastIndexOf('.'));
         File tmp = File.createTempFile("tika-embedded-", suffix);
         wanted.put(embeddedName, tmp);
         return tmp;
      }

      public Set<MediaType> getSupportedTypes(ParseContext context) {
         // Never used in an auto setup
         return null;
      }

      public void parse(InputStream stream, ContentHandler handler,
              Metadata metadata, ParseContext context) throws IOException,
              SAXException, TikaException {
         String name = metadata.get(Metadata.RESOURCE_NAME_KEY);
         if (name != null && wanted.containsKey(name)) {
            FileOutputStream out = new FileOutputStream(wanted.get(name));
            IOUtils.copy(stream, out);
            out.close();
         } else {
            if (downstreamParser != null) {
               downstreamParser.parse(stream, handler, metadata, context);
            }
         }
      }
   }
}
