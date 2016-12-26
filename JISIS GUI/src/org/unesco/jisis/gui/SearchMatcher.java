/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.gui;


/**
 * An abstract class for matching strings.
 * @author Slava Pestov
 * @version $Id: SearchMatcher.java 13907 2008-10-19 08:22:44Z k_satoda $
 */
public abstract class SearchMatcher
{
	public SearchMatcher()
	{
		returnValue = new Match();
	}

	/**
	 * Returns the offset of the first match of the specified text
	 * within this matcher.
	 * @param text The text to search in
	 * @param start True if the start of the segment is the beginning of the
	 * buffer
	 * @param end True if the end of the segment is the end of the buffer
	 * @param firstTime If false and the search string matched at the start
	 * offset with length zero, automatically find next match
	 * @param reverse If true, searching will be performed in a backward
	 * direction.
	 * @return an array where the first element is the start offset
	 * of the match, and the second element is the end offset of
	 * the match
	 * @since jEdit 4.3pre5
	 */
	public abstract Match nextMatch(CharSequence text, boolean start,
		boolean end, boolean firstTime, boolean reverse);

	protected Match returnValue;

	//{{{ Match class
	public static class Match
	{
		public int start;
		public int end;
		public String[] substitutions;
	} //}}}
}