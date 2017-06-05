package org.tio.examples.im.server.handler;

import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.utils.SystemTimer;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.packets.ChatReqBody;
import org.tio.examples.im.common.packets.ChatRespBody;
import org.tio.examples.im.common.packets.ChatType;
import org.tio.examples.im.common.packets.Client;
import org.tio.examples.im.common.packets.Command;
import org.tio.examples.im.common.utils.ImUtils;
import org.tio.examples.im.service.BadWordService;
import org.tio.examples.im.service.ImgFjService;
import org.tio.examples.im.service.ImgMnService;

/**
 * 
 * 
 * @author tanyaowu 
 *
 */
public class ChatReqHandler implements ImBsHandlerIntf {
	private static Logger log = LoggerFactory.getLogger(ChatReqHandler.class);
	private static Logger chatlog = LoggerFactory.getLogger("tio-chatxxxx-trace-log");

	private static final String CHAT_COMMAND_PREFIX = "c:"; //聊天命令前缀

	public static class ChatCommand {
		/**
		 * 显示一张美女图片的命令
		 */
		public static final String SHOW_MM_IMG = CHAT_COMMAND_PREFIX + "mm";

		/**
		 * 批量查看美女图片的命令
		 */
		public static final String SHOW_BATCH_MM_IMG = CHAT_COMMAND_PREFIX + "mm-10";
		/**
		 * 显示一张风景图片的命令
		 */
		public static final String SHOW_FJ_IMG = CHAT_COMMAND_PREFIX + "fj";

		/**
		 * 显示tio码云地址的命令
		 */
		public static final String SHOW_TIO_IN_MAYUN_IMG = CHAT_COMMAND_PREFIX + "tio";

		/**
		 * 五方会议的命令
		 */
		public static final String SHOW_WFHT_IMG = "五方会谈";

		/**
		 * 来电话了的命令
		 */
		public static final String SHOW_LDHL_IMG = "来电话了";
	}

	/*
	 * 敏感词的替换词
	 */
	public static final String replaceText = "<span style='color:#ee3344;padding:4px;border:1px solid #ee3344;border-radius:5px;margin:4px 4px;'><a href='http://www.gov.cn' target='_blank'>此处为敏感词</a></span>";

