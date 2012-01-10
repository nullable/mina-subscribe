package org.nullable.mina.subscribe;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public abstract class SubscribeIoHandlerAdapter extends IoHandlerAdapter {
	
	private Map<Class<?>, Method> methodCache;
	
	private void init(){
//		TODO: organize classes and interfaces into a coherent searchable structure 
//		      where bottom classes appear first followed by their interfaces, etc...
		
		methodCache = new HashMap<Class<?>, Method>();
		
		for(Method m : this.getClass().getMethods()){
			if(m.getName() != "comReceived") continue;
			
			Class<?>[] parameterTypes = m.getParameterTypes();
			if(parameterTypes.length != 2 && parameterTypes[0] != IoSession.class) continue;
			
			Class<?> messageClass = parameterTypes[1];
			methodCache.put(messageClass, m);
		}
		
	}
	
	private Method getMethodFromObject(Object message){
		if (this.methodCache == null) this.init();
		Method m = null;
		Class<?> c = message.getClass();
		
		if(this.methodCache.containsKey(c)){
			m = this.methodCache.get(c);
		}
		else{
			System.out.println("Entering the else:");
			for(Class<?> sc : this.methodCache.keySet()){
				System.out.println(sc);
				try{
					sc.cast(message);
					
					m = this.methodCache.get(sc);
					this.methodCache.put(c, m);
					break;
				}
				catch(ClassCastException e){
					
				}
			}
		}
		return m;
	}
	
	public abstract void comReceived(IoSession session, Object com) throws Exception;
	
	
	
    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception
    {
    	Method m = this.getMethodFromObject(message);
    	
    	//m is guaranteed not to be null
    	m.invoke(this, session, message);
    	
    }
    
    public static void main(String[] args) throws Exception {
		SubscribeIoHandlerAdapter sioha = new SubscribeIoHandlerAdapter() {
			
			@SuppressWarnings("unused")
			public void comReceived(IoSession session, A com) throws Exception{
				System.out.println("(A) Received: " + com +" | class: " + com.getClass());
			}
			
			@Override
			public void comReceived(IoSession session, Object com) throws Exception {
				System.out.println("(Object) Received: " + com +" | class: " + com.getClass());
			}
			

		};		
		sioha.messageReceived(null, 3);
	}

}
