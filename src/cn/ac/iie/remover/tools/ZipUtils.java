package cn.ac.iie.remover.tools;

import org.apache.commons.codec.binary.Hex;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipUtils {
	private static Logger log = LoggerFactory.getLogger(ZipUtils.class);
	
	private static final int BUFFER = 8192;

	private boolean getMD5;
	
	public ZipUtils(boolean getMD5) {
		this.getMD5 = getMD5;
	}

	public String compress(String zipPath, String srcPath) {
		// 创建需要压缩的文件对象
		File file = new File(srcPath);
		if (!file.exists()) {
			throw new RuntimeException(srcPath + "不存在！");
		}
		return compress(zipPath, file);
	}
	
	/**
	 * 压缩单个或多文件方法
	 * 
	 * @param zipPath
	 *            压缩后的文件路径
	 * @param file
	 *            要压缩的文件路径 参数srcPathName也可以定义成数组形式，需调用方把参数封装到数组中传过来即可
	 * @return 
	 */
	public String compress(String zipPath, File file) {
		// 压缩后的文件对象
		File zipFile = new File(zipPath);
		String md5 = null;
		try {
			// 创建写出流操作
			FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
			CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
			ZipOutputStream out = new ZipOutputStream(cos);
			/*
			 * (1)如果在zip压缩文件中不需要一级文件目录，定义String basedir = "";
			 * 下面的compress方法中当判断文件file是目录后不需要加上basedir = basedir +
			 * file.getName() + File.separator;
			 * (2)如果只是想在压缩后的zip文件里包含一级文件目录，不包含二级以下目录， 直接在这定义String basedir =
			 * file.getName() + File.separator;
			 * 下面的compress方法中当判断文件file是目录后不需要加上basedir = basedir +
			 * file.getName() + File.separator;
			 * (3)如果想压缩后的zip文件里包含一级文件目录，也包含二级以下目录，即zip文件里的目录结构和原文件一样
			 * 在此定义String basedir = "";
			 * 下面的compress方法中当判断文件file是目录后需要加上basedir = basedir +
			 * file.getName() + File.separator;
			 */
			// String basedir = file.getName() + File.separator;
			String basedir = "";
			md5 = compress(file, out, basedir);
			out.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return md5;
	}

	private String compress(File file, ZipOutputStream out, String basedir) throws Exception {
		/*
		 * 判断是目录还是文件
		 */
		if (file.isDirectory()) {
			basedir += file.getName() + File.separator;
			compressDirectory(file, out, basedir);
			return null;
		} else {
			System.out.println("压缩：" + basedir + file.getName());
			return compressFile(file, out, basedir);
		}
	}

	/**
	 * 压缩一个目录
	 * @throws FileNotFoundException 
	 */
	private void compressDirectory(File dir, ZipOutputStream out, String basedir) throws Exception {
		if (!dir.exists()) {
			return;
		}
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			/* 递归 */
			compress(files[i], out, basedir);
		}
	}

	/**
	 * 压缩一个文件
	 * @return 
	 * @throws FileNotFoundException 
	 */
	private String compressFile(File file, ZipOutputStream out, String basedir) throws Exception {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}
		MessageDigest MD5 = null;if (getMD5) MD5 = MessageDigest.getInstance("MD5");
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		// 创建Zip实体，并添加进压缩包
		ZipEntry entry = new ZipEntry(basedir + file.getName());
		out.putNextEntry(entry);
		// 读取待压缩的文件并写进压缩包里
		int count;
		byte data[] = new byte[BUFFER];
		while ((count = bis.read(data, 0, BUFFER)) != -1) {
			out.write(data, 0, count);
			if (getMD5) MD5.update(data, 0, count);
		}
		bis.close();
		String md5 = "";
		if (getMD5) md5 = new String(Hex.encodeHex(MD5.digest()));
		return md5;
	}

	/**
	 * 解压缩
	 * 
	 * @param sourceFile
	 *            要解压缩的文件的路径
	 * @param destDir
	 *            解压缩后的目录路径
	 * @throws Exception
	 */
	public String deCompress(String sourceFile, String destDir, String dest) throws Exception {
		// 创建需要解压缩的文件对象
		File file = new File(sourceFile);
		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}
		// 创建解压缩的文件目录对象
		File destDiretory = new File(destDir);
		if (!destDiretory.exists()) {
			destDiretory.mkdirs();
		}
		/*
		 * 保证文件夹路径最后是"/"或者"\" charAt()返回指定索引位置的char值
		 */
		char lastChar = destDir.charAt(destDir.length() - 1);
		if (lastChar != '/' && lastChar != '\\') {
			// 在最后加上分隔符
			destDir += File.separator;
		}
		return unzip(sourceFile, destDir, dest);
	}

	/**
	 * 解压方法 需要ant.jar
	 */
	private String unzip(String sourceZip, String destDir, String dest) throws Exception {
		Project p = new Project();
		Expand e = new Expand();
		File srcFile = new File(sourceZip);
		File destFile = new File(destDir);
		e.setProject(p);
		e.setSrc(srcFile);
		e.setOverwrite(false);
		e.setDest(destFile);
		e.execute();
		String md5 = "";
		if (getMD5) {
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(dest)));
			int count;
			byte data[] = new byte[BUFFER];
			while ((count = bis.read(data, 0, BUFFER)) != -1) {
				MD5.update(data, 0, count);
			}
			md5 = new String(Hex.encodeHex(MD5.digest()));
			bis.close();
		}
		return md5;
	}

	public static void main(String[] args) throws Exception {
		String srcPathName1 = "C:/Users/zzq12/Desktop/";
		String zipPath1 = "C:/Users/zzq12/Desktop/test.zip";
//		new ZipUtils(false).compress(zipPath1, srcPathName1);
		new ZipUtils(false).deCompress(zipPath1, srcPathName1, "");
//		String sourceFile = "C:/Users/zzq12/Desktop/test.zip";
//		String destDir = "C:/Users/zzq12/Desktop/test";
//		new ZipUtils().deCompress(sourceFile, destDir);
	}
}
