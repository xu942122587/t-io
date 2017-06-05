/**
 * 
 */
package org.tio.examples.im.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;

/**
 * 
 * @author tanyaowu 
 * 2017年5月9日 上午11:46:36
 */
public class ChatRespHandler implements ImAioHandlerIntf
{
	private static Logger log = LoggerFactory.getLogger(ChatRespHandler.class);

	/**
	 * 
	 */
	public ChatRespHandler()
	{

	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	

	@Override
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) throws Exception
	{
		
		return null;
	}
}
