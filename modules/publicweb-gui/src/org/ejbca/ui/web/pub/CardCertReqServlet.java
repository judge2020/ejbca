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
 
package org.ejbca.ui.web.pub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.ObjectNotFoundException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cesecore.authentication.tokens.AlwaysAllowLocalAuthenticationToken;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSession;
import org.cesecore.certificates.ca.CaSessionLocal;
import org.cesecore.certificates.ca.SignRequestException;
import org.cesecore.certificates.ca.SignRequestSignatureException;
import org.cesecore.certificates.certificate.CertificateStoreSessionLocal;
import org.cesecore.certificates.certificate.IllegalKeyException;
import org.cesecore.certificates.certificate.request.PKCS10RequestMessage;
import org.cesecore.certificates.certificate.request.ResponseMessage;
import org.cesecore.certificates.certificate.request.X509ResponseMessage;
import org.cesecore.certificates.certificateprofile.CertificateProfileSessionLocal;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.certificates.util.CertTools;
import org.cesecore.util.Base64;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.ejb.ca.sign.SignSession;
import org.ejbca.core.ejb.ca.sign.SignSessionLocal;
import org.ejbca.core.ejb.hardtoken.HardTokenSessionLocal;
import org.ejbca.core.ejb.ra.UserAdminSessionLocal;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.approval.ApprovalException;
import org.ejbca.core.model.approval.WaitingForApprovalException;
import org.ejbca.core.model.ca.AuthLoginException;
import org.ejbca.core.model.ca.AuthStatusException;
import org.ejbca.core.model.hardtoken.profiles.EIDProfile;
import org.ejbca.core.model.hardtoken.profiles.HardTokenProfile;
import org.ejbca.core.model.hardtoken.profiles.SwedishEIDProfile;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.util.RequestMessageUtils;

/**
 * Servlet used to install a private key with a corresponding certificate in a browser. A new
 * certificate is installed in the browser in following steps:<br>
 * 1. The key pair is generated by the browser. <br>
 * 2. The public part is sent to the servlet in a POST together with user info ("pkcs10|keygen",
 * "inst", "user", "password"). For internet explorer the public key is sent as a PKCS10
 * certificate request. <br>
 * 3. The new certificate is created by calling the RSASignSession session bean. <br>
 * 4. A page containing the new certificate and a script that installs it is returned to the
 * browser. <br>
 * 
 * <p></p>
 * 
 * <p>
 * The following initiation parameters are needed by this servlet: <br>
 * "responseTemplate" file that defines the response to the user (IE). It should have one line
 * with the text "cert =". This line is replaced with the new certificate. "keyStorePass".
 * Password needed to load the key-store. If this parameter is none existing it is assumed that no
 * password is needed. The path could be absolute or relative.<br>
 * </p>
 *
 * @author Original code by Lars Silven
 * @version $Id$
 */
