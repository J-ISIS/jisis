/*
 * OO8Processor.java
 *
 */
package org.unesco.jisis.z3950;

public abstract class OO8Processor {

   public static OO8Processor get008Processor(int n) {
      return getProcessor(n, false);
   }

   public static OO8Processor getProcessor(int n, boolean six) {

      if (((n & (CatalogueConstants.BOOKS | CatalogueConstants.COMPUTERFILES
              | CatalogueConstants.VISUAL_MATERIAL)) > 0)
              && (((n & CatalogueConstants.BLVL_MASK) & CatalogueConstants.SERIAL) > 0)) {
         return new oo8SerialProcessor(six);
      }
      if ((n & CatalogueConstants.VISUAL_MATERIAL) > 0) {
         return new oo8VisualMaterialProcessor(six);
      }
      if ((n & CatalogueConstants.MIXED_MATERIAL) > 0) {
         return new oo8MixedMaterialProcessor(six);
      }
      if ((n & CatalogueConstants.MUSIC) > 0) {
         return new oo8MusicProcessor(six);
      }
      if ((n & CatalogueConstants.MAPS) > 0) {
         return new oo8MapProcessor(six);
      }
      if ((n & CatalogueConstants.COMPUTERFILES) > 0) {
         return new oo8ComputerFileProcessor(six);
      }
      if ((n & CatalogueConstants.BOOKS) > 0) {
         return new oo8BookProcessor(six);
      }
      return new oo8BookProcessor(six);
   }

   public static OO8Processor get006Processor(int n) {
      return getProcessor(n, true);
   }

   public void process006(FixedField f, String data) throws MarcException {
      if (data == null) {
         return;
      }
      if (data.length() != 18) {
         throw new MarcException("Invalid 006 length: " + data);
      }
      //throw new MarcException("Invalid 006 length: "+data);

   }

   public void process008(FixedField f, String data) throws MarcException {
      if (data == null) {
         return;
      }
      if (data.length() != 40) {
         throw new MarcException("Invalid 008 length " + data);

      }

      f.setDtst(data.charAt(6));
      f.setDates(data.substring(7, 15));
      f.setCtry(data.substring(15, 17));
      f.setSrce(data.charAt(39));
      f.setLang(data.substring(35, 38));



   }//process

   public abstract void process(FixedField f, String data) throws MarcException;
}

class oo8BookProcessor extends OO8Processor {

   private boolean six = true;

   public oo8BookProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }
      f.setIlls(data.substring(18, 22));
      f.setForm(data.charAt(22));
      f.setCont(data.substring(24, 28));
      f.setConf(data.charAt(29));
      f.setGpub(data.charAt(28));
      f.setFest(data.charAt(30));
      f.setAudn(data.charAt(22));
      f.setBiog(data.charAt(34));
      f.setLitf(data.charAt(33));

   }
}//book

class oo8SerialProcessor extends OO8Processor {

   private boolean six = true;

   public oo8SerialProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }

      f.setFreq(data.charAt(18));
      f.setRegl(data.charAt(19));
      f.setIssn(data.charAt(20));
      f.setSrtp(data.charAt(21));
      f.setOrig(data.charAt(22));
      f.setForm(data.charAt(23));
      f.setEntw(data.charAt(24));
      f.setCont(data.substring(25, 28));
      f.setGpub(data.charAt(28));
      f.setConf(data.charAt(29));
      f.setAlph(data.charAt(33));
      f.setS_L(data.charAt(34));


   }
}//serial

class oo8MixedMaterialProcessor extends OO8Processor {

   private boolean six = true;

   public oo8MixedMaterialProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }

      f.setForm(data.charAt(23));


   }
}//Mixed material

class oo8MapProcessor extends OO8Processor {

   private boolean six = true;

   public oo8MapProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }

      //18 - 21 Relif
      f.setProj(data.substring(22, 24));
      f.setTmat(data.charAt(25));
      f.setGpub(data.charAt(28));
      f.setForm(data.charAt(29));
      //31 - Index
      //33  34 special formats.


   }
}//Map

class oo8VisualMaterialProcessor extends OO8Processor {

   private boolean six = true;

   public oo8VisualMaterialProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }

      f.setTime(data.substring(18, 21));
      f.setAudn(data.charAt(22));
      f.setGpub(data.charAt(28));
      f.setForm(data.charAt(29));
      f.setTmat(data.charAt(33));
      f.setTech(data.charAt(34));


   }
}//VisualMaterial

class oo8MusicProcessor extends OO8Processor {

   private boolean six = true;

   public oo8MusicProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }

      // 18 - 19 composition

      f.setSpform(data.charAt(20));
      f.setAudn(data.charAt(22));
      f.setForm(data.charAt(23));
      //24 - 29 accompaning material
      //30 - 31 Literary text.


   }
}//Musis

class oo8ComputerFileProcessor extends OO8Processor {

   private boolean six = true;

   public oo8ComputerFileProcessor(boolean six) {
      this.six = six;
   }

   @Override
   public void process(FixedField f, String data) throws MarcException {
      if (f == null) {
         return;
      }
      if (data == null) {
         return;
      }
      if (!six) {
         super.process008(f, data);
      } else {
         super.process006(f, data);
      }

      f.setAudn(data.charAt(22));
      f.setFile(data.charAt(26));
      f.setGpub(data.charAt(28));

   }
}//Computer File.

