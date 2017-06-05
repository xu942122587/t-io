package org.tio.examples.im.common.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;

import nl.basjes.parse.useragent.UserAgent;

/**
 * 
 * @author tanyaowu 
 *
 */
public class HttpRequestPacket extends ImPacket {
	
	private static Logger log = LoggerFactory.getLogger(HttpRequestPacket.class);

	
	private RequestLine requestLine = null;
	private Map<String, String> headers = null;
	private List<Cookie> cookies = null;
	private Map<String, Cookie> cookieMap = null;
	private int contentLength;
	private byte[] httpRequestBody;
	private UserAgent userAgent;

	public static class RequestLine {
		private String method;
		private String requestUrl;
		private String queryStr; //譬如http://www.163.com?name=tan&id=789，那些此值就是name=tan&id=789
		private String version;
		private String initStr;

		/**
		 * @return the method
		 */
		public String getMethod() {
			return method;
		}

		/**
		 * @param method the method to set
		 */
		public void setMethod(String method) {
			this.method = method;
		}

		/**
		 * @return the requestUrl
		 */
		public String getRequestUrl() {
			return requestUrl;
		}

		/**
		 * @param requestUrl the requestUrl to set
		 */
		public void setRequestUrl(String requestUrl) {
			this.requestUrl = requestUrl;
		}

		/**
		 * @return the version
		 */
		public String getVersion() {
			return version;
		}

		/**
		 * @param version the version to set
		 */
		public void setVersion(String version) {
			this.version = version;
		}

		/**
		 * @return the initStr
		 */
		public String getInitStr() {
			return initStr;
		}

		/**
		 * @param initStr the initStr to set
		 */
		public void setInitStr(String initStr) {
			this.initStr = initStr;
		}

		/**
		 * @return the queryStr
		 */
		public String getQueryStr() {
			return queryStr;
		}

		/**
		 * @param queryStr the queryStr to set
		 */
		public void setQueryStr(String queryStr) {
			this.queryStr = queryStr;
		}
	}

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * 2017年2月22日 下午4:14:40
	 * 
	 */
	public HttpRequestPacket() {
	}

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * 2017年2月22日 下午4:14:40
	 * 
	 */
	public static void main(String[] args) {
	}

	public Cookie getCookieByName(String cooiename){
		if (cookieMap == null) {
			return null;
		}
		return cookieMap.get(cooiename);
	}
	
	/**
	 * @return the firstLine
	 */
	public RequestLine getRequestLine() {
		return requestLine;
	}

	/**
	 * @param requestLine the requestLine to set
	 */
	public void setRequestLine(RequestLine requestLine) {
		this.requestLine = requestLine;
	}

	/**
	 * @return the headers
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * 设置好header后，会把cookie等头部信息也设置好
	 * @param headers the headers to set
	 * @param channelContext 
	 */
	public void setHeaders(Map<String, String> headers, ChannelContext<ImSessionContext, ImPacket, Object> channelContext) {
		this.headers = headers;
		if (headers != null) {
			parseCookie(channelContext);
		}
	}

	public void parseCookie( ChannelContext<ImSessionContext, ImPacket, Object> channelContext) {
		String cookieline = headers.get(HttpConst.HttpRequestHeaderKey.Cookie);
		if (StringUtils.isNotBlank(cookieline)) {
			cookies = new ArrayList<>();
			cookieMap = new HashMap<>();
			Map<String, String> _cookiemap = Cookie.getEqualMap(cookieline);
			List<Map<String, String>> cookieListMap = new ArrayList<Map<String, String>>();
			for (Entry<String, String> cookieMapEntry : _cookiemap.entrySet()) {
				HashMap<String, String> cookieOneMap = new HashMap<String, String>();
				cookieOneMap.put(cookieMapEntry.getKey(), cookieMapEntry.getValue());
				cookieListMap.add(cookieOneMap);

				Cookie cookie = Cookie.buildCookie(cookieOneMap);
				cookies.add(cookie);
				cookieMap.put(cookie.getName(), cookie);
				log.error("{}, 收到cookie:{}", channelContext, cookie.toString());
			}
		}
	}

	/**
	 * @return the bodyLength
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * @param bodyLength the bodyLength to set
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * @return the httpRequestBody
	 */
	public byte[] getHttpRequestBody() {
		return httpRequestBody;
	}

	/**
	 * @param httpRequestBody the httpRequestBody to set
	 */
	public void setHttpRequestBody(byte[] httpRequestBody) {
		this.httpRequestBody = httpRequestBody;
	}

	/**
	 * @return the userAgent
	 */
	public UserAgent getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(UserAgent userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the cookies
	 */
	public List<Cookie> getCookies() {
		return cookies;
	}

	/**
	 * @param cookies the cookies to set
	 */
	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}

	/**
	 * @return the cookieMap
	 */
	public Map<String, Cookie> getCookieMap() {
		return cookieMap;
	}

	/**
	 * @param cookieMap the cookieMap to set
	 */
	public void setCookieMap(Map<String, Cookie> cookieMap) {
		this.cookieMap = cookieMap;
	}

}
