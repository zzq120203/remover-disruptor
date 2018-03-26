package cn.ac.iie.remover.tools;

import java.io.File;
import java.io.FilenameFilter;

public class MyFilter implements FilenameFilter {
	private String type;

	public MyFilter(String type) {
		this.type = type;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(type);
	}
}
