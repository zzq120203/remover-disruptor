package cn.ac.iie.remover.receive;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.ReceiveFileEntity;
import cn.ac.iie.remover.tools.HdfsUtils;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件put到hdfs
 * 
 * @author zzq12
 *
 */
public class PutHDFSHandler implements EventHandler<ReceiveFileEntity>, LifecycleAware {

	private static Logger log = LoggerFactory.getLogger(PutHDFSHandler.class);
	private static List<String> mkdirold;
	private static int size = 0;
	
	private static HdfsUtils hdfs;

	public PutHDFSHandler() {
		mkdirold = new ArrayList<String>();
		hdfs = new HdfsUtils();
	}

	@Override
	public void onEvent(ReceiveFileEntity file, long sequence, boolean endOfBatch) {
		try {
			if (file.getName() == null) {
				return;
			}
			if (file.isDeCompress()) {
				boolean mkdir = false;
				int i = 0;
				do {
					mkdir = hdfs.mkdir(Config.hadoopPath + File.separator + file.getParentDir() + File.separator);
					if (i++ >= 2) {
						break;
					}
				} while (!mkdir);
				if (mkdir) {
					boolean put = hdfs.uploadLocalFile2HDFS(Config.receiveDataPath + File.separator + file.getParentDir() + File.separator + file.getName(),
							Config.hadoopPath + File.separator+ file.getParentDir() + File.separator);
					if (put) {
						file.setPutHDFS(true);
					}
					log.info("uploadHDFS File:{};", file);
				} else {
					log.error("HdfsUtils.mkdir({}) is defeated", Config.hadoopPath + File.separator + file.getParentDir() + File.separator);
				}
			}
//			if (file.isDeCompress()) {
//				// hadoop fs -mkdir -p < hdfs path>
//				String mkdir = "hadoop fs -mkdir -p " + Config.hadoopPath + File.separator + file.getParentDir() + File.separator;
//				if (!mkdirold.contains(mkdir)) {
//					int i = 0;
//					while (!BashUtils.runRemoteCmd(mkdir)) {
//						if (i++ > 3) {
//							break;
//						}
//					}
//					if (mkdirold.size() < 10) {
//						mkdirold.add(mkdir);
//					} else {
//						mkdirold.set(size++ % 10, mkdir);
//						size = size >= 10 ? 0 : size;
//					}
//				}
//				// hadoop fs -put < local file > < hdfs file >
//				String put = "hadoop fs -put " + Config.receiveDataPath + File.separator + file.getParentDir() + File.separator + file.getName()
//						+ " " + Config.hadoopPath + File.separator+ file.getParentDir() + File.separator;//file.getName();
//				boolean s = BashUtils.runRemoteCmd(put);
//				if (s) {
//					file.setPutHDFS(true);
//				}
//			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onShutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub

	}

}
