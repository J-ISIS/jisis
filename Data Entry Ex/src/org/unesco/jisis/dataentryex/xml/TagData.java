/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex.xml;

/**
 *
 * @author jcd
 */
public class TagData {

   String tagDescription;

  
   String repeatitive;
   String fullUrl;
   String conciseUrl;

    public String getTagDescription() {
      return tagDescription;
   }

   public void setTagDescription(String tagDescription) {
      this.tagDescription = tagDescription;
   }
   public String getConciseUrl() {
      return conciseUrl;
   }

   public void setConciseUrl(String conciseUrl) {
      this.conciseUrl = conciseUrl;
   }

   public String getFullUrl() {
      return fullUrl;
   }

   public void setFullUrl(String fullUrl) {
      this.fullUrl = fullUrl;
   }

   public String getRepeatitive() {
      return repeatitive;
   }

   public void setRepeatitive(String repeatitive) {
      this.repeatitive = repeatitive;
   }

   public TagData(String tagDescription, String repeatitive, String fullUrl, String conciseUrl) {
      this.tagDescription = tagDescription;
      this.repeatitive = repeatitive;
      this.fullUrl = fullUrl;
      this.conciseUrl = conciseUrl;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(tagDescription).append("|").append(repeatitive).append("|").append(fullUrl).append("|").append(conciseUrl);
      return sb.toString();
   }
}