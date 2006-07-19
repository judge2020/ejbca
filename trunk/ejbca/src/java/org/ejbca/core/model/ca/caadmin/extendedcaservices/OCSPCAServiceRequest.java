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
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.ocsp.OCSPReq;

/**
 * Class used when requesting OCSP related services from a CA.  
 *
 * @version $Id: OCSPCAServiceRequest.java,v 1.2 2006-07-19 14:05:45 anatom Exp $
 */
public class OCSPCAServiceRequest extends ExtendedCAServiceRequest implements Serializable {    
    
	public static final Logger m_log = Logger.getLogger(OCSPCAServiceRequest.class);
	
    private OCSPReq req = null;
    private ArrayList responseList = null;
    private X509Extensions exts = null;
    private String sigAlg = "SHA1WithRSA";
    private boolean useCACert = false;
    private boolean includeChain = true;
    
    /** Constructor for OCSPCAServiceRequest
     */                   
    public OCSPCAServiceRequest(OCSPReq req, ArrayList responseList, X509Extensions exts, String sigAlg, boolean useCACert, boolean includeChain) {
        this.req = req;
        this.responseList = responseList;
        this.exts = exts;
        this.sigAlg = sigAlg;       
        this.useCACert = useCACert;
        this.includeChain = includeChain;
    }
    public OCSPReq getOCSPrequest() {
        return req;
    }  
    public X509Extensions getExtensions() {
    	return exts;
    }
    public ArrayList getResponseList() {
    	return responseList;
    }
    public String getSigAlg() {
        return sigAlg;
    }
    /** If true, the CA certificate should be used to sign the OCSP response.
     * 
     * @return true if the CA cert should be used, false if the OCSPSigner cert shoudl be used.
     */
    public boolean useCACert() {
        return useCACert;
    }
    /** If true, the CA certificate chain is included in the response.
     * 
     * @return true if the CA cert chain should be included in the response.
     */
    public boolean includeChain() {
        return includeChain;
    }
}
