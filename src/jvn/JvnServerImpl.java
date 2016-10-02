/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;



public class JvnServerImpl 	extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer{
	
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	
	// le Coordonateur
	private JvnRemoteCoord jRCoordonator;
		
	// la liste des objects de type jvnObject
	HashMap<Integer, JvnObject> listJObject;

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		this.listJObject = new HashMap();//<Integer, JvnObject>();
		this.jRCoordonator = (JvnRemoteCoord) Naming.lookup("//localhost:2016/Coordinator/");
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()	throws jvn.JvnException {
		// to be completed 
		try {
			this.jRCoordonator.jvnTerminate(js);
		} catch (RemoteException e) {
			System.out.println("Error : " + e.getMessage());
		}
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException { 
		// to be completed 
		JvnObject jObject = null;
		try {
			jObject = new JvnObjectImpl(o, this.jRCoordonator.jvnGetObjectId());
			listJObject.put(jObject.jvnGetObjectId(), jObject);
			System.out.println("jvnServerImpl - list Object Create: " + listJObject.toString());
		} catch (RemoteException e) {
			System.out.println("Error creation object : " + e.getMessage());
		}
		return jObject;
		//return null; 
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		// to be completed 
		try {
			this.jRCoordonator.jvnRegisterObject(jon, jo, js);
		} catch (RemoteException e) {
			System.out.println("Error creation object : " + e.getMessage());
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		JvnObject jObject = null;
		try{
			jObject = this.jRCoordonator.jvnLookupObject(jon,js);			
			//on ajoute l'object au server si different de null
			if(jObject != null){
				this.listJObject.put(jObject.jvnGetObjectId(),jObject);
			}
			System.out.println(": JvnServerImpl -> jvnLookupObject : ok");
		} catch (Exception e) {
			System.out.println("Error jvnLookupObject : " + e.getMessage());
		}
		return jObject;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi) throws JvnException {
	   	Serializable object = null;
		try {
			object = this.jRCoordonator.jvnLockRead(joi, js);
		} catch (RemoteException e) {
			System.out.println("Error jvnLockRead : " + e.getMessage());
		}
		return object;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi) throws JvnException {
	   	Serializable object = null;
		try {
			object = this.jRCoordonator.jvnLockWrite(joi, js);
		} catch (RemoteException e) {
			System.out.println("Error jvnLockWrite : " + e.getMessage());
		}
		return object;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
   	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,jvn.JvnException {
   		this.listJObject.get(joi).jvnInvalidateReader();
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		return this.listJObject.get(joi).jvnInvalidateWriter();
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		return this.listJObject.get(joi).jvnInvalidateWriterForReader();
	};

}

 
