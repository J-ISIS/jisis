/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryexdl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.SwingWorker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.openide.util.Exceptions;

/**
 *
 * @author jcd
 */
public class MetadataExtractSwingWorker extends SwingWorker<Metadata, Object> {

    private final File f;
    private Metadata metadata;
    public static Logger logger = null;

    static {
        logger = Logger.getRootLogger();
        logger.setLevel(Level.OFF);

    }

    public MetadataExtractSwingWorker(File f) {
        this.metadata = null;
        this.f = f;
    }

    @Override
    protected Metadata doInBackground() throws Exception {
        try {
            metadata = extractMetaDataFromDocument(new FileInputStream(f));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return metadata;  //can be retrieved by calling get()
    }

    public static Metadata extractMetaDataFromDocument(InputStream is) throws IOException {

        Tika tika = new Tika();
        tika.setMaxStringLength(-1);

        Metadata met = new Metadata();

        tika.parse(is, met);

        return met;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
