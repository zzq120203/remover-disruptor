package cn.ac.iie.remover.send;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.SendFileEntity;
import cn.ac.iie.remover.tools.FileUtils;
import cn.ac.iie.remover.tools.MyFilter;

/**
 * 获取待发送文件
 * 
 * @author zzq12
 *
 */
public class SendDataGet implements Runnable {
	private static Logger log = LoggerFactory.getLogger(SendDataGet.class);
	public static boolean isSend = true;
	private RingBuffer<SendFileEntity> ringBuffer;

	private static MyFilter fileFilter;
	private static DateTimeFormatter dtf;
	
	private static List<String> oldDir;
	private static int size = 0;

	public SendDataGet(RingBuffer<SendFileEntity> ringBuffer) {
		log.info("SendDataGetThread start");
		this.ringBuffer = ringBuffer;
		fileFilter = new MyFilter(Config.fileMark);
		dtf = DateTimeFormatter.ofPattern("yyyyMMddHH");
		oldDir = new ArrayList<String>();
	}

	@Override
	public void run() {
		long seq = 0;
		SendFileEntity sf = null;
		try {
			while (isSend) {
				LocalDateTime date = LocalDateTime.now();
				for (int i = Config.minHour; i >= 0; i--) {
					String parentDir = File.separator + dtf.format(date.minusHours(i));
					File dataDir = new File(Config.sendDataPath + parentDir);
					if (dataDir.exists()) {
						String[] fileNames = dataDir.list(fileFilter);
						if (fileNames != null && fileNames.length > 0) {
//							if (fileNames.length > 1) {
//								Arrays.sort(fileNames, (x, y) -> Integer.parseInt(x.split(parentDir + "_|.ok")[1])
//										- Integer.parseInt(y.split(parentDir + "_|.ok")[1]));
//							}
							File markFile = null;
							FileInputStream in = null;
							for (String markName : fileNames) {
								String fileName = markName.substring(0, markName.length() - Config.fileMark.length());
								try {
									seq = ringBuffer.next();
									sf = ringBuffer.get(seq);
									sf.init();
									sf.setName(fileName);
									sf.setMarkName(markName);
									sf.setZipName(fileName + Config.zipMark);
									sf.setParentDir(parentDir);
									if (oldDir.contains(parentDir)) {
										sf.set_parentDir(File.separator + dtf.format(date));
									} else {
										sf.set_parentDir(parentDir);
									}
									markFile = new File(Config.sendDataPath + File.separator + parentDir
											+ File.separator + markName);
									in = new FileInputStream(markFile);
									if (markFile.length() == 0) {
										Thread.sleep(1000 * 2);
									}
									byte[] content = new byte[(int) markFile.length()];
									in.read(content);
									if (content != null && content.length > 0) {
										sf.setLength(Long.parseLong(new String(content)));
									}
								} finally {
									ringBuffer.publish(seq);
									if (in != null) {
										in.close();
									}
								}
								FileUtils.deleteFile(markFile);
								log.info("src data file:{};", sf);
							}
						} else {
							if (i > 0 && !oldDir.contains(parentDir)) {
								try {
									seq = ringBuffer.next();
									sf = ringBuffer.get(seq);
									sf.init();
									sf.setParentDir(parentDir);
								} finally {
									ringBuffer.publish(seq);
								}
								if (oldDir.size() < 10) {
									oldDir.add(parentDir);
								} else {
									oldDir.set(size++ % 10, parentDir);
									size = size >= 10 ? 0 : size;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("src data file:{};", sf);
			log.error(e.getMessage(), e);
		}
	}

}