public class CardCertReqServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final static Logger log = Logger.getLogger(CardCertReqServlet.class);

	@EJB
	private CaSessionLocal caSession;
	@EJB
	private CertificateStoreSessionLocal certificateStoreSession;
	@EJB
	private CertificateProfileSessionLocal certificateProfileSession;
	@EJB
	private HardTokenSessionLocal hardTokenSession;
	@EJB
	private SignSessionLocal signSession;
	@EJB
	private UserAdminSessionLocal userAdminSession;

    /**
     * Servlet init
     *
     * @param config servlet configuration
     *
     * @throws ServletException on error
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            // Install BouncyCastle provider
        	CryptoProviderTools.installBCProvider();
        } catch( Exception e ) {
            throw new ServletException(e);
        }
    }

    /**
     * Handles HTTP POST
     *
     * @param request servlet request
     * @param response servlet response
     *
     * @throws IOException input/output error
     * @throws ServletException on error
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        final ServletDebug debug = new ServletDebug(request, response);
        boolean usekeyrecovery = false;
        try {
			final AuthenticationToken administrator = new AlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("CardCertReqServlet: "+request.getRemoteAddr()));
            //Admin administrator = new Admin(Admin.TYPE_RA_USER);
            final String username; {
                Object o = request.getAttribute("javax.servlet.request.X509Certificate");
                final X509Certificate[] certs;
                if ( o!=null && o instanceof X509Certificate[] ) {
                    certs = (X509Certificate[])o;
                }
                else {
                    throw new AuthLoginException("No authenicating certificate");
                }
                boolean isRevoked = certificateStoreSession.isRevoked(certs[0].getIssuerDN().getName(),certs[0].getSerialNumber());
                if (isRevoked) {
                    throw new UserCertificateRevokedException(certs[0]);
                }
                username = certificateStoreSession.findUsernameByCertSerno(certs[0].getSerialNumber(), certs[0].getIssuerX500Principal().toString());
                if ( username==null || username.length()==0 ) {
                    throw new ObjectNotFoundException("Not possible to retrieve user name");
                }
            }
            log.debug("Got request for " + username + ".");
            debug.print("<h3>username: " + username + "</h3>");
            
            final EndEntityInformation data = userAdminSession.findUser(administrator, username);
            final X509Certificate notRevokedCerts[]; {
                Set<X509Certificate> set = new HashSet<X509Certificate>();
                for( Iterator<java.security.cert.Certificate> i = certificateStoreSession.findCertificatesByUsername(username).iterator(); i.hasNext(); ) {
                    Object o = i.next();
                    if ( o instanceof X509Certificate ) {
                        X509Certificate cert = (X509Certificate)o;
                        boolean isRevoked = certificateStoreSession.isRevoked(cert.getIssuerDN().getName(), cert.getSerialNumber());
                        if (!isRevoked) {
                            set.add(cert);
                        }
                    }
                }
                notRevokedCerts = (X509Certificate[])set.toArray(new X509Certificate[0]);
            }
            if (data == null) {
                throw new ObjectNotFoundException();
            }
            final String authReq = request.getParameter("authpkcs10");
            final String signReq = request.getParameter("signpkcs10");
            
            if ( authReq!=null && signReq!=null ) {
                final int authCertProfile;
                final int signCertProfile;
                final HardTokenProfile hardTokenProfile = hardTokenSession.getHardTokenProfile(administrator, data.getTokenType());
                {
                    CertProfileID certProfileID = new CertProfileID(data, administrator, hardTokenProfile);
                    authCertProfile = certProfileID.getProfileID("authCertProfile", SwedishEIDProfile.CERTUSAGE_AUTHENC);
                    signCertProfile = certProfileID.getProfileID("signCertProfile", SwedishEIDProfile.CERTUSAGE_SIGN);
                }
                final int authCA;
                final int signCA;
                {
                    CAID caid = new CAID(data,administrator, hardTokenProfile, caSession);
                    authCA = caid.getProfileID("authCA", SwedishEIDProfile.CERTUSAGE_AUTHENC);
                    signCA = caid.getProfileID("signCA", SwedishEIDProfile.CERTUSAGE_SIGN);
                }
                // if not IE, check if it's manual request
                final byte[] authReqBytes = authReq.getBytes();
                final byte[] signReqBytes = signReq.getBytes();
                if ( authReqBytes!=null && signReqBytes!=null) {
                	try {
                		userAdminSession.changeUser(administrator, username,data.getPassword(), data.getDN(), data.getSubjectAltName(),
                				data.getEmail(), true, data.getEndEntityProfileId(), authCertProfile, data.getType(),
                				SecConst.TOKEN_SOFT_BROWSERGEN, 0, data.getStatus(), authCA);
                		final byte[] authb64cert=pkcs10CertRequest(administrator, signSession, authReqBytes, username, data.getPassword());

                		userAdminSession.changeUser(administrator, username, data.getPassword(), data.getDN(), data.getSubjectAltName(),
                				data.getEmail(), true, data.getEndEntityProfileId(), signCertProfile, data.getType(),
                				SecConst.TOKEN_SOFT_BROWSERGEN, 0, UserDataConstants.STATUS_NEW, signCA);
                		final byte[] signb64cert=pkcs10CertRequest(administrator, signSession, signReqBytes, username, data.getPassword());


                		for (int i=0; i<notRevokedCerts.length; i++) {
                			try {
                				userAdminSession.revokeCert(administrator, notRevokedCerts[i].getSerialNumber(),
                						notRevokedCerts[i].getIssuerDN().toString(), RevokedCertInfo.REVOCATION_REASON_SUPERSEDED);
                			} catch (WaitingForApprovalException e) {
                				log.info("A request for approval to revoke " + username + "'s old certificate "+
                						notRevokedCerts[i].getSerialNumber().toString(16)+" was added.");
                			} catch (ApprovalException e) {
                				log.info("A request for approval to revoke " + username + "'s old certificate "+
                						notRevokedCerts[i].getSerialNumber().toString(16)+" already exists.");
                			}
                		}

                		sendCertificates(authb64cert, signb64cert, response,  getServletContext(),
                				getInitParameter("responseTemplate"), notRevokedCerts);
                	} catch( Throwable t ) {
                        if (t instanceof Exception) {
                            throw (Exception)t;
                        }
                        else {
                            throw new Error(t);
                        }
                    } finally {
                        data.setStatus(UserDataConstants.STATUS_GENERATED);
                        userAdminSession.changeUser(administrator, data, true); // set back to original values
                    }
                }
            }
        } catch( UserCertificateRevokedException e) {
            log.error("An error revoking certificaates occured: ", e);
            debug.printMessage(e.getMessage());
            debug.printDebugInfo();
            return;
        } catch (ObjectNotFoundException oe) {
            log.error("Non existent username!", oe);
            debug.printMessage("Non existent username!");
            debug.printDebugInfo();
            return;
        } catch (AuthStatusException ase) {
            log.error("Wrong user status!", ase);
            debug.printMessage("Wrong user status!");
            if (usekeyrecovery) {
                debug.printMessage(
                "To generate a certificate for a user the user must have status new, failed or inprocess.");
            } else {
                debug.printMessage(
                "To generate a certificate for a user the user must have status new, failed or inprocess.");
            }
            debug.printDebugInfo();
            return;
        } catch (AuthLoginException ale) {
            log.error("Wrong password for user!", ale);
            debug.printMessage("Wrong username or password!");
            debug.printDebugInfo();
            return;
        } catch (SignRequestException re) {
            log.error("Invalid request!", re);
            debug.printMessage("Invalid request!");
            debug.printMessage("Please supply a correct request.");
            debug.printDebugInfo();
            return;
        } catch (SignRequestSignatureException se) {
            log.error("Invalid signature on certificate request!", se);
            debug.printMessage("Invalid signature on certificate request!");
            debug.printMessage("Please supply a correctly signed request.");
            debug.printDebugInfo();
            return;
        } catch (java.lang.ArrayIndexOutOfBoundsException ae) {
            log.error("Empty or invalid request received.", ae);
            debug.printMessage("Empty or invalid request!");
            debug.printMessage("Please supply a correct request.");
            debug.printDebugInfo();
            return;
        } catch (IllegalKeyException e) {
            log.error("Illegal Key received: ", e);
            debug.printMessage("Invalid Key in request: "+e.getMessage());
            debug.printMessage("Please supply a correct request.");
            debug.printDebugInfo();
            return;
        } catch (Exception e) {
            log.error("Exception occured: ", e);
            debug.print("<h3>parameter name and values: </h3>");
            Enumeration paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement().toString();
                String parameter = request.getParameter(name);
                debug.print("<h4>" + name + ":</h4>" + parameter + "<br>");
            }
            debug.takeCareOfException(e);
            debug.printDebugInfo();
        }
    } //doPost

    private class UserCertificateRevokedException extends Exception {
		private static final long serialVersionUID = 1L;

		UserCertificateRevokedException(X509Certificate cert) {
            super("User certificate with serial number "+cert.getSerialNumber() +
                  " from issuer \'"+cert.getIssuerX500Principal()+"\' is revoked.");
        }
    }
    private class CAID extends BaseID {
        final private CaSession caSession;
        CAID(EndEntityInformation d, AuthenticationToken a, HardTokenProfile hardTokenProfile, CaSession caSession) {
            super(d, a, hardTokenProfile);
            this.caSession = caSession;                       
        }
        protected int getFromName(String name) {
        	int ret = 0;
			try {
				CAInfo caInfo = caSession.getCAInfo(administrator, name);
				ret = caInfo.getCAId();
			} catch (CADoesntExistsException e) {
				log.debug("CA does not exist: "+name);
			} catch (AuthorizationDeniedException e) {
				log.debug("Not authorized to CA: "+name);
			}
			return ret;
        }
        protected int getFromOldData() {
            return data.getCAId();
        }
        protected int getFromHardToken(int keyType) {
            final int id = hardTokenProfile.getCAId(keyType);
            if ( id!=EIDProfile.CAID_USEUSERDEFINED ) {
                return id;
            } else {
                return data.getCAId();
            }
        }
    }
    private class CertProfileID extends BaseID {
        CertProfileID(EndEntityInformation d, AuthenticationToken a,
                      HardTokenProfile hardTokenProfile) {
            super(d, a, hardTokenProfile);
        }
        protected int getFromName(String name) {
            return certificateProfileSession.getCertificateProfileId(name);
        }
        protected int getFromOldData() {
            return data.getCertificateProfileId();
        }
        protected int getFromHardToken(int keyType) {
            return hardTokenProfile.getCertificateProfileId(keyType);
        }
    }
    private abstract class BaseID {
        final EndEntityInformation data;
        final AuthenticationToken administrator;
        final EIDProfile hardTokenProfile;
        
        protected abstract int getFromHardToken(int keyType);
        protected abstract int getFromName(String name);
        protected abstract int getFromOldData();
        BaseID(EndEntityInformation d, AuthenticationToken a, HardTokenProfile htp) {
            data = d;
            administrator = a;
            if ( htp!=null && htp instanceof EIDProfile ) {
                hardTokenProfile = (EIDProfile)htp;
            } else {
                hardTokenProfile = null;
            }
        }
        public int getProfileID(String parameterName, int keyType) {
            if ( hardTokenProfile!=null ) {
                return getFromHardToken(keyType);
            }
            String name = CardCertReqServlet.this.getInitParameter(parameterName);
            if ( name!=null && name.length()>0 ) {
                final int id = getFromName(name);
                log.debug("parameter name "+parameterName+" has ID "+id);
                if (id!=0) {
                    return id;
                }
            }
            return getFromOldData();
        }
    }
    /**
     * Handles HTTP GET
     *
     * @param request servlet request
     * @param response servlet response
     *
     * @throws IOException input/output error
     * @throws ServletException on error
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        log.trace(">doGet()");
        response.setHeader("Allow", "POST");

        ServletDebug debug = new ServletDebug(request, response);
        debug.print("The certificate request servlet only handles POST method.");
        debug.printDebugInfo();
        log.trace("<doGet()");
    }

    /**
     * Reads template and inserts cert to send back to netid for installation of cert
     *
     * @param b64cert cert to be installed in netid
     * @param response utput stream to send to
     * @param sc serveltcontext
     * @param responseTemplate path to responseTemplate
     * @param notRevokedCerts 
     * @param classid replace
     *
     * @throws Exception on error
     */
    private static void sendCertificates(byte[] authb64cert,byte[] signb64cert, HttpServletResponse response, ServletContext sc,
        String responseTemplate, X509Certificate[] notRevokedCerts) throws Exception {
        if (authb64cert.length == 0 || signb64cert.length == 0) {
            log.error("0 length certificate can not be sent to  client!");
            return;
        }
        StringWriter sw = new StringWriter();
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(sc.getResourceAsStream(responseTemplate)));
            PrintWriter pw = new PrintWriter(sw);
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                line = line.replaceAll("TAG_authb64cert",new String(authb64cert));
                line = line.replaceAll("TAG_signb64cert",new String(signb64cert));
                if ( notRevokedCerts.length > 0 ) {
                    line = line.replaceAll("TAG_certToRemove1",new String(Base64.encode(notRevokedCerts[0].getEncoded(),false)));
                }
                if ( notRevokedCerts.length > 1 ) {
                    line = line.replaceAll("TAG_certToRemove2",new String(Base64.encode(notRevokedCerts[1].getEncoded(),false)));
                }
                if ( notRevokedCerts.length > 2 ) {
                    line = line.replaceAll("TAG_certToRemove3",new String(Base64.encode(notRevokedCerts[2].getEncoded(),false)));
                }
                if ( notRevokedCerts.length > 3 ) {
                    line = line.replaceAll("TAG_certToRemove4",new String(Base64.encode(notRevokedCerts[3].getEncoded(),false)));
                }
                pw.println(line);
            }
            pw.close();
            sw.flush();
        }
        {
            OutputStream out = response.getOutputStream();
            PrintWriter pw = new PrintWriter(out);
            log.debug(sw);
            pw.print(sw);
            pw.close();
            out.flush();
        }
    } // sendCertificates
    
    /**
     * Handles PKCS10 certificate request, these are constructed as: <code> CertificationRequest
     * ::= SEQUENCE { certificationRequestInfo  CertificationRequestInfo, signatureAlgorithm
     * AlgorithmIdentifier{{ SignatureAlgorithms }}, signature                       BIT STRING }
     * CertificationRequestInfo ::= SEQUENCE { version             INTEGER { v1(0) } (v1,...),
     * subject             Name, subjectPKInfo   SubjectPublicKeyInfo{{ PKInfoAlgorithms }},
     * attributes          [0] Attributes{{ CRIAttributes }}} SubjectPublicKeyInfo { ALGORITHM :
     * IOSet} ::= SEQUENCE { algorithm           AlgorithmIdentifier {{IOSet}}, subjectPublicKey
     * BIT STRING }</code> PublicKey's encoded-format has to be RSA X.509.
     *
     * @param signsession signsession to get certificate from
     * @param b64Encoded base64 encoded pkcs10 request message
     * @param username username of requesting user
     * @param password password of requesting user
     * @param resulttype should indicate if a PKCS7 or just the certificate is wanted.
     *
     * @return Base64 encoded byte[] 
     */
    private byte[] pkcs10CertRequest(AuthenticationToken administrator, SignSession signsession, byte[] b64Encoded,
        String username, String password) throws Exception {
        byte[] result = null;	
        Certificate cert=null;
		PKCS10RequestMessage req = RequestMessageUtils.genPKCS10RequestMessage(b64Encoded);
		req.setUsername(username);
        req.setPassword(password);
        ResponseMessage resp = signsession.createCertificate(administrator, req, X509ResponseMessage.class, null);
        cert = CertTools.getCertfromByteArray(resp.getResponseMessage());
        result = cert.getEncoded();
        return Base64.encode(result, false);
    }
}