	@Override
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) throws Exception {

		if (packet.getBody() == null) {
			throw new Exception("body is null");
		}

		ImSessionContext imSessionContext = channelContext.getSessionContext();
		Client currClient = imSessionContext.getClient();
		String formatedUserAgent = ImUtils.formatUserAgent(channelContext);

		ChatReqBody chatReqBody = ChatReqBody.parseFrom(packet.getBody());

		if (chatReqBody != null) {
			String toId = chatReqBody.getToId();
			String text = chatReqBody.getText();
			if (StringUtils.isBlank(text)) {
				return null;
			}

			String region = StringUtils.rightPad(imSessionContext.getDataBlock().getRegion(), 10);
			String clientNodeStr = StringUtils.rightPad(channelContext.getClientNode().toString(), 21);
			String userId = StringUtils.rightPad(currClient.getUser().getId() + "", 11);
			String userNick = StringUtils.rightPad(currClient.getUser().getNick(), 8);
			chatlog.info("{} {} {} {} {} \r\n【{}】\r\n", region, clientNodeStr, userId, userNick, formatedUserAgent, text);

			String newText = handlerChatCommand(text);
			if (newText == null) { //不是指令，则需要转义过滤等操作
				text = StringEscapeUtils.escapeHtml4(text);
				String text1 = BadWordService.replaceBadWord(text, replaceText, channelContext + " 【" + currClient.getRegion() + "】 【" + currClient.getUser().getNick() + "】");
				if (text1 != null) {  //有敏感词
					text = text1;
				} else {
					
				}
			} else {
				text = newText;
			}

			Client toClient = null;
			ChannelContext<ImSessionContext, ImPacket, Object> toChannelContext = (ChannelContext<ImSessionContext, ImPacket, Object>) Aio
					.getChannelContextById(channelContext.getGroupContext(), toId);
			if (toChannelContext != null) {
				toClient = toChannelContext.getSessionContext().getClient();
			}

			String toGroup = chatReqBody.getGroup();

			ChatRespBody.Builder builder = ChatRespBody.newBuilder();
			builder.setType(chatReqBody.getType());
			builder.setText(text);
			builder.setFromClient(currClient);

			if (toClient != null) {
				builder.setToClient(toClient);
			}

			builder.setGroup(toGroup);
			builder.setTime(SystemTimer.currentTimeMillis());
			ChatRespBody chatRespBody = builder.build();
			byte[] bodyByte = chatRespBody.toByteArray();

			ImPacket respPacket = new ImPacket();
			respPacket.setCommand(Command.COMMAND_CHAT_RESP);

			respPacket.setBody(bodyByte);

			//公聊则发往群里
			if (Objects.equals(ChatType.CHAT_TYPE_PUBLIC, chatReqBody.getType())) {
//				if (StringUtils.equals(ChatCommand.SHOW_BATCH_MM_IMG, text)) { //批量看美女，只发给自己，否则影响面太大
//					Aio.send(channelContext, respPacket);
//				} else {
					Aio.sendToGroup(channelContext.getGroupContext(), toGroup, respPacket);
//				}
				
			} else if (Objects.equals(ChatType.CHAT_TYPE_PRIVATE, chatReqBody.getType())) {
				if (toClient != null) {
					Aio.sendToId(channelContext.getGroupContext(), toId + "", respPacket);
				} else {
					log.info("用户不在线,channel id:{}", toId);
				}
			}
		}
		return null;
	}

	/**
	 * 处理聊天中的指令
	 * @param text
	 * @return
	 * @author: tanyaowu
	 */
	public static String handlerChatCommand(String text) {
		if (StringUtils.startsWith(text, CHAT_COMMAND_PREFIX) || ChatCommand.SHOW_WFHT_IMG.equals(text) || ChatCommand.SHOW_LDHL_IMG.equals(text)) {
			text = StringUtils.trim(text);
			text = StringUtils.replaceAll(text, "\r", "");
			text = StringUtils.replaceAll(text, "\n", "");

			if (StringUtils.equals(ChatCommand.SHOW_MM_IMG, text)) {
				String imgsrc = ImgMnService.nextImg();
				String href = imgsrc;
				String title = "点击查看大图";
				text = "<a alt='" + title + "' title='" + title + "' href='" + href + "' target='_blank'>" + title + "<br><img style='width:200px;height:100px;' src='" + imgsrc
						+ "'><br>" + title + "</a>";
				return text;
			} else if (StringUtils.equals(ChatCommand.SHOW_BATCH_MM_IMG, text)) {
				text = "";
				for (int i = 0; i < 3; i++) {
					text += "<div style='padding:4px;border:1px solid #5FB878;border-radius:5px;margin:4px 0px;'>";
					for (int j = 0; j < 3; j++) {
						String imgsrc = ImgMnService.nextImg();
						String href = imgsrc;
						String title = "点击查看大图";
						text += "<span style='padding:4px;border:1px solid #5FB878;border-radius:5px;margin:4px 0px;'>";
						text += "<a style='border-radius: 9px; -webkit-border-radius: 9px; -moz-border-radius: 9px;' alt='" + title + "' title='" + title + "' href='" + href + "' target='_blank'><img style='width:200px;height:100px;' src='" + imgsrc + "'></a>";
						text += "</span>";
					}
					text += "</div>";
				}
				return text;
			} else if (StringUtils.equals(ChatCommand.SHOW_FJ_IMG, text)) {
				String imgsrc = ImgFjService.nextImg();
				String href = imgsrc;
				String title = "点击查看大图";
				text = "<a alt='" + title + "' title='" + title + "' href='" + href + "' target='_blank'>" + title + "<br><img style='width:200px;height:100px;' src='" + imgsrc
						+ "'><br>" + title + "</a>";
				return text;
			} else if (StringUtils.equals(ChatCommand.SHOW_TIO_IN_MAYUN_IMG, text)) {
				String imgsrc = "https://git.oschina.net/tywo45/t-io/raw/master/docs/api/t-io-api.png";
				String href = "https://git.oschina.net/tywo45/t-io";
				String title = "点击带你去tio在码云的老巢";
				text = "<a alt='" + title + "' title='" + title + "' href='" + href + "' target='_blank'>" + title + "<br><img style='width:200px;height:100px;' src='" + imgsrc
						+ "'><br>" + title + "</a>";
				return text;
			} else if (StringUtils.equals(ChatCommand.SHOW_WFHT_IMG, text)) {
				String imgsrc = "https://git.oschina.net/tywo45/t-io/raw/master/docs/dl/1.png";
				String href = "http://www.dtcaas.com";
				String title = "点击带你去看高清无码的五方会谈";
				text = "<a alt='" + title + "' title='" + title + "' href='" + href + "' target='_blank'>" + title + "<br><img style='width:200px;height:100px;' src='" + imgsrc
						+ "'><br>" + title + "</a>";
				return text;
			} else if (StringUtils.equals(ChatCommand.SHOW_LDHL_IMG, text)) {
				String imgsrc = ImgMnService.nextImg();//"http://images.rednet.cn/articleimage/2013/01/23/1403536948.jpg";
				String href = "http://mp.weixin.qq.com/s/RSi8Au0n7UrlebVVYLGFGw";
				String title = "点击带你了解'来电话了'公众号";
				text = "<a alt='" + title + "' title='" + title + "' href='" + href + "' target='_blank'>" + title + "<br><img style='width:200px;height:100px;' src='" + imgsrc
						+ "'><br>" + title + "</a>";
				return text;
			}

		}
		return null;
	}
}
