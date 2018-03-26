package cn.ac.iie.remover.receive;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.ReceiveFileEntity;
import cn.ac.iie.remover.tools.FileUtils;
import cn.ac.iie.remover.tools.HdfsUtils;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 删除源文件
 * 
 * @author zzq12
 *
 */
public class DeleteReceiveDataHandler implements EventHandler<ReceiveFileEntity> {

	private static Logger log = LoggerFactory.getLogger(DeleteReceiveDataHandler.class);
	private static HdfsUtils hdfs;

	public DeleteReceiveDataHandler() {
		hdfs = new HdfsUtils();
	}

	@Override
	public void onEvent(ReceiveFileEntity file, long sequence, boolean endOfBatch) {
		try {
			if (file.getName() != null) {
				if (file.isPutHDFS() || (!Config.toHadoop && file.isToLoong())) {
					FileUtils.deleteFile(Config.receiveDataPath + File.separator + file.getParentDir() + File.separator
							+ file.getZipName());
					if (Config.toHadoop)
					FileUtils.deleteFile(Config.receiveDataPath + File.separator + file.getParentDir() + File.separator
							+ file.getName());
					log.info("Delete File:{}", file);
				} else {
					boolean make = FileUtils.makeFile(Config.receiveDataPath + File.separator + file.getParentDir()
							+ File.separator + file.getMarkName(), file.getMd5().getBytes());
					if (!make) {
						log.error("makeFile:{} is defeated", file);
					}
					log.info("Make File:{};", file);
				}
			} else {
				String okName = file.getParentDir();
				while (okName.startsWith(File.separator))
					okName = okName.substring(1);
				String path = Config.hadoopPath + File.separator + file.getParentDir() + File.separator 
						+ Config.hostID + "_" + okName + ".ok";
				hdfs.createFile(path);
				log.info("File:{}; File.ok:{}", file, path);
//				String mkdirok = "hadoop fs -touchz " + Config.hadoopPath + File.separator + file.getParentDir() + File.separator
//						+ file.getParentDir() + ".ok";
//				boolean touch = BashUtils.runRemoteCmd(mkdirok);
//				if (!touch) {
//					log.error("touch file:{}.ok is defeated ", file);
//				} else {
//					log.info("touch file:{}.ok ", file);
//				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}