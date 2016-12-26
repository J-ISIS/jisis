/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.printsort;

/**
 *
 * @author jc_dauphin
 */

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;


/**
 *
 * @author Tomas Stupka
 *
 *  final ProgressHandle progress2 = ProgressHandleFactory.createHandle("SVN Checkout");
 *
 *           SvnProgressSupport support = new SortProgressSupport() {
 *               protected ProgressHandle getProgressHandle() {
 *                    return progress2;
 *               }
 *
 *               public void perform() {
                    final SvnClient client;
                    try {
                        client = Subversion.getInstance().getClient(repository);
                    } catch (SVNClientException ex) {
                        Exceptions.printStackTrace(ex); // should not happen
                        return;
                    }
                    try {
                        setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
                        CheckoutAction.checkout(client, repository, repositoryFiles, file, true, this);
                    } catch (SVNClientException ex) {
                        annotate(ex);
                        return;
                    }
                    if(isCanceled()) {
                        return;
                    }
                    boolean atWorkingDirLevel = true; //XXX ?

                    setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_ScanFolders_Progress"));
                    if (SvnModuleConfig.getDefault().getShowCheckoutCompleted()) {
                        String[] folders;
                        if(atWorkingDirLevel) {
                            folders = new String[1];
                            folders[0] = "."; // NOI18N
                        } else {
                            folders = new String[repositoryFiles.length];
                            for (int i = 0; i < repositoryFiles.length; i++) {
                                if(isCanceled()) {
                                    return;
                                }
                                if(repositoryFiles[i].isRepositoryRoot()) {
                                    folders[i] = "."; // NOI18N
                                } else {
                                    folders[i] = repositoryFiles[i].getFileUrl().getLastPathSegment();
                                }
                            }
                        }
                        CheckoutCompleted cc = new CheckoutCompleted(file, folders, true);
                        if(isCanceled()) {
                            return;
                        }
                        cc.scanForProjects(this);
                    }
                }
            };
            support.start(Subversion.getInstance().getRequestProcessor(repository), repository, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
        } catch (MalformedURLException mre) {
            Exceptions.printStackTrace(mre);
        }
        return null;
    }

 */
public abstract class SortProgressSupport implements Runnable, Cancellable {

    private Cancellable delegate;
    private volatile boolean canceled;

    private ProgressHandle progressHandle = null;
    private String displayName = "";            // NOI18N
    private String originalDisplayName = "";    // NOI18N
//    private OutputLogger logger;
//    private SVNUrl repositoryRoot;
    private RequestProcessor.Task task;

    public RequestProcessor.Task start(RequestProcessor rp, String displayName) {
        setDisplayName(displayName);
        //this.repositoryRoot = repositoryRoot;
        startProgress();
        setProgressQueued();
        task = rp.post(this);
        task.addTaskListener(new TaskListener() {
            public void taskFinished(org.openide.util.Task task) {
                delegate = null;
            }
        });
        return task;
    }

//    public void setRepositoryRoot(SVNUrl repositoryRoot) {
//        this.repositoryRoot = repositoryRoot;
//        logger = null;
//    }

    public void run() {
        setProgress();
        performIntern();
    }

    protected void performIntern() {
        try {
            //Diagnostics.println("Start - " + displayName); // NOI18N
            if(!canceled) {
                perform();
            }
            //Diagnostics.println("End - " + displayName); // NOI18N
        } finally {
            finnishProgress();
            //getLogger().closeLog();
        }
    }

    protected abstract void perform();

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized boolean cancel() {
        if (canceled) {
            return false;
        }
        //getLogger().flushLog();
        if(task != null) {
            task.cancel();
        }
        if(delegate != null) {
            delegate.cancel();
        }
        getProgressHandle().finish();
        canceled = true;
        return true;
    }

    void setCancellableDelegate(Cancellable cancellable) {
        this.delegate = cancellable;
    }

    public void setDisplayName(String displayName) {
        if(originalDisplayName.equals("")) { // NOI18N
            originalDisplayName = displayName;
        }
        this.displayName = displayName;
        setProgress();
    }

    private void setProgressQueued() {
        if(progressHandle != null) {
            progressHandle.progress(NbBundle.getMessage(SortProgressSupport.class,  "LBL_Queued", displayName));
        }
    }

    private void setProgress() {
        if(progressHandle != null) {
            progressHandle.progress(displayName);
        }
    }

    protected String getDisplayName() {
        return displayName;
    }

    protected ProgressHandle getProgressHandle() {
        if(progressHandle == null) {
            progressHandle = ProgressHandleFactory.createHandle(displayName, this);
        }
        return progressHandle;
    }

    protected void startProgress() {
        getProgressHandle().start();
        //getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName); // NOI18N
    }

    protected void finnishProgress() {
        getProgressHandle().finish();
        if (isCanceled() == false) {
            //getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(SvnProgressSupport.class, "MSG_Progress_Finished")); // NOI18N
        } else {
            //getLogger().logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(SvnProgressSupport.class, "MSG_Progress_Canceled")); // NOI18N
        }
    }

//    protected OutputLogger getLogger() {
//        if (logger == null) {
//            logger = Subversion.getInstance().getLogger(repositoryRoot);
//        }
//        return logger;
//    }
//
//    public void annotate(SVNClientException ex) {
//        SvnClientExceptionHandler.notifyException(ex, !isCanceled(), true);
//    }
}
