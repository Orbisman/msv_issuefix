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
 * base interface of types to which length-related facets are applicable.
 * 
 * @author	Kohsuke Kawaguchi
 */
interface Discrete {
	/** count the number of item in value type.
	 * 
	 * Actual semantics of this method varies.
	 */
	public int countLength( Object value );
}
