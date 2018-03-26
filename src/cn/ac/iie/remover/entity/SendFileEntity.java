package cn.ac.iie.remover.entity;

public class SendFileEntity {

	private String parentDir;

	private String _parentDir;

	private String name;

	private String markName;

	private String zipName;

	private String md5;
	
	private long length;
	
	private boolean compress;
	private boolean rsync;

	public String getParentDir() {
		return parentDir;
	}

	public void setParentDir(String parentDir) {
		this.parentDir = parentDir;
	}

	public String get_parentDir() {
		return _parentDir;
	}

	public void set_parentDir(String _parentDir) {
		this._parentDir = _parentDir;
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

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public boolean isRsync() {
		return rsync;
	}

	public void setRsync(boolean rsync) {
		this.rsync = rsync;
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
		return "SendFileEntity [parentDir=" + parentDir + ", _parentDir=" + _parentDir + ", name=" + name
				+ ", markName=" + markName + ", zipName=" + zipName + ", md5=" + md5 + ", length=" + length
				+ ", compress=" + compress + ", rsync=" + rsync + "]";
	}

	public void init() {
		this.parentDir = null;
		this._parentDir = null;
		this.name = null;
		this.markName = null;
		this.zipName = null;
		this.length = -1L;
		this.md5 = null;
		this.compress = false;
		this.rsync = false;
	}

}
