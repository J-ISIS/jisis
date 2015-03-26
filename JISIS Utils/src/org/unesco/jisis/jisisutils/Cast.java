/*
 * Cast.java
 *
 * Created on 29 d�cembre 2006, 15:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils;

/**
 *
 * @author jc_dauphin
 */

public class Cast {
   
   public static boolean booleanValue( Object object ) {
      if ( object instanceof Boolean )
         return ( ( Boolean ) object ).booleanValue();
      else
         throw new Error( "Can't cast to Boolean" );
   }
   
   public static int intValue( Object object ) {
      if ( object instanceof Integer )
         return ( ( Integer ) object ).intValue();
      else
         throw new Error( "Can't cast to Integer" );
   }
   
   public static String stringValue( Object object ) {
      if ( object instanceof String )
         return ( String ) object;
      else
         throw new Error( "Can't cast to String" );
   }
   
   public static long longValue( Object object ) {
      if ( object instanceof Long )
         return ( ( Long ) object ).longValue();
      else
         throw new Error( "Can't cast to long" );
   }
   
   public static float floatValue( Object object ) {
      if ( object instanceof Float )
         return ( ( Float ) object ).floatValue();
      else
         throw new Error( "Can't cast to float" );
   }
   public static double doubleValue( Object object ) {
      if ( object instanceof Double )
         return ( ( Double ) object ).doubleValue();
      else
         throw new Error( "Can't cast to double" );
   }
   
   
}