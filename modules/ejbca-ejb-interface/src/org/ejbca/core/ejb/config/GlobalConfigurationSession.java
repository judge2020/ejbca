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
package org.ejbca.core.ejb.config;

import java.util.Properties;

import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.ejbca.config.GlobalConfiguration;

/** 
 * Session bean to handle global configuration and such.
 * 
 * @version $Id$
 */
public interface GlobalConfigurationSession {

    /**
     * Flushes the cached GlobalConfiguration value and reads the current one
     * from persistence.
     * 
     * @return a fresh GlobalConfiguration from persistence, or null of no such
     *         configuration exists.
     */
    GlobalConfiguration flushCache();
    
    /**
     * Retrieves the cached GlobalConfiguration. This cache is updated from
     * persistence either by the time specified by
     * {@link #MIN_TIME_BETWEEN_GLOBCONF_UPDATES} or when {@link #flushCache()}
     * is executed. This method should be used in all cases where a quick
     * response isn't necessary, otherwise use {@link #flushCache()}.
     * 
     * @return the cached GlobalConfiguration value.
     */
    GlobalConfiguration getCachedGlobalConfiguration();

    /** Clear and load global configuration cache. */
    void flushGlobalConfigurationCache();
    
    /** @return all currently used properties (configured in conf/*.properties.
     * Required admin access to '/' to dump these properties. 
     */
    Properties getAllProperties(AuthenticationToken admin) throws AuthorizationDeniedException;

}
