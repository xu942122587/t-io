package org.tio.examples.im.service;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.examples.im.server.ImServerStarter;

import com.xiaoleilu.hutool.dfa.WordTree;
import com.xiaoleilu.hutool.io.FileUtil;

/**
 * @author tanyaowu 
 * 2017年5月29日 下午2:50:41
 */
public class BadWordService {
	private static Logger log = LoggerFactory.getLogger(BadWordService.class);

	public static final WordTree wordTree = new WordTree();

	/**
	 * 
	 * @author: tanyaowu
	 */
	public BadWordService() {
	}

	public static void initBadWord() {
		String rootDirStr = FileUtil.getAbsolutePath("classpath:dict/");
		File rootDir = new File(rootDirStr);
		File[] files = rootDir.listFiles();
		int count = 0;
		if (files != null) {
			
			for (File file : files) {
				List<String> lines = FileUtil.readLines(file, "utf-8");
				for (String line : lines) {
					wordTree.addWord(line);
					count++;
					//log.error(line);
				}
			}
		}
		log.error("一共{}个敏感词", count);
	}
	
	/**
	 * 如果没匹配到就返回null，否则返回替换后的string
	 * @param initText
	 * @param replaceText
	 * @param logstr
	 * @return
	 * @author: tanyaowu
	 */
	public static String replaceBadWord(String initText, String replaceText, Object logstr) {
		List<String> list = wordTree.matchAll(initText);
		if (list != null && list.size() > 0) {
			String ret = initText;
			for (String word : list) {
				ret = StringUtils.replaceAll(ret, word, replaceText);
			}
			if (logstr != null) {
				log.error("{}, 找到敏感词，原文:【{}】，替换后的:【{}】", logstr, initText, ret);
			} else {
				log.error("找到敏感词，原文:【{}】，替换后的:【{}】", initText, ret);
			}
			return ret;
		}
		return null;
	}
	
	public static String replaceBadWord(String initText, String replaceText) {
		return replaceBadWord(initText, replaceText, null);
	}
	
	
	public static String replaceWithDftReplace(String initText) {
		return replaceBadWord(initText, ImServerStarter.conf.getString("dft.badword.replaceText"));
	}
	
	public static String replaceWithDftReplace(String initText, String logstr) {
		return replaceBadWord(initText, ImServerStarter.conf.getString("dft.badword.replaceText"), logstr);
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {
		initBadWord();
		replaceWithDftReplace("习近平dddd|sss");
	}
}
