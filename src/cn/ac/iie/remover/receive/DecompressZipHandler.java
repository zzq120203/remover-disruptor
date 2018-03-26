package cn.ac.iie.remover.receive;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.ReceiveFileEntity;
import cn.ac.iie.remover.tools.ZipUtils;
import com.lmax.disruptor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 解压文件
 * @author zzq12
 *
 */
public class DecompressZipHandler implements WorkHandler<ReceiveFileEntity> {

	private static Logger log = LoggerFactory.getLogger(DecompressZipHandler.class);
	
	private ZipUtils zipUtils;
	
	public DecompressZipHandler() {
		zipUtils = new ZipUtils(true);
	}

	@Override
	public void onEvent(ReceiveFileEntity file) {
		try {
			if (file.getName() == null) {
				return;
			}
			if (Config.toHadoop) {
				if (file.isToLoong()) {
					String md5 = zipUtils.deCompress(Config.receiveDataPath + File.separator + file.getParentDir() + File.separator + file.getZipName()
								, Config.receiveDataPath + File.separator + file.getParentDir() + File.separator 
								, Config.receiveDataPath + File.separator + file.getParentDir() + File.separator + file.getName());
					if (!md5.equals(file.getMd5())) {
						log.error("src File({}) is ok.md5({}) != file.md5({})", file, file.getMd5(), md5);
						//return;
					}
					file.setDeCompress(true);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}


}
