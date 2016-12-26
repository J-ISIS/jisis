/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.fxbrowser;

import com.sun.javafx.application.PlatformImpl;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import static javafx.concurrent.Worker.State.FAILED;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.openide.util.Lookup;
import org.slf4j.LoggerFactory;
import org.unesco.jisis.corelib.common.Global;

import org.unesco.jisis.corelib.util.DesktopLaunching;
import org.w3c.dom.Document;


/**
 * SwingFXWebView
 */
public class SwingFXWebView extends JPanel {

   private JFXPanel jfxPanel = null;
   private WebView webView;
   private WebEngine webEngine;
   private static final int PANEL_WIDTH_INT = 675;
   private static final int PANEL_HEIGHT_INT = 400;
   private JLabel lblStatus = new JLabel();
   private JProgressBar progressBar = new JProgressBar();
   private final StringProperty status = new SimpleStringProperty();
   
   Document doc;
   
   public static final String DEFAULT_JQUERY_MIN_VERSION = "1.7.2";
  public static final String JQUERY_LOCATION = "http://code.jquery.com/jquery-1.7.2.min.js";
  
   protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DesktopLaunching.class);

   public interface JFXRuntimePathProvider {

      /**
       * Returns the installation path of the JavaFX runtime; must contain file
       * lib/jfxrt.jar.
       *
       */
      public String getJFXRuntimePath();
   }


     private static String[] getFXClassPath() {

            Collection<? extends JFXRuntimePathProvider> pathProviders = Lookup.getDefault().lookupAll( JFXRuntimePathProvider.class );
           
            for( JFXRuntimePathProvider rtPathProvider : pathProviders ) {
                String rtPath = rtPathProvider.getJFXRuntimePath();
               if (rtPath == null) {
                    continue;
                }
                return new String[] {
                    rtPath + File.separatorChar + "lib" + File.separatorChar + "jfxrt.jar" //NOI18N
               };
            }
            return null;
        }


   /**
    * There are some restrictions related to JFXPanel. As a Swing component, it
    * should only be accessed from the event dispatch thread, except the 
    * setScene(javafx.scene.Scene) method, which can be called either on the 
    * event dispatch thread or on the JavaFX application thread. 
    */
   public SwingFXWebView() {
      System.out.println("System.getProperties -javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
      String version = com.sun.javafx.runtime.VersionInfo.getRuntimeVersion();
      System.out.println("javafx.runtime.RunTimeVersion: " + version);
      System.out.println("javafx.runtime.version: " + com.sun.javafx.runtime.VersionInfo.getVersion());
      String[] fxpath = null;

      fxpath = getFXClassPath();

       SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               Platform.setImplicitExit(false);
                initAndShowGUI();
                System.out.println("System.getProperties - javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
                String[] fxpath1 = getFXClassPath();
                System.out.println("fxpath1: "+fxpath1);
            }
        });
      
   }


   private void initAndShowGUI() {
      jfxPanel = new JFXPanel();
      progressBar.setPreferredSize(new Dimension(150, 18));
      progressBar.setStringPainted(true);
      System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
      JPanel statusBar = new JPanel(new BorderLayout(5, 0));
      statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      statusBar.add(lblStatus, BorderLayout.CENTER);
      statusBar.add(progressBar, BorderLayout.EAST);

      setLayout(new BorderLayout());
      add(jfxPanel, BorderLayout.CENTER);
      add(statusBar, BorderLayout.SOUTH);



      Platform.runLater(new Runnable() {
         @Override
         public void run() {
            initFX();
         }
      });
   }

   private void initFX() {
      // This method is invoked on JavaFX thread
      Scene scene = createScene();
      jfxPanel.setScene(scene);
   }

   /**
    * createScene
    *    
    * Note: The Key ISSUE is that Scene needs to be created and run on 
    * "FX user thread" and NOT on the AWT-EventQueue Thread
    *    
*/
    
    
   private Scene createScene() {

      // Set up the embedded browser:
      webView = new WebView();
      webView.setPrefSize(700, 500);
      Double widthDouble = new Integer(PANEL_WIDTH_INT).doubleValue();
      Double heightDouble = new Integer(PANEL_HEIGHT_INT).doubleValue();
      webView.setMinSize(widthDouble, heightDouble);
      webView.setPrefSize(widthDouble, heightDouble);
      webEngine = webView.getEngine();
      
      registerListeners();

      Scene scene = new Scene(webView);
      return (scene);
   }
   
    private void registerListeners() {
       
        /**
         * Title of the current Web page. If the current page has no title, 
         * the value is null.
         */
        webEngine.titleProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, 
                                    String oldValue, final String newValue) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            updateUI();
                        }
                    });
                }
      });
        /**
         * JavaScript has three kind of popup boxes: Alert box, Confirm box, 
         * and Prompt box.
         */
        // create handlers for javascript actions and status changes.
        // Sets the JavaScript prompt handler.
        webEngine.setPromptHandler(createPromptHandler());
        // Sets the JavaScript confirm handler.
        webEngine.setConfirmHandler(createConfirmHandler());
        // Sets the JavaScript alert handler.
        webEngine.setOnAlert(createAlertHandler());
        
        

        //handle popup windows  
        /**
         * JavaScript popup handler property. This handler is invoked when a
         * script running on the Web page requests a popup to be created.
         */
      webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() { // todo should create a new tab.
         @Override
         public WebEngine call(PopupFeatures popupFeatures) {
            Stage popupStage = new Stage();
            final WebView popupWebView = new WebView();
            final Scene popupScene = new Scene(popupWebView);
            popupStage.setScene(popupScene);
            popupStage.setResizable(popupFeatures.isResizable());
            popupWebView.prefWidthProperty().bind(popupScene.widthProperty());
            popupWebView.prefHeightProperty().bind(popupScene.heightProperty());
            popupStage.show();

            return popupWebView.getEngine();
         }
      });
      /**
       * Sets the JavaScript status handler.
       */
       webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
          @Override
          public void handle(final WebEvent<String> event) {
             SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   lblStatus.setText(event.getData());
                }
             });
          }
       });
       
       /**
       * Loading always happens on a background thread. Methods that initiate 
       * loading return immediately after scheduling a background job. To track
       * progress and/or cancel a job, use the Worker instance available from 
       * the getLoadWorker() method. 
       */
       Worker worker = webEngine.getLoadWorker();
       
       // monitor the web views loading state so we can provide progress feedback.
       /**
        * stateProperty - Gets the ReadOnlyObjectProperty representing the current state.
        */
       worker.stateProperty().addListener(new ChangeListener<Worker.State>() {
          @Override
          public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
             if (newState == Worker.State.CANCELLED) {
                // todo possible hook here for implementing a download handler.
             } else if (newState == Worker.State.SUCCEEDED) {
                   doc = webEngine.getDocument();
             }      
          }
       });
     /**
      * workDoneProperty - Gets the ReadOnlyDoubleProperty representing the current progress.
      */
      
      worker.workDoneProperty().addListener(new ChangeListener<Number>() {
         @Override
         public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  progressBar.setValue(newValue.intValue());
               }
            });
         }
      });

      /**
       * exceptionProperty -Gets the ReadOnlyObjectProperty representing any exception which occurred.
       */
      worker.exceptionProperty().addListener(new ChangeListener<Throwable>() {
         public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
            if (webEngine.getLoadWorker().getState() == FAILED) {
               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     JOptionPane.showMessageDialog(
                             jfxPanel,
                             (value != null)
                             ? webEngine.getLocation() + "\n" + value.getMessage()
                             : webEngine.getLocation() + "\nUnexpected error.",
                             "Loading error...",
                             JOptionPane.ERROR_MESSAGE);
                  }
               });
            }
         }
      });
      /**
       * URL of the current Web page. If the current page has no URL, the value 
       * is an empty String.
       * Change listeners are a construct that helps enable declarative programming.
       * For example, the change listener shown in this snippet executes whenever 
       * the url of the current Web page changes:
       */
      webEngine.locationProperty().addListener(new ChangeListener<String>() {
         @Override
         public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {

            if (newLoc.startsWith("mailto:")) {
               DesktopLaunching.sendMail(newLoc);

            } else {
               String downloadableExtension = null;
               String[] downloadableExtensions = {".doc", ".docx", ".pdf", ".xls", ".odt", ".zip", ".tgz", ".jar"};
               for (String ext : downloadableExtensions) {
                  if (newLoc.endsWith(ext)) {
                     downloadableExtension = ext;
                     break;
                  }
               }
               if (downloadableExtension != null) {
                  if (Desktop.isDesktopSupported()
                          && Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                     String filePath = null;
                      File file;
                     if (newLoc.startsWith("file://")) {
                        // No need to download, file is on local machine. Remove "file:" protocol
                        filePath = newLoc.substring(7);
                        filePath = filePath.replace('|', ':');
                        file = new File(filePath);
                        if (!file.exists()) {
                           final String message = "File: " + filePath + " Doesn't exist!";
                           SwingUtilities.invokeLater(new Runnable() {
                              @Override
                              public void run() {
                                 JOptionPane.showMessageDialog(null, message);
                              }
                           });
                           return;
                        }

                     } else {
                        // Document is served by a remote server, we need 1st to download
                        String tempPath = Global.getClientTempPath();
                        URL url = null;
                        try {
                           url = new URL(newLoc);
                        } catch (MalformedURLException ex) {
                           //LOGGER.error(ex.getMessage(), ex);
                        }
                       
                        
                        try {
                           file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
                           filePath = tempPath + File.separator + file.getName();

                           FileUtils.copyURLToFile(url, new File(filePath));

                        } catch (UnsupportedEncodingException ex) {
                           //LOGGER.error(ex.getMessage(), ex);
                        } catch (IOException ex) {
                           //LOGGER.error(ex.getMessage(), ex);
                        }
                     }

                     DesktopLaunching.openFile(filePath);
                     SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                           progressBar.setValue(progressBar.getMaximum());
                        }
                     });
                  } else {
                     fileLaunching(newLoc);
                  }
               }
            }

         }
      });
      
   
      
