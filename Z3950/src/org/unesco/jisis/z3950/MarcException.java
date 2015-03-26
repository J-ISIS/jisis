/*
 * MarcException.java
 *
 * Created on August 3, 2002, 5:20 PM
 */



package org.unesco.jisis.z3950;
/**
 *
 * @author  Siddarth1
public class MarcException extends Exception{
 
 /** 
 * default constructor */


 public class MarcException extends Exception{
 
 /**
 * default constructor */
 public MarcException(){
		this("Marc Exception...");
 }
 
 /**
 * constructor with string as argument
 * @param It takes string as argument  */
 public MarcException(String s){
		super(s);
 }
 
 }