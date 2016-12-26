package org.unesco.jisis.jisiscore;

//~--- non-JDK imports --------------------------------------------------------


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import java.awt.Font;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;



import org.apache.shiro.util.Factory;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.server.DbServerService;
import org.unesco.jisis.corelib.server.HomeManager;
import org.unesco.jisis.jetty.webserver.JettyRunner;
import org.unesco.jisis.jisisutil.history.HistoryModel;
import org.unesco.jisis.jisisutil.history.JisisHistoryModelSaver;
import org.unesco.jisis.jisisutils.gui.SwingUtils;


public class JavaISIS extends ModuleInstall  {
  
    
    private static JettyRunner jettyRunner;
   
    public static Preferences            prefs         = Preferences.userNodeForPackage(JavaISIS.class);
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JavaISIS.class);

    private static DbServerService dbServer_ = null;


   @Override
    public void restored() {

        // print logback internal state
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        System.out.println("Entry in JavaISIS restored");
        LOGGER.info("Entry in JavaISIS restored");
        System.out.println(Thread.currentThread().getContextClassLoader());
        if (!isJavaGE18()) {

            String msg = NbBundle.getMessage(JavaISIS.class, "MSG_JavaLessThan17", System.getProperty("java.version"));
            NotifyDescriptor d = new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(d);

        }

        try {
            LOGGER.info("entering JavaISIS restored()");

            dbServer_ = new DbServerService();
            System.out.println("Creating Reactor with port:" + dbServer_.getServerPort());
            LOGGER.info("Creating Reactor with port: [{}]", dbServer_.getServerPort());

            String[] homes = DbServerService.getDbHomeManager().getDbHomeNames();
            String homePath = DbServerService.getDbHomeManager().getDbHomePath(homes[0]);
            jettyRunner = new JettyRunner(homePath);

            initClientGui();

            /**
             * Load History
             */
            HistoryModel.setSaver(new JisisHistoryModelSaver());
            HistoryModel.loadHistory();

            dbServer_.start();
            System.out.println("After Starting the server");
            LOGGER.debug("Server Thread successfully started");

            /**
             * Should be enable to re-create the users database
             */
            //UserDB.createUserDatabase();
        } catch (final Exception ex) {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {

                    JPanel panel = new JPanel();
                    JOptionPane.showMessageDialog(
                        panel, "Error: " + ex.toString(),
                        "Error", JOptionPane.ERROR_MESSAGE
                    );

                }
            });
            Exceptions.printStackTrace(ex);
            LifecycleManager.getDefault().exit();
        }
        System.out.println("exiting restored()");

//      DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("restored done",
//             NotifyDescriptor.INFORMATION_MESSAGE));
    }

    @Override
    public boolean closing() {
        WindowManager wm = WindowManager.getDefault();
        org.openide.windows.Mode mode = wm.findMode("editor");
        TopComponent[] topComponents = wm.getOpenedTopComponents(mode);
        for (TopComponent topComponent : topComponents) {
            if (!topComponent.canClose()) {
                return false;
            }
        }
        String label = NbBundle.getMessage(JavaISIS.class, "MSG_OkToClose");
        String title = NbBundle.getMessage(JavaISIS.class, "MSG_JavaIsis");
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(label, title, NotifyDescriptor.OK_CANCEL_OPTION);

        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
         
            return true;
        }
        return false;
    }


    @Override
    public void close() {
        HistoryModel.saveHistory();
        LOGGER.info("J-ISIS History saved");
        try {
            if (dbServer_ != null) {
                dbServer_.stop();
            }
            if (jettyRunner != null) {
                jettyRunner.stop();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        System.out.println("Server Thread closed!");
        System.out.println("J-ISIS finished");
        LOGGER.info("Server Thread closed!");
        LOGGER.info("J-ISIS finished");
    }


  
   private void initClientGui() {
      //Locale.setDefault(Locale.ENGLISH);

      /**
       * Force the display of button mnemonics otherwise they only appear after
       * pressing Alt on Windows
       */
      UIManager.getLookAndFeelDefaults().put("Button.showMnemonics", true);

      Global.setPreference(prefs);

      String fontFamily = Global.prefs_.get("APPLICATION_FONT_FAMILY", "DEFAULT_APPLICATION-FONT-FAMILY");


      if (!fontFamily.equalsIgnoreCase("DEFAULT_APPLICATION-FONT-FAMILY")) {
         int optionType = JOptionPane.YES_NO_OPTION; // YES+NO+CANCEL
         int messageType = JOptionPane.PLAIN_MESSAGE; // no standard icon

         String msg = NbBundle.getMessage(JavaISIS.class, "MSG_DO_YOU_WANT_TO_KEEP_THE_CHANGED_FONT");
         String title = NbBundle.getMessage(JavaISIS.class, "MSG_APPLICATION_FONT");
         int res = JOptionPane.showConfirmDialog(null, msg, title,
                 optionType, messageType);
         if (res == JOptionPane.YES_OPTION) {
            int fontStyle = Integer.valueOf(Global.prefs_.get("APPLICATION_FONT_STYLE", "DEFAULT_APPLICATION-FONT-FAMILY"));
            int fontSize = Integer.valueOf(Global.prefs_.get("APPLICATION_FONT_SIZE", "DEFAULT_APPLICATION-FONT-SIZE"));
            Font font = new Font(fontFamily, fontStyle, fontSize);
            SwingUtils.setApplicationFont(font);
            Global.setApplicationFont(font);
         } else {
            Global.prefs_.remove("APPLICATION_FONT_FAMILY");
            Global.prefs_.remove("APPLICATION_FONT_STYLE");
            Global.prefs_.remove("APPLICATION_FONT_SIZE");
         }
      }

      /* Check that client work dir  exists */
      boolean exists = (new File(getClientWorkPath()).exists());
      if (!exists) {
         System.out.println("Creating Client Work Path: " + getClientWorkPath());
         boolean bmkdir = (new File(getClientWorkPath())).mkdir();
      }
      Global.setClientWorkPath(getClientWorkPath());

      /* Check that client temp dir  exists */
      exists = (new File(getClientTempPath()).exists());
      if (!exists) {
         System.out.println("Creating Client Temp Path: " + getClientTempPath());
         boolean bmkdir = (new File(getClientTempPath())).mkdir();
      }
      Global.setClientTempPath(getClientTempPath());
   }

    /** User's current working directory
    * @return  */
    public static String getHome() {
        return System.getProperty("user.dir");
    }

    public static String getSrvConfigPath() {
        return DbServerService.getSrvConfigPath();
    }

    public static String getDbHomePath() {
        return DbServerService.getDbHomePath();
    }

    public static HomeManager getDbHomeManager() {
        return DbServerService.getDbHomeManager();
    }


      public static String getClientWorkPath() {
        return DbServerService.getJIsisHome() + File.separator + "work" ;
    }

       public static String getClientTempPath() {
        return DbServerService.getJIsisHome() + File.separator + "temp" ;
    }
       
       public static boolean isJavaGE18() {
          boolean b = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) >= 8;
          return b;
       }
       
     public static void initSecurity() {

       //1. Load the INI configuration
        Factory<org.apache.shiro.mgt.SecurityManager> factory
            = new IniSecurityManagerFactory("classpath:shiro.ini");

        //2. Create the SecurityManager
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        //3. Make it accessible
        SecurityUtils.setSecurityManager(securityManager);

    }

}
