package org.tio.examples.im.server.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.utils.SystemTimer;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.packets.ChatRespBody;
import org.tio.examples.im.common.packets.ChatType;
import org.tio.examples.im.common.packets.Client;
import org.tio.examples.im.common.packets.Command;
import org.tio.examples.im.common.packets.LoginReqBody;
import org.tio.examples.im.common.packets.LoginRespBody;
import org.tio.examples.im.common.packets.User;
import org.tio.examples.im.service.UserService;

public class LoginReqHandler implements ImBsHandlerIntf {
	private static Logger log = LoggerFactory.getLogger(LoginReqHandler.class);

	@Override
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) throws Exception {
		if (packet.getBody() == null) {
			Aio.remove(channelContext, "body is null");
			return null;
		}

		ImSessionContext imSessionContext = channelContext.getSessionContext();
		String handshakeToken = imSessionContext.getToken();

		LoginReqBody loginReqBody = LoginReqBody.parseFrom(packet.getBody());
		String token = loginReqBody.getToken();
		String loginname = loginReqBody.getLoginname();
		String password = loginReqBody.getPassword();

		User user = null;
		if (!StringUtils.isBlank(handshakeToken)) {
			user = UserService.getUser(handshakeToken);
		}
		if (user == null) {
			if (!StringUtils.isBlank(loginname)) {
				user = UserService.getUser(loginname, password);
			} else if (!StringUtils.isBlank(token)) {
				user = UserService.getUser(token);
			}
		}

		if (user == null) {
			log.info("登录失败, loginname:{}, password:{}", loginname, password);
			Aio.remove(channelContext, "loginname and token is null");
			return null;
		}
		long userid = user.getId();
		GroupContext<ImSessionContext, ImPacket, Object> groupContext = channelContext.getGroupContext();
		ChannelContext<ImSessionContext, ImPacket, Object> bindedChannelContext = groupContext.users.find(userid + "");
		if (bindedChannelContext != null) {
			ChatRespBody.Builder builder = ChatRespBody.newBuilder();
			builder.setType(ChatType.CHAT_TYPE_PUBLIC);
			builder.setText("<div style='color:#ee3344'>系统检测到你已经开了多个窗口，请友好浏览^_^</div>");
			builder.setFromClient(org.tio.examples.im.service.UserService.sysClient);
//			builder.setGroup();
			builder.setTime(SystemTimer.currentTimeMillis());
			ChatRespBody chatRespBody = builder.build();
			ImPacket respPacket1 = new ImPacket(Command.COMMAND_CHAT_RESP, chatRespBody.toByteArray());
			Aio.send(channelContext, respPacket1);
		}

		LoginRespBody.Builder loginRespBodyBuilder = LoginRespBody.newBuilder();

		Aio.bindUser(channelContext, user.getId() + "");

		if (StringUtils.isBlank(token)) {
			token = UserService.newToken();
		}
		imSessionContext.setToken(token);

		Client client = imSessionContext.getClient().toBuilder().setUser(user).build();
		imSessionContext.setClient(client);

		loginRespBodyBuilder.setUser(user);
		loginRespBodyBuilder.setToken(token);

		LoginRespBody loginRespBody = loginRespBodyBuilder.build();
		byte[] bodyByte = loginRespBody.toByteArray();

		ImPacket respPacket = new ImPacket(Command.COMMAND_LOGIN_RESP, bodyByte);
		Aio.send(channelContext, respPacket);
		return null;
	}

}
