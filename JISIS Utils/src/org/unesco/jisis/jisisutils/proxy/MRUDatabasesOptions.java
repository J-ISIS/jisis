/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.proxy;

//~--- non-JDK imports --------------------------------------------------------

import org.openide.util.NbPreferences;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.event.EventListenerList;
import org.unesco.jisis.corelib.common.DbInfo;

/**
 *
 * @author jc_dauphin
 */
public class MRUDatabasesOptions {
   protected static String            DEFAULT_NODE_NAME = "prefs";
   protected String                   nodeName          = null;
   private EventListenerList          listenerList;
   public static final String         MRU_FILE_LIST_PROPERTY = "MRUFileList";
   private List<DbInfo>               mruDatabaseList_;
   private int                        maxSize;
   private static MRUDatabasesOptions instance;    // The single instance

   static {
      instance = new MRUDatabasesOptions();
   }

   /**
    * Returns the single instance, creating one if it's the
    * first time this method is called.
    *
    * @return The single instance.
    */
   public static MRUDatabasesOptions getInstance() {
      return instance;
   }

   /** {@inheritDoc} */
   protected MRUDatabasesOptions() {
      nodeName         = "mrudatabases";
      maxSize          = 9;    // default is 9
      mruDatabaseList_ = new ArrayList<DbInfo>(maxSize);
      listenerList     = new EventListenerList();

      retrieve();
   }

   public List<DbInfo> getMRUDatabaseList() {
      return mruDatabaseList_;
   }

   public void setMRUDatabaseList(List<DbInfo> list) {
      this.mruDatabaseList_.clear();

      for (int i = 0; i < list.size(); i++) {
         this.mruDatabaseList_.add(list.get(i));

         if (i >= maxSize) {
            break;
         }
      }

      firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruDatabaseList_);
      store();
   }

   public void addDatabase(DbInfo dbInfo) {
      // remove the old
      int idx = -1;
      for (int i=0; i<mruDatabaseList_.size(); i++) {
         DbInfo dbi =  mruDatabaseList_.get(i);
         if (dbi.equals(dbInfo)) {
            idx = i;
            break;
         }
      }
      if (idx>=0) {
         mruDatabaseList_.remove(idx);
      }
      // add to the top
      mruDatabaseList_.add(0, dbInfo);

      while (mruDatabaseList_.size() > maxSize) {
         mruDatabaseList_.remove(mruDatabaseList_.size() - 1);
      }

      firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruDatabaseList_);
      store();
   }

   protected void store() {
      Preferences prefs = getPreferences();

      // clear the backing store
      try {
         prefs.clear();
      } catch (BackingStoreException ex) {}

      try {
         for (int i = 0; i < mruDatabaseList_.size(); i++) {
            DbInfo dbInfo = mruDatabaseList_.get(i);
            // Serialize
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream    oos  = new ObjectOutputStream(baos);

            oos.writeObject(dbInfo);
            oos.close();
            prefs.putByteArray(MRU_FILE_LIST_PROPERTY + i, baos.toByteArray());
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   protected void retrieve() {
      mruDatabaseList_.clear();

      Preferences prefs = getPreferences();

      try {
         for (int i = 0; i < maxSize; i++) {
            byte[] obj = prefs.getByteArray(MRU_FILE_LIST_PROPERTY + i, null);

            if (obj != null) {
               InputStream       in     = new ByteArrayInputStream(obj);
               ObjectInputStream ois    = new ObjectInputStream(in);
               Object            o      = ois.readObject();
               DbInfo            dbInfo = (DbInfo) o;

               mruDatabaseList_.add(dbInfo);

            } else {
               break;
            }
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   /** {@inheritDoc} */
   public void addPropertyChangeListener(PropertyChangeListener listener) {
      listenerList.add(PropertyChangeListener.class, listener);
   }

   /** {@inheritDoc} */
   public void removePropertyChangeListener(PropertyChangeListener listener) {
      listenerList.remove(PropertyChangeListener.class, listener);
   }

   protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);

      for (int i = listeners.length - 2; i >= 0; i -= 2) {
         if (listeners[i] == PropertyChangeListener.class) {
            ((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
         }
      }
   }

   /**
    * Return the backing store Preferences
    * @return Preferences
    */
   protected final Preferences getPreferences() {
      String name = DEFAULT_NODE_NAME;

      if (nodeName != null) {
         name = nodeName;
      }

      Preferences prefs = NbPreferences.forModule(this.getClass()).node("options").node(name);

      return prefs;
   }
}
