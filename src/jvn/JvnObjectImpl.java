package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
	
	//enum Verrou {NL, RC, WC, RT, WT, RTWC};
	
	int idObject;
	Verrou state;
	Serializable jObject;
	
	public JvnObjectImpl(Serializable o, int id){
		super();
		jObject = o;
		state = Verrou.WT; //Verrou.NL;
		this.idObject = id;
	}
	
	public Verrou getState(){
		return this.state;
	}
	public void setState(Verrou _ver){
		this.state = _ver;
	}
	public void setObjectState(Serializable _ver) {
		this.jObject = _ver;
	}

	public void jvnLockRead() throws JvnException {		
		if(state == Verrou.NL){
			jObject = JvnServerImpl.jvnGetServer().jvnLockRead(this.idObject);
			state = Verrou.RT;
		}else if(state == Verrou.WT){
			throw new JvnException("Error: tu ne peux etre en Ecriture et demander verrou en Lecture");
		}else if(state == Verrou.RC || state == Verrou.RT){
			state = Verrou.RT;
		}else if(state == Verrou.WC){
			state = Verrou.RTWC;
		}		
	}

	public void jvnLockWrite() throws JvnException {
		if(state == Verrou.RT){
			throw new JvnException("Error: tu ne peux etre en Lecture et demander verrou en Ecriture");
		}else if(state == Verrou.NL || state == Verrou.RC){
			jObject = JvnServerImpl.jvnGetServer().jvnLockWrite(idObject);
			state = Verrou.WT;
		}else if(state == Verrou.WT || state == Verrou.WC || state == Verrou.RTWC){
			state = Verrou.WT;
		}		
	}
	

	public synchronized void jvnUnLock() throws JvnException {
		if(state == Verrou.NL ){//|| state == Verrou.RC || state == Verrou.WC){
			
		}else if(state == Verrou.RT || state == Verrou.RC){
			state = Verrou.RC;
			//notify();
		}else{
			state = Verrou.WC;
			//notify();
		}		
	}
	

	public int jvnGetObjectId() throws JvnException {
		return idObject;
	}
	

	public Serializable jvnGetObjectState() throws JvnException {
		return this.jObject;
	}
	

	public synchronized void jvnInvalidateReader() throws JvnException {
		if(state == Verrou.WT || state == Verrou.WC || state == Verrou.RTWC){
			throw new JvnException("verrou : "+state);
		}else if(state == Verrou.NL || state == Verrou.RC){
			state = Verrou.NL;
		}else{
			while (state == Verrou.RT) {
				try {
					wait();					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				state = Verrou.NL;
			}
		}		
	}
	

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		/*if(state == Verrou.NL || state == Verrou.RC){
			throw new JvnException("verrou : "+state);
		}else*/ 
		if(state == Verrou.NL || state == Verrou.RC || state == Verrou.WC){
			state = Verrou.NL;
		}else{
			while(state == Verrou.RT  || state == Verrou.WT || state==Verrou.RTWC){		
				try {
					wait();
					//state = Verrou.NL;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				state = Verrou.NL;
			}
		}
		return jObject;
	}
	

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		if(state == Verrou.NL || state == Verrou.WC ){
			state = Verrou.NL;
		}else if(state == Verrou.RC){
			
		}else if(state == Verrou.RT || state == Verrou.RTWC){
			state = Verrou.RT;
		}else{
			while(state == Verrou.WT){
				try {
					wait();
					//state = Verrou.NL;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
			state = Verrou.WC;
		}
		return jObject;
	}

	

}
