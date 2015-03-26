/*
 * CatalogueConstants.java
 *
 * Created on August 3, 2002, 4:12 PM
 */

package org.unesco.jisis.z3950;

/**
 *
 * @author  Siddarth1
 */
public class CatalogueConstants {
    	/**unit teriminater	*/
	public static final int UNI_T = 0x1f;
	
	/**record teriminater*/
	public static final int REC_T = 0x1d;
	
	/**field teriminater*/
	public static final int FLD_T = 0x1e;
	
	/*Fields to show marc constants - BLVL*/
	 public static final int MONOGRAPH_ANALYTIC = 1;
	 public static final int MONOGRAPH = 2;
	 public static final int SERIAL_ANALYTIC = 4;
	 public static final int SERIAL = 8;
	 public static final int SUBUNIT = 16;
	 public static final int COLLECTION = 32;	
	 
	 public static final int BLVL_MASK = 0x3F;
	 
	 public static final int VISUAL_MATERIAL = 64 * 2;
	 public static final int MIXED_MATERIAL = 64 * 4;
         public static final int MUSIC = 64 * 8;
         public static final int MAPS = 64 * 16;
         public static final int COMPUTERFILES = 64 * 32;
         public static final int BOOKS = 64 * 64;
         public static final int SERIALS = 64 * 64 * 2;

    /** Creates a new instance of CatalogueConstants */
    public CatalogueConstants() {
    }
    
}
