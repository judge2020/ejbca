package se.anatom.ejbca.hardtoken.hardtokenprofiles;

import java.awt.print.PrinterException;
import java.io.IOException;

import se.anatom.ejbca.ra.UserAdminData;



/**
 * Interface contating methods that need to be implementet in order 
 * to have a hard token profile contain PIN Envelope settings.
 * 
 * @version $Id: IPINEnvelopeSettings.java,v 1.1 2003-12-05 14:50:27 herrvendil Exp $
 */

public interface IPINEnvelopeSettings {


	/**
	 * Constant indicating that no envelope should be printed.
	 */    
	public static int PINENVELOPETYPE_NONE = 0;
    /**
     * Constant indicating that a general envelope type should be printed.
     */    
    public static int PINENVELOPETYPE_GENERALENVELOBE = 1;
    
    /**      
     * @return the type of PIN envelope to print.
     */
    public abstract int getPINEnvelopeType();    

	/**      
	 * sets the pin envelope type.
	 */
	public abstract void setPINEnvelopeType(int pinenvelopetype);    
    
    /**
     * @return the filename of the current PIN envelope template.
     */
    public abstract String getPINEnvelopeTemplateFilename();

	/**
	 * Sets the filename of the current PIN envelope template.
	 */    
	public abstract void setPINEnvelopeTemplateFilename(String filename);
    
    /**
     * @return the data of the PIN Envelope template.
     */
    public abstract String getPINEnvelopeData();
    
    /**
     * Sets the data of the PIN envelope template.
     */
    public abstract void setPINEnvelopeData(String data);

    /**
     * @return the number of copies of this PIN Envelope that should be printed.
     */
    public abstract int getNumberOfPINEnvelopeCopies();

	/**
	 * Sets the number of copies of this PIN Envelope that should be printed.
	 */
	public abstract void setNumberOfPINEnvelopeCopies(int copies);

	/**
	 * @return the validity of the visual layout in days.
	 */
	public abstract int getVisualValidity();

	/**
	 * Sets the validity of the visual layout in days.
	 */
	public abstract void setVisualValidity(int validity);

   /**
    * Method that parses the template, replaces the userdata
    * and returning a printable byte array 
    */	
	public abstract byte[] printPINEnvelope(UserAdminData userdata, 
	                                        String[] pincodes, String[] pukcodes,
	                                        String hardtokensn, String copyoftokensn) 
	                                          throws IOException, PrinterException;
}