//      webEngine.documentProperty().addListener(new ChangeListener<Document>() {
//      @Override public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
//        executejQuery(
//          webEngine, 
//          "$(\"a\").click(function(event){" +
//          "  event.preventDefault();" +
//          "  $(this).hide(\"slow\");" +
//          "});"
//        );
//      }
//    });

   }
  /**
   * File launching to be used if Desktop not available
   * @param newLoc 
   */
   private void fileLaunching(String newLoc) {
      // check if the newLoc corresponds to a file you want to be downloadable
      // and if so trigger some code and dialogs to handle the download.
      if (newLoc.endsWith(".pdf")) {
         try {
//                  final PDFViewer pdfViewer = new PDFViewer(true);  // todo try icepdf viewer instead...
//                  pdfViewer.openFile(new URL(newLoc))

            final IcePdfViewer icePdfViewer = new IcePdfViewer(newLoc);
            return;
         } catch (Exception ex) {
            // just fail to open a bad pdf url silently - no action required.
         }
      }
      String downloadableExtension = null;  // todo I wonder how to find out from WebView which documents it could not process so that I could trigger a save as for them?
      String[] downloadableExtensions = {".doc", ".docx", ".pdf", ".xls", ".odt", ".zip", ".tgz", ".jar"};
      for (String ext : downloadableExtensions) {
         if (newLoc.endsWith(ext)) {
            downloadableExtension = ext;
            break;
         }
      }
      if (downloadableExtension != null) {
         // create a file save option for performing a download.
         FileChooser chooser = new FileChooser();
         chooser.setTitle("Save " + newLoc);
         chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Downloadable File", downloadableExtension));
         int filenameIdx = newLoc.lastIndexOf("/") + 1;
         if (filenameIdx != 0) {
            String fileName = newLoc.substring(filenameIdx);

            File saveFile = chooser.showSaveDialog(webView.getScene().getWindow());

            if (saveFile != null) {
               BufferedInputStream is = null;
               BufferedOutputStream os = null;
               try {
                  is = new BufferedInputStream(new URL(newLoc).openStream());
                  os = new BufferedOutputStream(new FileOutputStream(saveFile));
                  int b = is.read();
                  while (b != -1) {
                     os.write(b);
                     b = is.read();
                  }
               } catch (FileNotFoundException e) {
                  System.out.println("Unable to save file: " + e);
               } catch (MalformedURLException e) {
                  System.out.println("Unable to save file: " + e);
               } catch (IOException e) {
                  System.out.println("Unable to save file: " + e);
               } finally {
                  try {
                     if (is != null) {
                        is.close();
                     }
                  } catch (IOException e) {
                     /**
                      * no action required.
                      */
                  }
                  try {
                     if (os != null) {
                        os.close();
                     }
                  } catch (IOException e) {
                     /**
                      * no action required.
                      */
                  }
               }
            }
            // todo provide feedback on the save function and provide a download list and download list lookup.
         }
      }

   }
    
    private EventHandler<WebEvent<String>> createAlertHandler() {
        return new EventHandler<WebEvent<String>>() {
            @Override
           public void handle(WebEvent<String> stringWebEvent) {
               final String message = stringWebEvent.getData();
              SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                    JOptionPane.showMessageDialog(null, message);
                 }
              });
            }
        };
    }

    private Callback<String, Boolean> createConfirmHandler() {
      return new Callback<String, Boolean>() {
         
          int ret = JOptionPane.CANCEL_OPTION;
         @Override
         public Boolean call(String message) {
           final String msg = message;
            try {
               SwingUtilities.invokeAndWait(new Runnable() {
                  @Override
                  public void run() {
                     ret = JOptionPane.showConfirmDialog(jfxPanel, msg);

                  }
               });
            } catch (InterruptedException ex) {
               Logger.getLogger(SwingFXWebView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
               Logger.getLogger(SwingFXWebView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return (ret == JOptionPane.OK_OPTION ? true : false);
            }
        };
    }

    private Callback<PromptData, String> createPromptHandler() {
      return new Callback<PromptData, String>() {
         String input = null;

         @Override
         public String call(PromptData promptData) {
            final String message = promptData.getMessage();
            final String defaultValue = promptData.getDefaultValue();
            try {
               SwingUtilities.invokeAndWait(new Runnable() {
                  @Override
                  public void run() {
                     input = JOptionPane.showInputDialog(jfxPanel, message,
                             defaultValue);

                  }
               });
            } catch (InterruptedException ex) {
               Logger.getLogger(SwingFXWebView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
               Logger.getLogger(SwingFXWebView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return input;
            }
        };
    }
    
    public Document getDocument() {
        return doc;
    }
   
    public void load(final String url) {

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            PlatformImpl.runLater(new Runnable() {
               @Override
               public void run() {
                  webEngine.load(url);

               }
            });
         }
      });

   }
   
   public void loadContent(final String htmlContent) {

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            PlatformImpl.runLater(new Runnable() {
               @Override
               public void run() {
                  webEngine.loadContent(htmlContent);

               }
            });
         }
      });

   }
  
   public void executeScript(final java.lang.String script) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            PlatformImpl.runLater(new Runnable() {
               @Override
               public void run() {

                  webEngine.executeScript(script);
               }
            });
         }
      });
   }
   
   

   /**
   * Enables Firebug Lite for debugging a webEngine.
   * @param engine the webEngine for which debugging is to be enabled.
   */
  private static void enableFirebug(final WebEngine engine) {
    engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}"); 
  }
  
   /**
   * Executes a script which may reference jQuery function on a document.
   * Checks if the document loaded in a webEngine has a version of jQuery corresponding to 
   * the minimum required version loaded, and, if not, then loads jQuery into the document 
   * from the default JQUERY_LOCATION.
   * @param engine the webView engine to be used.
   * @Param jQueryLocation the location of the jQuery script to be executed.
   * @param minVersion the minimum version of jQuery which needs to be included in the document.
   * @param script provided javascript script string (which may include use of jQuery functions on the document).
   * @return the result of the script execution.
   */ 
  private static Object executejQuery(final WebEngine engine, String minVersion, String jQueryLocation, String script) {
    return engine.executeScript(
      "(function(window, document, version, callback) { "
        + "var j, d;"
        + "var loaded = false;"
        + "if (!(j = window.jQuery) || version > j.fn.jquery || callback(j, loaded)) {"
        + "  var script = document.createElement(\"script\");"
        + "  script.type = \"text/javascript\";"
        + "  script.src = \"" + jQueryLocation + "\";"
        + "  script.onload = script.onreadystatechange = function() {"
        + "    if (!loaded && (!(d = this.readyState) || d == \"loaded\" || d == \"complete\")) {"
        + "      callback((j = window.jQuery).noConflict(1), loaded = true);"
        + "      j(script).remove();"
        + "    }"
        + "  };"
        + "  document.documentElement.childNodes[0].appendChild(script) "
        + "} "
      + "})(window, document, \"" + minVersion + "\", function($, jquery_loaded) {" + script + "});"
    );
  }
  
   private Object executejQuery(final WebEngine engine, String script) {
    return executejQuery(engine, DEFAULT_JQUERY_MIN_VERSION, JQUERY_LOCATION, script);
  }
   
   
     private void createImage() {
        Platform.runLater(new Runnable() {
            public void run() {
                final SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.ALICEBLUE);
                webView.snapshot(
                        new Callback<SnapshotResult, Void>() {
                    public Void call(SnapshotResult result) {
                        BufferedImage image = SwingFXUtils.fromFXImage(result.getImage(), null);
                        try {
                            String WORKING_DIR = System.getProperty("user.dir");
                            String path = new File(WORKING_DIR, "test.png").getPath();
                            System.out.println(path);
                            ImageIO.write(image, "png", new File(path));
                        } catch (IOException ex) {
                        }

                        return null;
                    }
                },
                        params,
                        null);
            }
        });
    }
}
