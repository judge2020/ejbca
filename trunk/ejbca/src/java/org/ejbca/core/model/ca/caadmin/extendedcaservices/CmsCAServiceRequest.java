/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
 
package org.ejbca.core.model.ca.caadmin.extendedcaservices;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Class used when requesting CMS related services from a CA.  
 *
 * @version $Id: CmsCAServiceRequest.java,v 1.2 2006-12-28 13:51:15 anatom Exp $
 */
public class CmsCAServiceRequest extends ExtendedCAServiceRequest implements Serializable {    
    
	public static final Logger m_log = Logger.getLogger(CmsCAServiceRequest.class);
	
    /**
     * Determines if a de-serialized file is compatible with this class.
     *
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     *
     */
    private static final long serialVersionUID = -762331405718560161L;
	
    private byte[] doc = null;
    private boolean sign = false;
    
    /** Constructor
     */                   
    public CmsCAServiceRequest(byte[] doc, boolean sign) {
        this.doc = doc;
        this.sign = sign;
    }
    public byte[] getDoc() {
        return doc;
    }  
    public boolean isSign() {
    	return sign;
    }
}
