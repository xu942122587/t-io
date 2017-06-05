package org.tio.examples.im.common.http;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.http.HttpRequestPacket.RequestLine;
import org.tio.examples.im.common.utils.UserAgentAnalyzerFactory;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

/**
 * 
 * @author tanyaowu 
 *
 */
public class HttpRequestDecoder {
	private static Logger log = LoggerFactory.getLogger(HttpRequestDecoder.class);

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * 2017年2月22日 下午4:06:42
	 * 
	 */
	public HttpRequestDecoder() {

	}

	public static final int MAX_HEADER_LENGTH = 20480;

	public static HttpRequestPacket decode(ByteBuffer buffer, ChannelContext<ImSessionContext, ImPacket, Object> channelContext
	) throws AioDecodeException {
		int count = 0;
		Step step = Step.firstline;
		StringBuilder currLine = new StringBuilder();
		Map<String, String> headers = new HashMap<>();
		int contentLength = 0;
		byte[] httpRequestBody = null;
		RequestLine firstLine = null;
		while (buffer.hasRemaining()) {
			count++;
			if (count > MAX_HEADER_LENGTH) {
				throw new AioDecodeException("max http header length " + MAX_HEADER_LENGTH);
			}

			byte b = buffer.get();

			if (b == '\n') {
				if (currLine.length() == 0) {
					String contentLengthStr = headers.get("Content-Length");
					if (StringUtils.isBlank(contentLengthStr)) {
						contentLength = 0;
					} else {
						contentLength = Integer.parseInt(contentLengthStr);
					}

					int readableLength = buffer.limit() - buffer.position();
					if (readableLength >= contentLength) {
						step = Step.body;
						break;
					} else {
						return null;
					}
				} else {
					if (step == Step.firstline) {
						firstLine = parseRequestLine(currLine.toString());
						step = Step.header;
					} else if (step == Step.header) {
						KeyValue keyValue = parseHeaderLine(currLine.toString());
						headers.put(keyValue.getKey(), keyValue.getValue());
					}

					currLine.setLength(0);
				}
				continue;
			} else if (b == '\r') {
				continue;
			} else {
				currLine.append((char) b);
			}
		}

		if (step != Step.body) {
			return null;
		}

		if (contentLength > 0) {
			httpRequestBody = new byte[contentLength];
			buffer.get(httpRequestBody);
		}

		if (!headers.containsKey(HttpConst.HttpRequestHeaderKey.Host)) {
			throw new AioDecodeException("there is no host header");
		}

		

		HttpRequestPacket httpRequestPacket = new HttpRequestPacket();
		httpRequestPacket.setHttpRequestBody(httpRequestBody);
		httpRequestPacket.setRequestLine(firstLine);
		httpRequestPacket.setHeaders(headers, channelContext);
		httpRequestPacket.setContentLength(contentLength);
		
		String User_Agent = headers.get(HttpConst.HttpRequestHeaderKey.User_Agent);
		if (StringUtils.isNotBlank(User_Agent)) {
			long start = System.currentTimeMillis();
			UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzerFactory.getUserAgentAnalyzer();
			UserAgent userAgent = userAgentAnalyzer.parse(User_Agent);
			httpRequestPacket.setUserAgent(userAgent);
			long end = System.currentTimeMillis();
			log.error((end - start) + "ms");
			
//			List<String> fieldNames = userAgent.getAvailableFieldNamesSorted();
//			for (String fieldName : fieldNames) {
//				System.out.println(fieldName + " = " + userAgent.getValue(fieldName));
//			}
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
		}

		StringBuilder logstr = new StringBuilder();
		logstr.append("\r\n------------------ websocket header start ------------------------\r\n");
		logstr.append(firstLine.getInitStr()).append("\r\n");
		Set<Entry<String, String>> entrySet = headers.entrySet();
		for (Entry<String, String> entry : entrySet) {
			logstr.append(StringUtils.leftPad(entry.getKey(), 30)).append(" : ").append(entry.getValue()).append("\r\n");
		}
		//		for (Entry<String, String> entry : entrySet) {
		//			logstr.append("String ").append(StringUtils.replaceAll(StringUtils.leftPad(entry.getKey(), 30), "-", "_")).append(" = \"").append(entry.getKey()).append("\";    //").append(entry.getValue()).append("\r\n");
		//		}
		logstr.append("------------------ websocket header start ------------------------\r\n");
		log.error(logstr.toString());

		return httpRequestPacket;

	}

	/**
	 * 解析第一行(请求行)
	 * @param line
	 * @return
	 *
	 * @author: tanyaowu
	 * 2017年2月23日 下午1:37:51
	 *
	 */
	public static RequestLine parseRequestLine(String line) {
		int index1 = line.indexOf(' ');
		String method = line.substring(0, index1);
		int index2 = line.indexOf(' ', index1 + 1);
		String requestUrl = line.substring(index1 + 1, index2);
		String queryStr = null;
		int indexOfQuestionmark = requestUrl.indexOf("?");
		if (indexOfQuestionmark != -1) {
			queryStr = StringUtils.substring(requestUrl, indexOfQuestionmark + 1);
		}
		
		String version = line.substring(index2 + 1);

		RequestLine requestLine = new RequestLine();
		requestLine.setMethod(method);
		requestLine.setRequestUrl(requestUrl);
		requestLine.setQueryStr(queryStr);
		requestLine.setVersion(version);
		requestLine.setInitStr(line);
		return requestLine;
	}

	/**
	 * 解析请求头的每一行
	 * @param line
	 * @return
	 *
	 * @author: tanyaowu
	 * 2017年2月23日 下午1:37:58
	 *
	 */
	public static KeyValue parseHeaderLine(String line) {
		KeyValue keyValue = new KeyValue();
		int p = line.indexOf(":");
		if (p == -1) {
			keyValue.setKey(line);
			return keyValue;
		}

		String name = line.substring(0, p).trim();
		String value = line.substring(p + 1).trim();

		keyValue.setKey(name);
		keyValue.setValue(value);

		return keyValue;
	}

	public static enum Step {
		firstline, header, body
	}

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * 2017年2月22日 下午4:06:42
	 * 
	 */
	public static void main(String[] args) {

	}

}
