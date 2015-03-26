/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.dataentryex;

/**
 *
 * @author jcd
 */

public class ValidatedObjectAndEditString {
	private final Object validatedObject;
	private final String editString;
	private String errorText;

	public ValidatedObjectAndEditString(final Object validatedObject, final String editString,
	                                    final String errorText)
	{
		this.validatedObject = validatedObject;
		this.editString = editString;
		this.errorText = errorText;
	}

	public ValidatedObjectAndEditString(final Object validatedObject, final String editString) {
		this(validatedObject, editString, null);
	}

	public ValidatedObjectAndEditString(final Object validatedObject) {
		this(validatedObject, null);
	}

	public Object getValidatedObject() { return validatedObject; }
	public String getEditString() {
		if (editString != null)
			return editString;
		if (validatedObject != null)
			return validatedObject.toString();
		return "";
	}

	public void setErrorText(final String newErrorText) { errorText = newErrorText; }
	public String getErrorText() { return errorText; }

	@Override public String toString() {
		return "ValidatedObjectAndEditString: validatedObject=" + validatedObject
		       + ", editString=" + editString;
	}
}
