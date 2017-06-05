package org.tio.examples.im.server;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.DbSearcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.examples.im.common.CommandStat;
import org.tio.examples.im.common.ImPacket;
import org.tio.examples.im.common.ImSessionContext;
import org.tio.examples.im.common.utils.ImUtils;
import org.tio.monitor.RateLimiterWrap;
import org.tio.server.intf.ServerAioListener;

/**
 * 
 * @author tanyaowu 
 *
 */
public class ImServerAioListener implements ServerAioListener<ImSessionContext, ImPacket, Object> {

	private static Logger log = LoggerFactory.getLogger(ImServerAioListener.class);
	private static Logger iplog = LoggerFactory.getLogger("tio-ip-trace-log");

	static Map<String, AtomicLong> ipmap = new java.util.concurrent.ConcurrentHashMap<>();
	static AtomicLong accessCount = new AtomicLong();

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * 2016年12月16日 下午5:52:06
	 * 
	 */
	public ImServerAioListener() {
	}

	/**
	 * @param args
	 *
	 * @author: tanyaowu
	 * 2016年12月16日 下午5:52:06
	 * 
	 */
	public static void main(String[] args) {
	}

	/** 
	 * @see org.tio.server.intf.ServerAioListener#onAfterAccepted(java.nio.channels.AsynchronousSocketChannel, org.tio.server.AioServer)
	 * 
	 * @param asynchronousSocketChannel
	 * @param aioServer
	 * @return
	 * @author: tanyaowu
	 * 2016年12月20日 上午11:03:45
	 * 
	 */
	//	@Override
	//	public boolean onAfterAccepted(AsynchronousSocketChannel asynchronousSocketChannel, AioServer<ImSessionContext, ImPacket, Object> aioServer)
	//	{
	//		return true;
	//	}

	@Override
	public void onAfterConnected(ChannelContext<ImSessionContext, ImPacket, Object> channelContext, boolean isConnected, boolean isReconnect) {
		ImSessionContext imSessionContext = new ImSessionContext();
		channelContext.setSessionContext(imSessionContext);

		GroupContext<ImSessionContext, ImPacket, Object> groupContext = channelContext.getGroupContext();

		int permitsPerSecond = ImServerStarter.conf.getInt("request.permitsPerSecond");
		int warnClearInterval = 1000 * ImServerStarter.conf.getInt("request.warnClearInterval");
		int maxWarnCount = ImServerStarter.conf.getInt("request.maxWarnCount");
		int maxAllWarnCount = ImServerStarter.conf.getInt("request.maxAllWarnCount");
		RateLimiterWrap rateLimiterWrap = new RateLimiterWrap(permitsPerSecond, warnClearInterval, maxWarnCount, maxAllWarnCount);

		imSessionContext.setRequestRateLimiter(rateLimiterWrap);

		if (isConnected) {
			String ip = channelContext.getClientNode().getIp();

			DbSearcher dbSearcher = null;
			DataBlock dataBlock = null;
			try {
				dbSearcher = DbSearcherFactory.getDbSearcher(ImServerStarter.conf.getString("ip.db.path"));
				if (dbSearcher == null) {
					log.error("请检查一下文件是否存在:{}", ImServerStarter.conf.getString("ip.db.path"));
				}
				dataBlock = dbSearcher.memorySearch(ip);

				dataBlock.setRegion(ImUtils.formatRegion(dataBlock.getRegion()));

			} catch (Exception e) {
				log.error(e.toString(), e);
			} finally {
				if (dataBlock == null) {
					dataBlock = new DataBlock(0, "未知", 0);
				}
//				if (StringUtils.contains(dataBlock.getRegion(), "武汉")) {
//					Aio.IpBlacklist.add(groupContext, ip);
//					LogUtils.getIpBlacklistLog().info(StringUtils.leftPad(ip, 15) + "【" + dataBlock.getRegion() + "】");
//					log.error("ip{}来自武汉，暂时列入黑名单");
//					return;
//				}
				imSessionContext.setDataBlock(dataBlock);
			}

			ImUtils.setClient(channelContext);

			AtomicLong ipcount = ipmap.get(ip);
			if (ipcount == null) {
				ipcount = new AtomicLong();
				ipmap.put(ip, ipcount);
			}
			ipcount.incrementAndGet();

			String region = StringUtils.leftPad(dataBlock.getRegion(), 12);
			String accessCountStr = StringUtils.leftPad(accessCount.incrementAndGet() + "", 9);
			String ipCountStr = StringUtils.leftPad(ipmap.size() + "", 9);
			String ipStr = StringUtils.leftPad(ip, 15);
			//地区，所有的访问次数，有多少个不同的ip， ip， 这个ip连接的次数
			iplog.info("{} {} {} {} {}", region, accessCountStr, ipCountStr, ipStr, ipcount);
		}

		return;
	}

	/** 
	 * @see org.tio.core.intf.AioListener#onBeforeSent(org.tio.core.ChannelContext, org.tio.core.intf.Packet, int)
	 * 
	 * @param channelContext
	 * @param packet
	 * @author: tanyaowu
	 * 2016年12月20日 上午11:08:44
	 * 
	 */
	@Override
	public void onAfterSent(ChannelContext<ImSessionContext, ImPacket, Object> channelContext, ImPacket packet, boolean isSentSuccess) {
		if (isSentSuccess) {
			CommandStat.getCount(packet.getCommand()).sent.incrementAndGet();
		}

	}

	/** 
	 * @see org.tio.core.intf.AioListener#onAfterReceived(org.tio.core.ChannelContext, org.tio.core.intf.Packet, int)
	 * 
	 * @param channelContext
	 * @param packet
	 * @param packetSize
	 * @author: tanyaowu
	 * 2016年12月20日 上午11:08:44
	 * 
	 */
	@Override
	public void onAfterReceived(ChannelContext<ImSessionContext, ImPacket, Object> channelContext, ImPacket packet, int packetSize) {
		CommandStat.getCount(packet.getCommand()).received.incrementAndGet();
	}

	/** 
	 * @see org.tio.core.intf.AioListener#onAfterClose(org.tio.core.ChannelContext, java.lang.Throwable, java.lang.String)
	 * 
	 * @param channelContext
	 * @param throwable
	 * @param remark
	 * @author: tanyaowu
	 * 2017年2月1日 上午11:03:11
	 * 
	 */
	@Override
	public void onAfterClose(ChannelContext<ImSessionContext, ImPacket, Object> channelContext, Throwable throwable, String remark, boolean isRemove) {
	}

}
