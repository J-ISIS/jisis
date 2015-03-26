/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.index;

//~--- non-JDK imports --------------------------------------------------------
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

//~--- JDK imports ------------------------------------------------------------
import org.openide.util.RequestProcessor;
import org.unesco.jisis.corelib.common.FieldSelectionTable;
import org.unesco.jisis.corelib.common.IDatabase;
import org.unesco.jisis.corelib.exceptions.DbException;
import org.unesco.jisis.gui.Util;

/**
 *
 * @author jc_dauphin
 */
public class IndexDatabaseAction extends AbstractAction {

    static final String ICON_PATH = "org/unesco/jisis/index/index.png";

    /**
     * Creates a new named RequestProcessor with defined throughput which can support interruption of the
     * thread the processor runs in. public RequestProcessor(String name, int throughput, boolean
     * interruptThread)
     *
     * Parameters: name - the name to use for the request processor thread throughput - the maximal count of
     * requests allowed to run in parallel interruptThread - true if RequestProcessor.Task.cancel() shall
     * interrupt the thread
     */
    private final static RequestProcessor requestProcessor_ = new RequestProcessor("interruptible tasks", 1, true);

    public IndexDatabaseAction() {
        super(NbBundle.getMessage(IndexDatabaseAction.class, "CTL_Re_IndexDatabase"));
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(ICON_PATH, true)));
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!Util.isAdmin()) {
            return;
        }
        IDatabase db = Util.getDatabaseToUse(evt);
        if (db == null) {
            return;
        }
        FieldSelectionTable fst = null;
        try {
            fst = db.getFieldSelectionTable();
        } catch (DbException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (!Util.isFstCorrect(fst)) {
            String msg = NbBundle.getMessage(IndexDatabaseAction.class,
                "MSG_IndexDatabaseErrorsInFst");
            NotifyDescriptor d
                = new NotifyDescriptor.Confirmation(msg,
                    NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        ReIndex reIndexRun = new ReIndex(db);

        requestProcessor_.post(reIndexRun);

    }

}
