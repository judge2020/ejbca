/*
 * Generated by XDoclet - Do not edit!
 */
package se.anatom.ejbca.ra;

/**
 * Remote interface for UserData.
 */
public interface UserDataRemote
   extends javax.ejb.EJBObject,UserDataConstants
{

   public java.lang.String getUsername(  )
      throws java.rmi.RemoteException;

   /**
    * username must be called 'striped' using StringTools.strip()
    */
   public void setUsername( java.lang.String username )
      throws java.rmi.RemoteException;

   public java.lang.String getSubjectDN(  )
      throws java.rmi.RemoteException;

   public void setSubjectDN( java.lang.String subjectDN )
      throws java.rmi.RemoteException;

   public int getCAId(  )
      throws java.rmi.RemoteException;

   public void setCAId( int caid )
      throws java.rmi.RemoteException;

   public java.lang.String getSubjectAltName(  )
      throws java.rmi.RemoteException;

   public void setSubjectAltName( java.lang.String subjectAltName )
      throws java.rmi.RemoteException;

   public java.lang.String getSubjectEmail(  )
      throws java.rmi.RemoteException;

   public void setSubjectEmail( java.lang.String subjectEmail )
      throws java.rmi.RemoteException;

   public int getStatus(  )
      throws java.rmi.RemoteException;

   public void setStatus( int status )
      throws java.rmi.RemoteException;

   public int getType(  )
      throws java.rmi.RemoteException;

   public void setType( int type )
      throws java.rmi.RemoteException;

   /**
    * Returns clear text password or null.
    */
   public java.lang.String getClearPassword(  )
      throws java.rmi.RemoteException;

   /**
    * Sets clear text password, the preferred method is setOpenPassword().
    */
   public void setClearPassword( java.lang.String clearPassword )
      throws java.rmi.RemoteException;

   /**
    * Returns hashed password or null.
    */
   public java.lang.String getPasswordHash(  )
      throws java.rmi.RemoteException;

   /**
    * Sets hash of password, this is the normal way to store passwords, but use the method setPassword() instead.
    */
   public void setPasswordHash( java.lang.String passwordHash )
      throws java.rmi.RemoteException;

   /**
    * Returns the time when the user was created.
    */
   public long getTimeCreated(  )
      throws java.rmi.RemoteException;

   /**
    * Returns the time when the user was last modified.
    */
   public long getTimeModified(  )
      throws java.rmi.RemoteException;

   /**
    * Sets the time when the user was last modified.
    */
   public void setTimeModified( long createtime )
      throws java.rmi.RemoteException;

   /**
    * Returns the end entity profile id the user belongs to.
    */
   public int getEndEntityProfileId(  )
      throws java.rmi.RemoteException;

   /**
    * Sets the end entity profile id the user should belong to. 0 if profileid is not applicable.
    */
   public void setEndEntityProfileId( int endentityprofileid )
      throws java.rmi.RemoteException;

   /**
    * Returns the certificate profile id that should be generated for the user.
    */
   public int getCertificateProfileId(  )
      throws java.rmi.RemoteException;

   /**
    * Sets the certificate profile id that should be generated for the user. 0 if profileid is not applicable.
    */
   public void setCertificateProfileId( int certificateprofileid )
      throws java.rmi.RemoteException;

   /**
    * Returns the token type id that should be generated for the user.
    */
   public int getTokenType(  )
      throws java.rmi.RemoteException;

   /**
    * Sets the token type that should be generated for the user. Available token types can be found in SecConst.
    */
   public void setTokenType( int tokentype )
      throws java.rmi.RemoteException;

   /**
    * Returns the hard token issuer id that should genererate for the users hard token.
    */
   public int getHardTokenIssuerId(  )
      throws java.rmi.RemoteException;

   /**
    * Sets tthe hard token issuer id that should genererate for the users hard token. 0 if issuerid is not applicable.
    */
   public void setHardTokenIssuerId( int hardtokenissuerid )
      throws java.rmi.RemoteException;

   /**
    * Function that sets the BCDN representation of the string.
    */
   public void setDN( java.lang.String dn )
      throws java.rmi.RemoteException;

   /**
    * Sets password in ahsed form in the database, this way it cannot be read in clear form
    */
   public void setPassword( java.lang.String password )
      throws java.security.NoSuchAlgorithmException, java.rmi.RemoteException;

   /**
    * Sets the password in clear form in the database, needed for machine processing, also sets the hashed password to the same value
    */
   public void setOpenPassword( java.lang.String password )
      throws java.security.NoSuchAlgorithmException, java.rmi.RemoteException;

   /**
    * Verifies password by verifying against passwordhash
    */
   public boolean comparePassword( java.lang.String password )
      throws java.security.NoSuchAlgorithmException, java.rmi.RemoteException;

   /**
    * Non-searchable information about a user. for future use.
    */
   public se.anatom.ejbca.ra.ExtendedInformation getExtendedInformation(  )
      throws java.rmi.RemoteException;

   /**
    * Non-searchable information about a user. for future use.
    */
   public void setExtendedInformation( se.anatom.ejbca.ra.ExtendedInformation extendedinformation )
      throws java.rmi.RemoteException;

}
