/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.datatype;


/**
 * "token" type.
 * 
 * type of the value object is <code>java.lang.String</code>.
 * See http://www.w3.org/TR/xmlschema-2/#token for the spec
 * 
 * @author Kohsuke KAWAGUCHI
 */
public class TokenType extends StringType {
	public static final TokenType theInstance = new TokenType("token");
	protected TokenType( String typeName ) {
		super(typeName,WhiteSpaceProcessor.theCollapse);
	}
}
