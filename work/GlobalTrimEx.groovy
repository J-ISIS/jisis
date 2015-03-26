import org.unesco.jisis.corelib.server.JisisDbUtil;

def globalTrimEx() {
  
  dbHome = "DEF_HOME";
  dbName = "PERSO"
  
  JisisDbUtil.trimData(dbHome, dbName);
  
  }
  globalTrimEx();
  