package org.tio.core.maintain;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.SetWithLock;
import org.tio.core.intf.Packet;

/**
 * 
 * @author tanyaowu 
 * 2017年5月22日 下午2:53:47
 */
public class IpBlacklist {

	/** remoteAndChannelContext key: "ip:port" value: ChannelContext. */
	private SetWithLock<String> setWithLock = new SetWithLock<String>(new HashSet<String>());

	public <SessionContext, P extends Packet, R> void add(GroupContext<SessionContext, P, R> groupContext, String ip) {
		//先添加到黑名单列表
		Lock lock = setWithLock.getLock().writeLock();
		try {
			lock.lock();
			Set<String> m = setWithLock.getObj();
			m.add(ip);
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
		
		//再删除相关连接
		SetWithLock<ChannelContext<SessionContext, P, R>> setWithLock = Aio.getAllChannelContexts(groupContext);
		Lock lock2 = setWithLock.getLock().readLock();
		try {
			lock2.lock();
			Set<ChannelContext<SessionContext, P, R>> set = setWithLock.getObj();
			for (ChannelContext<SessionContext, P, R> channelContext : set) {
				String clientIp = channelContext.getClientNode().getIp();
				if (StringUtils.equals(clientIp, ip)) {
					Aio.remove(channelContext, "ip[" + clientIp + "]被加入了黑名单");
				}
			}
		} finally {
			lock2.unlock();
		}
	}

	public boolean remove(String ip) {
		Lock lock = setWithLock.getLock().writeLock();
		try {
			lock.lock();
			Set<String> m = setWithLock.getObj();
			return m.remove(ip);
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}

	public Set<String> getCopy() {
		Lock lock = setWithLock.getLock().readLock();
		try {
			lock.lock();
			Set<String> m = setWithLock.getObj();
			Set<String> copyObj = new HashSet<>();
			copyObj.addAll(m);
			return copyObj;
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}

	public int size() {
		Lock lock = setWithLock.getLock().readLock();
		try {
			lock.lock();
			Set<String> m = setWithLock.getObj();
			return m.size();
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 是否在黑名单中
	 * @param ip
	 * @return
	 * @author: tanyaowu
	 */
	public boolean isInBlacklist(String ip) {
		Lock lock = setWithLock.getLock().readLock();
		try {
			lock.lock();
			Set<String> m = setWithLock.getObj();
			return m.contains(ip);
		} catch (Exception e) {
			throw e;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 
	 *
	 * @return
	 */
	public SetWithLock<String> getSetWithLock() {
		return setWithLock;
	}

}
