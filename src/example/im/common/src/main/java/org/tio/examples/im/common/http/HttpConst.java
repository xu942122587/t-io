package org.tio.examples.im.common.http;

/**
 * 
 * @author tanyaowu 
 *
 */
public interface HttpConst
{
	/**
	 * 强制规定连接到本服务器的客户端统一用utf-8
	 */
	String CHARSET_NAME = "utf-8";
	
	/**
	 *         Accept-Language : zh-CN,zh;q=0.8
         Sec-WebSocket-Version : 13
      Sec-WebSocket-Extensions : permessage-deflate; client_max_window_bits
                       Upgrade : websocket
                          Host : t-io.org:9321
               Accept-Encoding : gzip, deflate, sdch
                    User-Agent : Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36
                        Origin : http://www.t-io.org:9292
             Sec-WebSocket-Key : kmCL2C7q9vtNSMyHpft7lw==
                    Connection : Upgrade
                 Cache-Control : no-cache
                        Pragma : no-cache
	 * 
	 * @author tanyaowu 
	 * 2017年5月27日 下午2:11:57
	 */
	public interface HttpRequestHeaderKey{
		
		
		String                   Cookie = "Cookie";//Cookie: $Version=1; Skin=new;
		String                   Origin = "Origin";    //http://127.0.0.1
		String        Sec_WebSocket_Key = "Sec-WebSocket-Key";    //2GFwqJ1Z37glm62YKKLUeA==
		String            Cache_Control = "Cache-Control";    //no-cache
		String               Connection = "Connection";    //Upgrade
		String               User_Agent = "User-Agent";    //Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3088.3 Safari/537.36
		String    Sec_WebSocket_Version = "Sec-WebSocket-Version";    //13
		String                     Host = "Host";    //127.0.0.1:9321
		String                   Pragma = "Pragma";    //no-cache
		String          Accept_Encoding = "Accept-Encoding";    //gzip, deflate, br
		String          Accept_Language = "Accept-Language";    //zh-CN,zh;q=0.8,en;q=0.6
		String                  Upgrade = "Upgrade";    //websocket
		String Sec_WebSocket_Extensions = "Sec-WebSocket-Extensions";    //permessage-deflate; client_max_window_bits
		
		String Content_Length = "Content-Length";    //65
		
	}
	
	public interface HttpResponseHeaderKey{
		//Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
		String                   Set_Cookie = "Set-Cookie";    //Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1

		String Content_Length = "Content-Length";    //65
		
	}
}
