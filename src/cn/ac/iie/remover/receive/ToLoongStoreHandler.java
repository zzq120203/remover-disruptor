package cn.ac.iie.remover.receive;

import cn.ac.iie.remover.config.Config;
import cn.ac.iie.remover.entity.ReceiveFileEntity;
import cn.ac.iie.remover.tools.BashUtils;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储到龙存
 * @author zzq12
 *
 */
public class ToLoongStoreHandler implements EventHandler<ReceiveFileEntity> {

	private static Logger log = LoggerFactory.getLogger(ToLoongStoreHandler.class);
	private static List<String> mkdirold;
	private static int size = 0;

	public ToLoongStoreHandler() {
		mkdirold = new ArrayList<String>();
	}

	@Override
	public void onEvent(ReceiveFileEntity file, long sequence, boolean endOfBatch) {
		try {
			if (file.getName() == null) {
				return;
			}
			if (ReceiveDataGet.isReceive) {
				String mkdir = "mkdir " + Config.loongStorePath + File.separator + file.getParentDir() + File.separator;
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
				
				String cp = "cp " + Config.receiveDataPath + File.separator + file.getParentDir() + File.separator + file.getZipName()
						+ " " + Config.loongStorePath + File.separator + file.getParentDir() + File.separator + file.getZipName();
				boolean s = BashUtils.runRemoteCmd(cp);
				if (s) {
					file.setToLoong(true);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}


}
