package org.tio.monitor;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.utils.SystemTimer;

import com.google.common.util.concurrent.RateLimiter;

/**
 * @author tanyaowu 
 * 2017年5月23日 下午1:09:55
 */
public class RateLimiterWrap {
		private static Logger log = LoggerFactory.getLogger(RateLimiterWrap.class);

	/**
	 * 频率控制
	 */
	private RateLimiter rateLimiter = null;//RateLimiter.create(3);

	/**
	 * 本阶段已经收到多少次警告
	 */
	private AtomicInteger warnCount = new AtomicInteger();
	
	/**
	 * 总共已经收到多少次警告
	 */
	private AtomicInteger allWarnCount = new AtomicInteger();

	/**
	 * 本阶段最多警告多次数
	 */
	private int maxWarnCount = 20;
	
	/**
	 * 一共最多警告多次数
	 */
	private int maxAllWarnCount = maxWarnCount * 10;

	/**
	 * 上一次警告时间
	 */
	private long lastWarnTime = SystemTimer.currentTimeMillis();

	/**
	 * 警告清零时间间隔，即如果有这么长时间没有收到警告，则把前面的警告次数清零
	 */
	private int warnClearInterval = 1000 * 60 * 60 * 2;

	/**
	 * 
	 * @param permitsPerSecond QPS
	 * @param warnClearInterval 清理本阶段警告的时间间隔，参考值1000 * 60 * 60 * 2，单位为ms
	 * @param maxWarnCount 本阶段最多警告多次数，参考值10
	 * @param maxAllWarnCount 一共最多警告多次数
	 * @author: tanyaowu
	 */
	public RateLimiterWrap(int permitsPerSecond, int warnClearInterval, int maxWarnCount, int maxAllWarnCount) {
		this.rateLimiter = RateLimiter.create(permitsPerSecond);
		this.warnClearInterval = warnClearInterval;
		this.maxWarnCount = maxWarnCount;
		this.maxAllWarnCount = maxAllWarnCount;
	}

	/**
	 * 
	 * @return 
	 * 0位置：根据QPS获取执行锁, false: 没拿到锁<br>
	 * 1位置：根据警告次数获取执行锁, false: 没拿到锁<br>
	 * @author: tanyaowu
	 */
	public boolean[] tryAcquire() {
		boolean ret = rateLimiter.tryAcquire();
		if (!ret) {
			synchronized (this) {
				long nowTime = SystemTimer.currentTimeMillis();
				if ((nowTime - lastWarnTime) > warnClearInterval) {
					warnCount.set(0);
				}
				lastWarnTime = SystemTimer.currentTimeMillis();
				int wc = warnCount.incrementAndGet();
				int awc = allWarnCount.incrementAndGet();

				if (wc > maxWarnCount || awc > maxAllWarnCount) {
					return new boolean[]{false, false};
				}
				return new boolean[]{false, true};
			}
		} else {
			return new boolean[]{true, true};
		}

	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the rateLimiter
	 */
	public RateLimiter getRateLimiter() {
		return rateLimiter;
	}

	/**
	 * @param rateLimiter the rateLimiter to set
	 */
	public void setRateLimiter(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	/**
	 * @return the warnCount
	 */
	public AtomicInteger getWarnCount() {
		return warnCount;
	}

	/**
	 * @param warnCount the warnCount to set
	 */
	public void setWarnCount(AtomicInteger warnCount) {
		this.warnCount = warnCount;
	}

	/**
	 * @return the lastWarnTime
	 */
	public long getLastWarnTime() {
		return lastWarnTime;
	}

	/**
	 * @param lastWarnTime the lastWarnTime to set
	 */
	public void setLastWarnTime(long lastWarnTime) {
		this.lastWarnTime = lastWarnTime;
	}

	/**
	 * @return the warnClearInterval
	 */
	public int getWarnClearInterval() {
		return warnClearInterval;
	}

	/**
	 * @param warnClearInterval the warnClearInterval to set
	 */
	public void setWarnClearInterval(int warnClearInterval) {
		this.warnClearInterval = warnClearInterval;
	}

	/**
	 * @return the maxWarnCount
	 */
	public int getMaxWarnCount() {
		return maxWarnCount;
	}

	/**
	 * @param maxWarnCount the maxWarnCount to set
	 */
	public void setMaxWarnCount(int maxWarnCount) {
		this.maxWarnCount = maxWarnCount;
	}

	/**
	 * @return the allWarnCount
	 */
	public AtomicInteger getAllWarnCount() {
		return allWarnCount;
	}

	/**
	 * @param allWarnCount the allWarnCount to set
	 */
	public void setAllWarnCount(AtomicInteger allWarnCount) {
		this.allWarnCount = allWarnCount;
	}

	/**
	 * @return the maxAllWarnCount
	 */
	public int getMaxAllWarnCount() {
		return maxAllWarnCount;
	}

	/**
	 * @param maxAllWarnCount the maxAllWarnCount to set
	 */
	public void setMaxAllWarnCount(int maxAllWarnCount) {
		this.maxAllWarnCount = maxAllWarnCount;
	}
}
