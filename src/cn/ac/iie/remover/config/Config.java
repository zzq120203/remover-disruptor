package cn.ac.iie.remover.config;

import java.util.Map;

public class Config {
	

	@FieldMeta(isOptional = false, desc = "启动服务模式")
	public static int serviceMode;

	@FieldMeta(isOptional = false, desc = "服务ID")
	public static String hostID;
	
	@FieldMeta(isOptional = false, desc = "标记文件结尾")
	public static String fileMark;

	@FieldMeta(isOptional = false, desc = "压缩文件类型")
	public static String zipMark;
	
	//send
	@FieldMeta(isOptional = false, desc = "源文件路径")
	public static String sendDataPath;

	@FieldMeta(isOptional = false, desc = "send脚本路径/名")
	public static String sendScript;

	@FieldMeta(isOptional = false, desc = "touch脚本路径/名")
	public static String touchOK;
	
	@FieldMeta(isOptional = false, desc = "mkdir脚本路径/名")
	public static String mkdirScript;
	
	@FieldMeta(isOptional = false, desc = "压缩线程数")
	public static int czipThreadNumber;
	
	@FieldMeta(isOptional = false, desc = "前推小时数")
	public static int minHour;
	
	//receive
	@FieldMeta(isOptional = false, desc = "目的文件路径")
	public static String receiveDataPath;
	
	@FieldMeta(isOptional = false, desc = "龙存文件路径")
	public static String loongStorePath;
	
	@FieldMeta(isOptional = false, desc = "hdfs文件路径")
	public static String hadoopPath;
	
	@FieldMeta(isOptional = false, desc = "解压缩线程数")
	public static int dczipThreadNumber;

	@FieldMeta(isOptional = false, desc = "是否存hadoop")
	public static boolean toHadoop;

	@FieldMeta(isOptional = false, desc = "是否存hadoop")
	public static Map<String, String> hdfsConf;

	
}
