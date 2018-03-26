package cn.ac.iie.remover.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileUtils {

	public static boolean deleteFile(String fileName) throws Exception {
		return deleteFile(new File(fileName));
	}

	public static boolean deleteFile(File file) throws Exception {
		if (file != null && file.exists()) {
			return file.delete();
		} else {
			throw new FileNotFoundException(file.getPath());
		}
	}

	public static boolean moveFile(String fileName, String dir) throws Exception {
		File dirFile = new File(dir);
		return moveFile(new File(fileName), dirFile);
	}

	public static boolean moveFile(File file, File dirFile) throws Exception {
		if (file != null && file.exists()) {
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			return file.renameTo(new File(dirFile.getPath() + File.separator + file.getName()));
		} else {
			throw new FileNotFoundException(file.getPath());
		}
	}

	public static boolean makeFile(String pathname) throws Exception {
		File file = new File(pathname);
		return file.createNewFile();
	}
	
	public static boolean makeFile(String pathname, byte[] content) throws Exception {
		FileOutputStream out = null;
		boolean re = false;
		try {
			File file = new File(pathname);
			file.createNewFile();
			out = new FileOutputStream(file);
			out.write(content);
			re = true;
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return re;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(moveFile("C:\\Users\\zzq12\\Desktop\\abc\\abctest1","C:\\Users\\zzq12\\Desktop\\abcvvv"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
