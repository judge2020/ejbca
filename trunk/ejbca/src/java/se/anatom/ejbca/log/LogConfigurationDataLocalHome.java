/*
 * Generated by XDoclet - Do not edit!
 */
package se.anatom.ejbca.log;

/**
 * Local home interface for LogConfigurationData.
 */
public interface LogConfigurationDataLocalHome
   extends javax.ejb.EJBLocalHome
{
   public static final String COMP_NAME="java:comp/env/ejb/LogConfigurationDataLocal";
   public static final String JNDI_NAME="LogConfigurationDataLocal";

   public se.anatom.ejbca.log.LogConfigurationDataLocal create(java.lang.Integer id , se.anatom.ejbca.log.LogConfiguration logConfiguration)
      throws javax.ejb.CreateException;

   public se.anatom.ejbca.log.LogConfigurationDataLocal findByPrimaryKey(java.lang.Integer pk)
      throws javax.ejb.FinderException;

}
