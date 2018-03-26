package cn.ac.iie.remover.send;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.SendFileEntity;
import cn.ac.iie.remover.tools.BashUtils;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送源文件到HDFS服务器待处理
 * 
 * @author zzq12
 *
 */
public class ToHDFSHandler implements EventHandler<SendFileEntity> {

	private static Logger log = LoggerFactory.getLogger(ToHDFSHandler.class);
	private static List<String> mkdirold;
	private static int size = 0;

	public ToHDFSHandler() {
		mkdirold = new ArrayList<String>();
	}

	@Override
	public void onEvent(SendFileEntity file, long sequence, boolean endOfBatch) {
		try {
			if (file.isCompress()) {
				String mkdir = Config.mkdirScript + " " + Config.receiveDataPath + File.separator + file.get_parentDir()
						+ File.separator;
				if (!mkdirold.contains(mkdir)) {
					int i = 0;
					while (!BashUtils.runRemoteCmd(mkdir)) {
						if (i++ > 3) {
							break;
						}
					}
					if (mkdirold.size() < 10) {
						mkdirold.add(mkdir);
					} else {
						mkdirold.set(size++ % 10, mkdir);
						size = size >= 10 ? 0 : size;
					}
				}

				String scp = Config.sendScript + " " + Config.sendDataPath + File.separator + file.getParentDir()
						+ File.separator + file.getZipName() + " " + Config.receiveDataPath + File.separator
						+ file.get_parentDir() + File.separator + file.getZipName();
				boolean s = BashUtils.runRemoteCmd(scp);
				if (s) {
					file.setRsync(true);
				}
			} else {
				log.error("file is not compress file:{};", file);
			}
			log.info("file is rsync file:{};", file);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
