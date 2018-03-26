package cn.ac.iie.remover.send;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.SendFileEntity;
import cn.ac.iie.remover.tools.BashUtils;
import cn.ac.iie.remover.tools.FileUtils;
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
public class DeleteSendDataHandler implements EventHandler<SendFileEntity> {

	private static Logger log = LoggerFactory.getLogger(DeleteSendDataHandler.class);

	public DeleteSendDataHandler() {
	}

	@Override
	public void onEvent(SendFileEntity file, long sequence, boolean endOfBatch) {
		try {
			if (file.getName() != null) {
				if (file.isRsync()) {
					
					FileUtils.deleteFile(Config.sendDataPath + File.separator + file.getParentDir() + File.separator + file.getZipName());
					FileUtils.moveFile(
							Config.sendDataPath + File.separator + file.getParentDir() + File.separator + file.getName(),
							Config.sendDataPath + File.separator + file.getParentDir() + ".bak" + File.separator);
	
					String cmd = Config.touchOK + " " + file.getMd5() + " " + Config.receiveDataPath + File.separator + file.get_parentDir()
							+ File.separator + file.getZipName() + Config.fileMark;
					boolean touch = BashUtils.runRemoteCmd(cmd);
					if (!touch) {
						log.error("touch file:{}.ok is defeated ", file);
					}
					log.info("Delete File:{}", file);
				} else {
					boolean make = FileUtils.makeFile(Config.sendDataPath + File.separator + file.getParentDir()
							+ File.separator + file.getMarkName(), (file.getLength() + "").getBytes());
					FileUtils.deleteFile(Config.sendDataPath + File.separator + file.getParentDir() + File.separator
							+ file.getZipName());
					if (!make) {
						log.error("makeFile:{} is defeated", file);
					}
					log.info("Make File:{};", file);
				}
			} else {
				String cmd = Config.touchOK + " " + file.getMd5() + " " + Config.receiveDataPath + File.separator + file.getParentDir() + ".ok";
				boolean touch = BashUtils.runRemoteCmd(cmd);
				if (!touch) {
					log.error("touch file:{}.ok is defeated ", file);
				} else {
					log.info("touch file:{}.ok ", file);
				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}