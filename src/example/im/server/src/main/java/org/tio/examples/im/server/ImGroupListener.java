package org.tio.examples.im.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.intf.GroupListener;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.packets.Command;
import org.tio.examples.im.common.packets.ExitGroupNotifyRespBody;

/**
 * @author tanyaowu 
 * 2017年5月13日 下午10:38:36
 */
public class ImGroupListener implements GroupListener<ImSessionContext, ImPacket, Object>{
	private static Logger log = LoggerFactory.getLogger(ImGroupListener.class);

	/**
	 * 
	 * @author: tanyaowu
	 */
	public ImGroupListener() {
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/** 
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author: tanyaowu
	 */
	@Override
	public void onAfterBind(ChannelContext<ImSessionContext, ImPacket, Object> channelContext, String group) throws Exception {
	}

	/** 
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author: tanyaowu
	 */
	@Override
	public void onAfterUnbind(ChannelContext<ImSessionContext, ImPacket, Object> channelContext, String group) throws Exception {
		ImSessionContext imSessionContext = channelContext.getSessionContext();
		ExitGroupNotifyRespBody exitGroupNotifyRespBody = ExitGroupNotifyRespBody.newBuilder().setGroup(group).setClient(imSessionContext.getClient()).build();
		ImPacket respPacket2 = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, exitGroupNotifyRespBody.toByteArray());
		Aio.sendToGroup(channelContext.getGroupContext(), group, respPacket2);
	
	}
}
