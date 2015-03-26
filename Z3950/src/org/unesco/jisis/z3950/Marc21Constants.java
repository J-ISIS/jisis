/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.z3950;

/**
 *
 * @author jcd
 */
public class Marc21Constants {



	/**
	 * Default MARC Schema file. The MARCXML will be validated against it, if
	 * no other schema file is given in the command line with -marc_schema argument.
	 * The default file is: <a href="http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd">http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd</a>
	 */
	public static final String MARC21_SCHEMA_URL =
		"http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd";

	/** The prefix of the file where the error records are written */
	public static final String ERROR_FILE_PREFIX = "error_records_in_";



	/**
	 * Types of the result of import. Possible values are:
	 * <ul>
	 * <li>CREATED: A new record was created</li>
	 * <li>UPDATED: An old record was found and its value was updated</li>
	 * <li>SKIPPED: This record was skipped (the new and old was the same)</li>
	 * <li>INVALID: This record was invalid</li>
	 * <li>DELETED: The record was deleted</li>
	 * <li>BIBLIOGRAPHIC: The record was a bibliographic one</li>
	 * <li>AUTHORITY: The record was an authority one</li>
	 * <li>HOLDINGS: The record was a holding one</li>
	 * <li>CLASSIFICATION: The record was a classification one</li>
	 * <li>COMMUNITY: The record was a community one</li>
	 * <li>INVALID_FILES: The file was invalid</li>
	 * </ul>
	 */
	public enum ImportType {
		/** A new record created */
		CREATED,

		/** An old record was found and its value was updated */
		UPDATED,

		/** The record was skipped (the new and old was the same) */
		SKIPPED,

		/** The record was invalid */
		INVALID,

		/** The record was deleted */
		DELETED,

		/** The record was a bibliographic one */
		BIBLIOGRAPHIC,

		/** The record was an authority one */
		AUTHORITY,

		/** The record was a holding one */
		HOLDINGS,

		/** The record was an classification one */
		CLASSIFICATION,

		/** The record was an community one */
		COMMUNITY,

		/** The file was invalid */
		INVALID_FILES
	};

}
