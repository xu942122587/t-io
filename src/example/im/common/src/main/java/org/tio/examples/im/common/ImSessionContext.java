package org.tio.examples.im.common;

import java.util.List;

import org.lionsoul.ip2region.DataBlock;
import org.tio.examples.im.common.http.HttpRequestPacket;
import org.tio.examples.im.common.packets.Client;
import org.tio.monitor.RateLimiterWrap;

/**
 * 
 * @author tanyaowu 
 *
 */
public class ImSessionContext
{
	/**
	 * 消息请求频率控制器
	 */
	private RateLimiterWrap requestRateLimiter = null;
	
	/**
	 * 是否已经握过手
	 */
	private boolean isHandshaked = false;
	
	/**
	 * ip地址信息
	 */
	private DataBlock dataBlock;
	
	/**
	 * 是否是走了websocket协议
	 */
	private boolean isWebsocket = false;
	
	
	
	/**
	 * websocket 握手包
	 */
	private HttpRequestPacket httpHandshakePacket = null;
	
	private Client client = null;
	
	private String token = null;
	
	
	//websocket 协议用到的，有时候数据包是分几个到的，注意那个fin字段，本im暂时不支持
	private List<byte[]> lastParts = null;
	
	
	/**
	 * 
	 *
	 * @author: tanyaowu
	 * 2017年2月21日 上午10:27:54
	 * 
	 */
	public ImSessionContext()
	{
		
	}

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * 2017年2月21日 上午10:27:54
	 * 
	 */
	public static void main(String[] args)
	{
		
	}



	/**
	 * @return the isWebsocket
	 */
	public boolean isWebsocket()
	{
		return isWebsocket;
	}

	/**
	 * @param isWebsocket the isWebsocket to set
	 */
	public void setWebsocket(boolean isWebsocket)
	{
		this.isWebsocket = isWebsocket;
	}

	/**
	 * @return the isHandshaked
	 */
	public boolean isHandshaked()
	{
		return isHandshaked;
	}

	/**
	 * @param isHandshaked the isHandshaked to set
	 */
	public void setHandshaked(boolean isHandshaked)
	{
		this.isHandshaked = isHandshaked;
	}

	/**
	 * @return the client
	 */
	public Client getClient()
	{
		return client;
	}

	/**
	 * @param client the client to set
	 */
	public void setClient(Client client)
	{
		this.client = client;
	}

	/**
	 * @return the token
	 */
	public String getToken()
	{
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token)
	{
		this.token = token;
	}

	/**
	 * @return the lastPart
	 */
	public List<byte[]> getLastParts() {
		return lastParts;
	}

	/**
	 * @param lastParts the lastPart to set
	 */
	public void setLastParts(List<byte[]> lastParts) {
		this.lastParts = lastParts;
	}

	/**
	 * @return the httpHandshakePacket
	 */
	public HttpRequestPacket getHttpHandshakePacket() {
		return httpHandshakePacket;
	}

	/**
	 * @param httpHandshakePacket the httpHandshakePacket to set
	 */
	public void setHttpHandshakePacket(HttpRequestPacket httpHandshakePacket) {
		this.httpHandshakePacket = httpHandshakePacket;
	}

	/**
	 * @return the dataBlock
	 */
	public DataBlock getDataBlock() {
		return dataBlock;
	}

	/**
	 * @param dataBlock the dataBlock to set
	 */
	public void setDataBlock(DataBlock dataBlock) {
		this.dataBlock = dataBlock;
	}

	/**
	 * @return the requestRateLimiter
	 */
	public RateLimiterWrap getRequestRateLimiter() {
		return requestRateLimiter;
	}

	/**
	 * @param requestRateLimiter the requestRateLimiter to set
	 */
	public void setRequestRateLimiter(RateLimiterWrap requestRateLimiter) {
		this.requestRateLimiter = requestRateLimiter;
	}


	
}
