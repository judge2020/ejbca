
package se.anatom.ejbca;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * Home interface for JobRunner session.
 *
 * @version $Id: IJobRunnerSessionHome.java,v 1.3 2002-11-17 14:01:39 herrvendil Exp $
 */
public interface IJobRunnerSessionHome extends EJBHome {

    /**
     * Default create method. Maps to ejbCreate in implementation.
     * @throws CreateException
     * @throws RemoteException
     * @return IJobRunnerSessionRemote interface
     */
    IJobRunnerSessionRemote create() throws CreateException, RemoteException;
}
