package org.tio.core.maintain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端ip集合
 * @author tanyaowu 
 * 2017年5月23日 下午2:40:20
 */
public class ClientIps<T> {
	private static Logger log = LoggerFactory.getLogger(ClientIps.class);
	
	private T context;

	/**
	 * 
	 * @author: tanyaowu
	 */
	public ClientIps(T context) {
		this.context = context;
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the context
	 */
	public T getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(T context) {
		this.context = context;
	}
}
