package org.tio.examples.im.server.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.utils.page.Page;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.packets.ClientPageReqBody;
import org.tio.examples.im.common.packets.ClientPageRespBody;
import org.tio.examples.im.common.packets.Command;

public class ClientPageReqHandler implements ImBsHandlerIntf {
	private static Logger log = LoggerFactory.getLogger(ClientPageReqHandler.class);

	@Override
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) throws Exception {
		if (packet.getBody() == null) {
			Aio.remove(channelContext, "body is null");
			return null;
		}

		GroupContext<ImSessionContext, ImPacket, Object> groupContext = channelContext.getGroupContext();
//		ImSessionContext imSessionContext = channelContext.getSessionContext();

		ClientPageReqBody clientPageReqBody = ClientPageReqBody.parseFrom(packet.getBody());
		int pageIndex = clientPageReqBody.getPageIndex();
		int pageSize = clientPageReqBody.getPageSize();
		String group = clientPageReqBody.getGroup();

		Page<ChannelContext<ImSessionContext, ImPacket, Object>> page = null;

		if (StringUtils.isNotBlank(group)) {
			page = Aio.getPageOfGroup(groupContext, group, pageIndex, pageSize);
		} else {
			page = Aio.getPageOfConnecteds(groupContext, pageIndex, pageSize);
		}

		ClientPageRespBody.Builder clientPageRespBodyBuilder = ClientPageRespBody.newBuilder();
		clientPageRespBodyBuilder.setPageIndex(page.getPageIndex()).setPageSize(page.getPageSize()).setRecordCount(page.getRecordCount());

		List<ChannelContext<ImSessionContext, ImPacket, Object>> pageData = page.getPageData();
		if (pageData != null) {
			for (ChannelContext<ImSessionContext, ImPacket, Object> ele : pageData) {
				clientPageRespBodyBuilder.addClients(ele.getSessionContext().getClient());
			}
		}

		ClientPageRespBody clientPageRespBody = clientPageRespBodyBuilder.build();
		ImPacket respPacket = new ImPacket(Command.COMMAND_CLIENT_PAGE_RESP, clientPageRespBody.toByteArray());
		Aio.send(channelContext, respPacket);
		Aio.send(channelContext, respPacket);
		return null;
	}

}
