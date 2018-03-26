package cn.ac.iie.remover.entity;

public class ReceiveFileEntity {

	private String parentDir;

	private String name;

	private String markName;

	private String zipName;

	private String md5;
	
	private long length;
	
	private boolean toLoong;

	private boolean putHDFS;

	private boolean deCompress;

	public String getParentDir() {
		return parentDir;
	}

	public void setParentDir(String parentDir) {
		this.parentDir = parentDir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMarkName() {
		return markName;
	}

	public void setMarkName(String markName) {
		this.markName = markName;
	}

	public String getZipName() {
		return zipName;
	}

	public void setZipName(String zipName) {
		this.zipName = zipName;
	}

	public boolean isToLoong() {
		return toLoong;
	}

	public void setToLoong(boolean toLoong) {
		this.toLoong = toLoong;
	}

	public boolean isPutHDFS() {
		return putHDFS;
	}

	public void setPutHDFS(boolean putHDFS) {
		this.putHDFS = putHDFS;
	}

	public boolean isDeCompress() {
		return deCompress;
	}

	public void setDeCompress(boolean deCompress) {
		this.deCompress = deCompress;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	@Override
	public String toString() {
		return "ReceiveFileEntity [parentDir=" + parentDir + ", name=" + name + ", markName=" + markName + ", zipName="
				+ zipName + ", md5=" + md5 + ", length=" + length + ", toLoong=" + toLoong + ", putHDFS=" + putHDFS
				+ ", deCompress=" + deCompress + "]";
	}

	public void init() {
		this.parentDir = null;
		this.name = null;
		this.markName = null;
		this.zipName = null;
		this.md5 = null;
		this.length = -1L;
		this.toLoong = false;
		this.putHDFS = false;
		this.deCompress = false;
	}

}
