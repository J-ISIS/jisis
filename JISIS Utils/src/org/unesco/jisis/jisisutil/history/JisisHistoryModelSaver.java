/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutil.history;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.Global;



/**
 * Handles loading and saving of the "historyFile" files.
 *
 * A historyFile file is .ini format and stores historymodels for all named historytextfields, separately but in
 the same file.
 *
 * @author Matthieu Casanova
 * @version $Id: FoldHandler.java 5568 2006-07-10 20:52:23Z kpouer $
 */
public class JisisHistoryModelSaver implements HistoryModelSaver {

   //{{{ Private members
    private static final String TO_ESCAPE = "\r\n\t\\\"'[]";
    private static File historyFile;
    private static long historyModTime;
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JisisHistoryModelSaver.class);
    //{{{ load() method

    /**
     *
     * @param models
     * @return
     */
    @Override
    public Map<String, HistoryModel> load(Map<String, HistoryModel> models) {

        
        historyFile = new File(Global.getClientTempPath(), "history");
        if (!historyFile.exists()) {
            return models;
        }

        historyModTime = historyFile.lastModified();

        LOGGER.info("Loading history");

        if (models == null) {
            models = Collections.synchronizedMap(new HashMap<String, HistoryModel>());
        }

        BufferedReader in = null;
        try {
			// Try loading with UTF-8 and fallback to the system
            // default encoding to load a historyFile which was made by
            // an old version as well.
            try {
				// Pass the decoder explicitly to report a decode error
                // as an exception instead of replacing with \xFFFD.
                in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(historyFile),
                    Charset.forName("UTF-8").newDecoder()));
                models.putAll(loadFromReader(in));
            } catch (CharacterCodingException e) {
                if (in != null) {
                    in.close();
                }
                LOGGER.info(
                    "Failed to load history with UTF-8."
                    + " Fallbacking to the system default encoding.");

                in = new BufferedReader(new FileReader(historyFile));
                models.putAll(loadFromReader(in));
            }
        } catch (FileNotFoundException fnf) {
            //Log.log(Log.DEBUG,HistoryModel.class,fnf);
        } catch (IOException io) {
            LOGGER.error("History IO error", io);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return models;
    } //}}}

    //{{{ save() method
    @Override
    public boolean save(Map<String, HistoryModel> models) {
        LOGGER.info("Saving history");
       
        File file1 = new File(Global.getClientTempPath(), "history.backup");
        File file2 = new File(Global.getClientTempPath(), "history");
        if (file2.exists() && file2.lastModified() != historyModTime) {
            LOGGER.warn(file2
                + " changed on disk; will not save history");
            return false;
        }
        
        // Backup
         file2.renameTo(file1);


        String lineSep = System.getProperty("line.separator");

        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file1), "UTF-8"));

            if (models != null) {
                Collection<HistoryModel> values = models.values();
                for (HistoryModel model : values) {
                    if (model.getSize() == 0) {
                        continue;
                    }

                    out.write('[');
                    out.write(MiscUtilities.charsToEscapes(
                        model.getName(), TO_ESCAPE));
                    out.write(']');
                    out.write(lineSep);

                    for (int i = 0; i < model.getSize(); i++) {
                        out.write(MiscUtilities.charsToEscapes(
                            model.getItem(i),
                            TO_ESCAPE));
                        out.write(lineSep);
                    }
                }
            }

            out.close();

            /* to avoid data loss, only do this if the above
             * completed successfully */
            file2.delete();
            file1.renameTo(file2);
        } catch (IOException io) {
            LOGGER.error("IO Error when saving", io);
        } finally {
            IOUtils.closeQuietly(out);
        }

        historyModTime = file2.lastModified();
        return true;
    } //}}}

    //{{{ loadFromReader() method
    private static Map<String, HistoryModel> loadFromReader(BufferedReader in)
        throws IOException {
        Map<String, HistoryModel> result = new HashMap<>();

        HistoryModel currentModel = null;
        String line;

        while ((line = in.readLine()) != null) {
            if (line.length() > 0 && line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
                if (currentModel != null) {
                    result.put(currentModel.getName(),
                        currentModel);
                }

                String modelName = MiscUtilities
                    .escapesToChars(line.substring(
                            1, line.length() - 1));
                currentModel = new HistoryModel(
                    modelName);
            } else if (currentModel == null) {
                throw new IOException("History data starts"
                    + " before model name");
            } else {
                currentModel.addElement(MiscUtilities
                    .escapesToChars(line));
            }
        }

        if (currentModel != null) {
            result.put(currentModel.getName(), currentModel);
        }

        return result;
    } //}}}

	//}}}
}
