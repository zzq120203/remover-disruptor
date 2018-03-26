package cn.ac.iie.remover.receive;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.RingBuffer;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.ReceiveFileEntity;
import cn.ac.iie.remover.tools.FileUtils;
import cn.ac.iie.remover.tools.MyFilter;

/**
 * 获取目的文件
 * @author zzq12
 *
 */
public class ReceiveDataGet implements Runnable {
	private static Logger log = LoggerFactory.getLogger(ReceiveDataGet.class);
	
	public static boolean isReceive = true;
	private RingBuffer<ReceiveFileEntity> ringBuffer;
	
	private static MyFilter fileFilter;
	private static DateTimeFormatter dtf;
	
	public ReceiveDataGet(RingBuffer<ReceiveFileEntity> ringBuffer) {
		this.ringBuffer = ringBuffer;
		fileFilter = new MyFilter(Config.fileMark);
		dtf = DateTimeFormatter.ofPattern("yyyyMMddHH");
	}
	
	@Override
	public void run() {
		long seq = 0;
		ReceiveFileEntity rf = null;
		try {
			while (isReceive) {
				LocalDateTime date = LocalDateTime.now();
				for (int i = Config.minHour; i >= 0; i--) {
					String parentDir = File.separator + dtf.format(date.minusHours(i));
					File dataDir = new File(Config.receiveDataPath + parentDir);
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
								String zipName = markName.substring(0, markName.length() - Config.fileMark.length());
								try {
									seq = ringBuffer.next();
									rf = ringBuffer.get(seq);
									rf.init();
									rf.setName(zipName.substring(0, zipName.length() - Config.zipMark.length()));
									rf.setMarkName(markName);
									rf.setZipName(zipName);
									rf.setParentDir(parentDir);
									
									markFile = new File(Config.receiveDataPath + File.separator + parentDir + File.separator + markName);
									in = new FileInputStream(markFile);
									if (markFile.length() == 0) {
										Thread.sleep(1000 * 2);
									}
									byte[] content = new byte[(int)markFile.length()];
									in.read(content);
									rf.setMd5(new String(content).trim());
								} finally {
									ringBuffer.publish(seq);
									if (in != null) {
										in.close();
									}
								}
								FileUtils.deleteFile(Config.receiveDataPath + File.separator + parentDir + File.separator + markName);
								log.info("src data file:{};", rf);
							}
						} else {
							if (i > 0) {
								File file = new File(dataDir+".ok");
								if (file.exists()) {
									try {
										seq = ringBuffer.next();
										rf = ringBuffer.get(seq);
										rf.init();
										rf.setParentDir(parentDir);
									} finally {
										ringBuffer.publish(seq);
									}
									FileUtils.deleteFile(dataDir+".ok");
									log.info("src data file:{};", rf);
								}
							}
						}
					}
				}
				Thread.sleep(1000 * 1);
			}
		} catch (Exception e) {
			log.error("src data file:{};", rf);
			log.error(e.getMessage(), e);
		}
	}

}
