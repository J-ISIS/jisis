/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

import java.awt.Color;

/**
 * A class that represents a colored string.
 * @author jcd
 */


public class ColoredString
  {
  private String string_ = "";
  private Color color_ = Color.black;

  /** Construct a new colored string.
    *
    * @param string The string.
    * @param color The color.
    */

  public ColoredString(String string, Color color)
    {
    string_ = string;
    color_ = color;
    }

  /** Construct a new colored string. The string is set to "" and the color
    * is set to black.
    */

  public ColoredString()
    {
    }

  /** Get the string.
    *
    * @return The string.
    * @see #setString
    */

  public String getString()
    {
    return(string_);
    }

  /** Set the string.
    *
    * @param string The new string.
    * @see #getString
    */

  public void setString(String string)
    {
    string_ = string;;
    }

  /** Get the color.
    *
    * @return The color.
    * @see #setColor
    */

  public Color getColor()
    {
    return(color_);
    }

  /** Set the color.
    *
    * @param color The new color.
    * @see #getColor
    */

  public void setColor(Color color)
    {
    color_ = color;
    }
  }
