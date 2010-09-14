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
package org.ejbca.core.ejb.log;

import javax.ejb.Remote;

/**
 * Remote interface for ProtectedLogSession.
 * 
 * @deprecated
 */
@Remote
public interface ProtectedLogSessionRemote extends ProtectedLogSession {

	/**
	 * Set the last cause identifier if ProtectedLogTestAction is used. This method is only used for testing.
	 */
	public void setLastTestActionCause(String causeIdentifier);

	/**
	 * @return the last cause identifier if ProtectedLogTestAction is used. This method is only used for testing.
	 */
	public String getLastTestActionCause();
}
