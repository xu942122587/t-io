package org.tio.core.utils.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.ObjWithLock;

/**
 * @author tanyaowu 
 * 2017年5月10日 下午1:14:15
 */
public abstract class PageUtils {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(PageUtils.class);

	private static <T> Page<T> pre(java.util.Collection<T> list, int pageIndex, int pageSize) {
		if (list == null) {
			return new Page<T>(null, pageIndex, pageSize, 0);
		}

		pageSize = processPageSize(pageSize);
		pageIndex = processPageIndex(pageIndex);

		int recordCount = list.size();

		List<T> pageData = new ArrayList<T>(pageSize);
		Page<T> ret = new Page<T>(pageData, pageIndex, pageSize, recordCount);
		return ret;
	}

	public static <T> Page<T> fromList(List<T> list, int pageIndex, int pageSize) {
		Page<T> page = pre(list, pageIndex, pageSize);

		List<T> pageData = page.getPageData();
		if (pageData == null) {
			return page;
		}

		int startIndex = Math.min((page.getPageIndex() - 1) * page.getPageSize(), list.size());
		int endIndex = Math.min((page.getPageIndex()) * page.getPageSize(), list.size());

		for (int i = startIndex; i < endIndex; i++) {
			pageData.add(list.get(i));
		}
		page.setPageData(pageData);
		return page;
	}

	public static <T> Page<T> fromSet(Set<T> set, int pageIndex, int pageSize) {
		Page<T> page = pre(set, pageIndex, pageSize);

		List<T> pageData = page.getPageData();
		if (pageData == null) {
			return page;
		}

		int startIndex = Math.min((page.getPageIndex() - 1) * page.getPageSize(), set.size());
		int endIndex = Math.min((page.getPageIndex()) * page.getPageSize(), set.size());

		int i = 0;
		for (T t : set) {
			if (i >= endIndex) {
				break;
			}
			if (i < startIndex) {
				i++;
				continue;
			}

			pageData.add(t);
			i++;
			continue;

		}
		page.setPageData(pageData);
		return page;
	}

	public static <T> Page<T> fromSetWithLock(ObjWithLock<Set<T>> setWithLock, int pageIndex, int pageSize) {

		Lock lock = setWithLock.getLock().readLock();
		try {
			lock.lock();
			Set<T> set = setWithLock.getObj();
			return fromSet(set, pageIndex, pageSize);
		} finally {
			lock.unlock();
		}

	}

	private static int processPageIndex(int pageIndex) {
		return pageIndex <= 0 ? 1 : pageIndex;
	}

	private static int processPageSize(int pageSize) {
		return pageSize <= 0 ? Integer.MAX_VALUE : pageSize;
	}
}
