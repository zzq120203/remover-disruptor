package cn.ac.iie.remover.send;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.SendFileEntity;
import cn.ac.iie.remover.tools.ZipUtils;
import com.lmax.disruptor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 压缩源文件
 * @author zzq12
 *
 */
public class CompressZipHandler implements WorkHandler<SendFileEntity> {

	private static Logger log = LoggerFactory.getLogger(CompressZipHandler.class);
	
	private ZipUtils zipUtils;

	public CompressZipHandler() {
		zipUtils = new ZipUtils(true);
	}

	@Override
	public void onEvent(SendFileEntity file) {
		try {
			if (SendDataGet.isSend && file.getName() != null) {
				String srcPath = Config.sendDataPath + File.separator + file.getParentDir() + File.separator + file.getName();
				String zipPath = Config.sendDataPath + File.separator + file.getParentDir() + File.separator + file.getZipName();
				File srcFile = new File(srcPath);
				if (srcFile.exists()) {
					if (file.getLength() != srcFile.length()) {
						log.error("src File({}) is ok.length({}) != srcFile.length({})", file, file.getLength(), srcFile.length());
						//return;
					}
					String md5 = zipUtils.compress(zipPath, srcPath);
					file.setMd5(md5);
					file.setCompress(true);
				} else {
					log.error("File({}) is not exists", file);
				}
				log.info("srcFile:{} compress succeed", file);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
