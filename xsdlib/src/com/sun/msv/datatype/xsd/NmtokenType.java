/*
 * Tranquilo : RELAX Verifier           written by Kohsuke Kawaguchi
 *                                           k-kawa@bigfoot.com
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.sun.tranquilo.datatype;

import com.sun.xml.util.XmlNames;

/**
 * "NMTOKEN" and string-derived types.
 * 
 * See http://www.w3.org/TR/xmlschema-2/#NMTOKEN for the spec
 */
public class NmtokenType extends TokenType
{
	public static final NmtokenType theInstance = new NmtokenType("NMTOKEN");
	protected NmtokenType(String typeName) { super(typeName); }
	
	public Object convertToValue( String content, ValidationContextProvider context )
	{
		if(XmlNames.isNmtoken(content))		return content;
		else								return null;
	}
}
