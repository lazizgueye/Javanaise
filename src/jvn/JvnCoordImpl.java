/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * Contacts: 
 *  nicolas.bouscarle@centraliens-lille.org
 *  arnaud.panaiotis@e.ujf-grenoble.fr
 *
 * Authors: 
 *  Bouscarle Nicolas
 *  Panaiotis Arnaud 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

//import jvn.JvnObjectImpl.Verrou;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord{
    private static int cpt;
    private HashMap<Integer, JvnObject> listObject;
    private HashMap<String, Integer> listNomId;
    private HashMap<Integer, JvnRemoteServer> listServer;
    private HashMap<Integer, ArrayList<JvnRemoteServer>> listEtatServer;
        
  

    /**
    * Default constructor
    * @throws JvnException
    **/
    JvnCoordImpl() throws Exception {
        super();
        cpt = 0;
        listObject = new HashMap();
        listNomId = new HashMap();
        listServer = new HashMap();
        listEtatServer = new HashMap();
    }    

    /**
    *  Allocate a NEW JVN object id (usually allocated to a 
    *  newly created JVN object)
    * @throws java.rmi.RemoteException,JvnException
    **/
    public int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {       
		int idObject = cpt;
		cpt++;
		return idObject;
    }
  
    /**
    * Associate a symbolic name with a JVN object
    * @param jon : the JVN object name
    * @param jo  : the JVN object 
    * @param joi : the JVN object identification
    * @param js  : the remote reference of the JVNServer
    * @throws java.rmi.RemoteException,JvnException
    **/
    synchronized public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
        throws java.rmi.RemoteException,jvn.JvnException{
		try{
			this.listNomId.put(jon, jo.jvnGetObjectId());
			this.listObject.put(jo.jvnGetObjectId(), jo);
			this.listServer.put(jo.jvnGetObjectId(), js);
			ArrayList<JvnRemoteServer> jsList = new ArrayList();
			jsList.add(js);
			this.listEtatServer.put(jo.jvnGetObjectId(), jsList);
		}catch(Exception e){
			System.out.println("Error : JvnCoordImpl -> jvnRegisterObject : " + e.getMessage());
		}
    }
  
    /**
    * Get the reference of a JVN object managed by a given JVN server 
    * @param jon : the JVN object name
    * @param js : the remote reference of the JVNServer
    * @throws java.rmi.RemoteException,JvnException
    **/
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
    throws java.rmi.RemoteException,jvn.JvnException{
		JvnObject jObject = null;
		try{
			int id = this.listNomId.get(jon);
			if(listObject.get(id) != null){
				listObject.get(id).setState(Verrou.NL);
				jObject = listObject.get(id);
			}
		}catch(Exception e){
			System.out.println("Error : JvnCoordImpl -> jvnLookupObject  : " + e.getMessage());
		}
		return jObject;
    }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
  public Serializable jvnLockRead(int joi, JvnRemoteServer js)
  throws java.rmi.RemoteException, JvnException{
    Serializable object = listObject.get(joi).jvnGetObjectState();
        try {
            if (listServer.containsKey(joi)) {
                object = listServer.get(joi).jvnInvalidateWriterForReader(joi);
                listServer.remove(joi);
            }
        
            if (!listEtatServer.containsKey(joi)) {
                listEtatServer.put(joi, new ArrayList());
            }
        } catch (Exception e) {
			System.out.println("Error: JvnCoordImpl -> jvnLockRead :" +e.getMessage());
        }
      listEtatServer.get(joi).add(js);
      return object;
  }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
        Serializable object = listObject.get(joi).jvnGetObjectState();
        try {
            if (listServer.containsKey(joi)) {
                object = listServer.get(joi).jvnInvalidateWriter(joi);
                listObject.get(joi).setObjectState(object);
                listServer.remove(joi);
            }
            
			if (listEtatServer.get(joi) != null){
				for (JvnRemoteServer server : listEtatServer.get(joi)){
					if (server != js) {
						server.jvnInvalidateReader(joi);
					}
				}
				listEtatServer.remove(joi);
			}            
            listServer.put(joi, js);
        } catch (Exception e) {
			System.out.println("Error: JvnCoordImpl -> jnvLockWrite :" +e.getMessage());
        }
        return object;
    }
    
	
    /**
    * A JVN server terminates
    * @param js  : the remote reference of the server
    * @throws java.rmi.RemoteException, JvnException
    **/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {       
		try{
			listServer.remove(js);
			listEtatServer.remove(js);
			System.out.println(js + "tterminate");
		}catch (Exception e){
			System.out.println("Error: JvnCoordImpl -> jvnTerminate :" +e.getMessage());
		}
    }
	
	/** le main du Coodonnateur  **/
	public static void main(String[] args) {       
		try{
			LocateRegistry.createRegistry(2016);
			JvnCoordImpl cs = new JvnCoordImpl();
			Naming.rebind("//localhost:2016/Coordinator/", cs);
			System.out.println("le Coordonnateur est en marche");
		}catch(Exception e){
			e.printStackTrace();
		}
    }
}