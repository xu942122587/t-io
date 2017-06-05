package org.tio.examples.im.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.examples.im.common.Const;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.utils.UserAgentAnalyzerFactory;
import org.tio.examples.im.service.BadWordService;
import org.tio.examples.im.service.ImgFjService;
import org.tio.examples.im.service.ImgMnService;
import org.tio.examples.im.service.ImgTxService;
import org.tio.server.AioServer;
import org.tio.server.ServerGroupContext;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 
 * @author tanyaowu 
 *
 */
public class ImServerStarter {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImServerStarter.class);

	public static Config conf = ConfigFactory.load("app.conf");

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * 2016年11月17日 下午5:59:24
	 * 
	 */
	public ImServerStarter() {

	}

	static ServerAioHandler<ImSessionContext, ImPacket, Object> aioHandler = new ImServerAioHandler();
	static ServerAioListener<ImSessionContext, ImPacket, Object> aioListener = new ImServerAioListener();
	static ImGroupListener imGroupListener = new ImGroupListener();
	static ServerGroupContext<ImSessionContext, ImPacket, Object> serverGroupContext = new ServerGroupContext<>(aioHandler, aioListener);
	static AioServer<ImSessionContext, ImPacket, Object> aioServer = new AioServer<>(serverGroupContext);
	static String bindIp = null;//"127.0.0.1";

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * @throws IOException 
	 * 2016年11月17日 下午5:59:24
	 * 
	 */
	public static void main(String[] args) throws IOException {
		if (conf.getBoolean("start.img.capture")) {
			ImgFjService.start();
			ImgMnService.start();
			ImgTxService.start();
		}

		if (conf.getBoolean("start.userAgent")) {
			//这货初始化比较慢，所以启动前就调用一下
			UserAgentAnalyzerFactory.getUserAgentAnalyzer();
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					UserAgentAnalyzerFactory.getUserAgentAnalyzer();
				}
			}).start();
		}
		
		BadWordService.initBadWord();

		serverGroupContext.setGroupListener(imGroupListener);
		aioServer.start(bindIp, Const.SERVER_PORT);
	}

}
