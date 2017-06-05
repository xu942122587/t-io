package org.tio.examples.im.server.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.http.Cookie;
import org.tio.examples.im.common.http.HttpConst;
import org.tio.examples.im.common.http.HttpRequestPacket;
import org.tio.examples.im.common.http.HttpResponsePacket;
import org.tio.examples.im.common.http.HttpResponseStatus;
import org.tio.examples.im.common.packets.Client;
import org.tio.examples.im.common.packets.Command;
import org.tio.examples.im.common.utils.BASE64Util;
import org.tio.examples.im.common.utils.ImUtils;
import org.tio.examples.im.common.utils.SHA1Util;

import nl.basjes.parse.useragent.UserAgent;

public class HandshakeReqHandler implements ImBsHandlerIntf {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(HandshakeReqHandler.class);
	private static Logger userAgentLog = LoggerFactory.getLogger("tio-userAgentxxxx-trace-log");

	public static final String COOKIE_NAME_FOR_SESSION = "tio-s";

	private ImPacket handshakeRespPacket = new ImPacket(Command.COMMAND_HANDSHAKE_RESP);

	@Override
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) throws Exception {
		ImSessionContext imSessionContext = channelContext.getSessionContext();
		imSessionContext.setHandshaked(true);

		boolean isWebsocket = imSessionContext.isWebsocket();
		if (isWebsocket) {
			HttpRequestPacket httpRequestPacket = (HttpRequestPacket) packet;
			Map<String, String> reqHeaders = httpRequestPacket.getHeaders();
			String cookie = reqHeaders.get(HttpConst.HttpRequestHeaderKey.Cookie);

			HttpResponsePacket httpResponsePacket = updateWebSocketProtocol(httpRequestPacket, channelContext);
			if (httpResponsePacket != null) {

				UserAgent userAgent = httpRequestPacket.getUserAgent();
				/*
				DeviceClass = Desktop
				DeviceName = Desktop
				DeviceBrand = Unknown
				DeviceCpu = Intel x86_64
				OperatingSystemClass = Desktop
				OperatingSystemName = Windows NT
				OperatingSystemVersion = Windows 7
				OperatingSystemNameVersion = Windows 7
				LayoutEngineClass = Browser
				LayoutEngineName = Blink
				LayoutEngineVersion = 60.0
				LayoutEngineVersionMajor = 60
				LayoutEngineNameVersion = Blink 60.0
				LayoutEngineNameVersionMajor = Blink 60
				AgentClass = Browser
				AgentName = Chrome
				AgentVersion = 60.0.3088.3
				AgentVersionMajor = 60
				AgentNameVersion = Chrome 60.0.3088.3
				AgentNameVersionMajor = Chrome 60
				*/
				if (userAgent != null) {

					String clientnode = StringUtils.rightPad(channelContext.getClientNode().toString(), 22);
					String id = StringUtils.leftPad(channelContext.getId(), 32);
					String formatedUserAgent = ImUtils.formatUserAgent(channelContext);
					userAgentLog.info("{} {} {}", clientnode, id, formatedUserAgent);

					Client client = Client.newBuilder(imSessionContext.getClient()).setUseragent(formatedUserAgent).build();
					imSessionContext.setClient(client);
				}

				httpResponsePacket.setCommand(Command.COMMAND_HANDSHAKE_RESP);

				Aio.send(channelContext, httpResponsePacket);
			} else {
				Aio.remove(channelContext, "不是websocket协议");
			}
		} else {
			Aio.send(channelContext, handshakeRespPacket);
		}

		return null;
	}

	/**
	 * 本方法摘自baseio: https://git.oschina.net/generallycloud/baseio<br>
	 * @param httpRequestPacket
	 * @return
	 *
	 * @author: tanyaowu
	 * 2017年2月23日 下午4:11:41
	 *
	 */
	public HttpResponsePacket updateWebSocketProtocol(HttpRequestPacket httpRequestPacket, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) {
		Map<String, String> headers = httpRequestPacket.getHeaders();

		String Sec_WebSocket_Key = headers.get("Sec-WebSocket-Key");

		if (StringUtils.isNotBlank(Sec_WebSocket_Key)) {
			String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			byte[] key_array = SHA1Util.SHA1(Sec_WebSocket_Key_Magic);
			String acceptKey = BASE64Util.byteArrayToBase64(key_array);
			HttpResponsePacket httpResponsePacket = new HttpResponsePacket();

			HttpResponseStatus httpResponseStatus = HttpResponseStatus.C101;
			httpResponsePacket.setHttpResponseStatus(httpResponseStatus);

			Map<String, String> respHeaders = new HashMap<>();
			respHeaders.put("Connection", "Upgrade");
			respHeaders.put("Upgrade", "WebSocket");
			respHeaders.put("Sec-WebSocket-Accept", acceptKey);
			httpResponsePacket.setHeaders(respHeaders);

			Cookie sessionCookie = httpRequestPacket.getCookieByName(COOKIE_NAME_FOR_SESSION);
			if (sessionCookie == null) {
				sessionCookie = new Cookie();
				sessionCookie.setDomain(StringUtils.split(headers.get(HttpConst.HttpRequestHeaderKey.Host), ":")[0]);
				sessionCookie.setName(COOKIE_NAME_FOR_SESSION);
				sessionCookie.setValue(channelContext.getId());
				httpResponsePacket.addCookie(sessionCookie);
			} else {
				//
			}
			
			ImSessionContext imSessionContext = channelContext.getSessionContext();
			imSessionContext.setToken(sessionCookie.getValue());


			return httpResponsePacket;
		}
		return null;
	}
}
