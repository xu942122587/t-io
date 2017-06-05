package org.tio.examples.im.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.AioClient;
import org.tio.client.ClientGroupContext;
import org.tio.client.ReconnConf;
import org.tio.client.intf.ClientAioHandler;
import org.tio.client.intf.ClientAioListener;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;

/**
 * 
 * @author tanyaowu 
 *
 */
public class ImClientStarter {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImClientStarter.class);

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * @throws IOException 
	 * 2016年11月17日 下午5:59:24
	 * 
	 */
	public ImClientStarter() throws IOException {
		aioClient = new AioClient<>(clientGroupContext);
	}

	private ClientAioHandler<ImSessionContext, ImPacket, Object> aioClientHandler = new ImClientAioHandler();
	private ClientAioListener<ImSessionContext, ImPacket, Object> aioListener = new ImClientAioListener();
	private static ReconnConf<ImSessionContext, ImPacket, Object> reconnConf = new ReconnConf<ImSessionContext, ImPacket, Object>(5000L);
	private ClientGroupContext<ImSessionContext, ImPacket, Object> clientGroupContext = new ClientGroupContext<>(aioClientHandler, aioListener, reconnConf);
	private AioClient<ImSessionContext, ImPacket, Object> aioClient = null;

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * @throws IOException 
	 * 2016年11月17日 下午5:59:24
	 * 
	 */
	public static void main(String[] args) throws Exception {
		org.tio.examples.im.client.ui.JFrameMain.main(args);
	}

	/**
	 * @return the aioClient
	 */
	public AioClient<ImSessionContext, ImPacket, Object> getAioClient() {
		return aioClient;
	}

	/**
	 * @return the clientGroupContext
	 */
	public ClientGroupContext<ImSessionContext, ImPacket, Object> getClientGroupContext() {
		return clientGroupContext;
	}

}
