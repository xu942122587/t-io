package org.tio.examples.im.server.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.packets.AuthReqBody;
import org.tio.examples.im.common.packets.AuthRespBody;
import org.tio.examples.im.common.packets.Command;
import org.tio.examples.im.common.packets.DeviceType;


/**
 * 
 * 
 * @author tanyaowu 
 *
 */
public class AuthReqHandler implements ImBsHandlerIntf
{
	private static Logger log = LoggerFactory.getLogger(AuthReqHandler.class);

	//	private static final byte tokenIndex = 0;

	/**
	 * 
	 */
	public AuthReqHandler()
	{

	}

	@Override
	public Object handler(ImPacket packet, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) throws Exception
	{
		if (packet.getBody() == null)
		{
			throw new Exception("body is null");
		}	
		
		AuthReqBody authReqBody = AuthReqBody.parseFrom(packet.getBody());
		String token = authReqBody.getToken();
		String deviceId = authReqBody.getDeviceId();
		String deviceInfo = authReqBody.getDeviceInfo();
		Long seq = authReqBody.getSeq();
		DeviceType deviceType = authReqBody.getDeviceType();
		String sign = authReqBody.getSign();

		if (StringUtils.isBlank(deviceId))
		{
			Aio.close(channelContext, "did is null");
			return null;
		}

		if (seq == null || seq <= 0)
		{
			Aio.close(channelContext, "seq is null");
			return null;
		}

		token = token == null ? "" : token;
		deviceInfo = deviceInfo == null ? "" : deviceInfo;

		String data = token + deviceId + deviceInfo + seq + org.tio.examples.im.common.Const.authkey;

//		try
//		{
//			String _sign = Md5.getMD5(data);
//			if (!_sign.equals(sign))
//			{
//				log.error("sign is invalid, {}, actual sign:{},expect sign:{}", channelContext.toString(), sign, _sign);
//				Aio.close(channelContext, "sign is invalid");
//				return null;
//			}
//		} catch (Exception e)
//		{
//			log.error(e.toString(), e);
//			Aio.close(channelContext, e.getMessage());
//			return null;
//		}

		

		ImPacket imRespPacket = new ImPacket();
		AuthRespBody authRespBody = AuthRespBody.newBuilder().build();
		imRespPacket.setCommand(Command.COMMAND_AUTH_RESP);
		imRespPacket.setBody(authRespBody.toByteArray());
		Aio.send(channelContext, imRespPacket);
		return null;
	}



	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		
	}
}
