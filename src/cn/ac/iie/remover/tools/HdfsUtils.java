package cn.ac.iie.remover.tools;

import cn.ac.iie.remover.config.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class HdfsUtils {

    private static Logger log = LoggerFactory.getLogger(HdfsUtils.class);

    private static Configuration conf;

    public HdfsUtils() {
        init();
    }

    private void init() {
        if (null == conf) {
            conf = new Configuration();
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            for (Entry<String, String> entry : Config.hdfsConf.entrySet()) {
                log.info("conf.set('{}', '{}')", entry.getKey(), entry.getValue());
                conf.set(entry.getKey(), entry.getValue());
            }
        }
    }

    private FileSystem getFileSystem() throws Exception {
        return FileSystem.get(new URI("hdfs://mycluster"), conf, "ambari");
    }

    public void createFile(String fileName) {
        try (FileSystem fs = getFileSystem()) {
            Path path = new Path(fileName);
            fs.create(path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean uploadLocalFile2HDFS(String localFile, String hdfsFile) {
        if ((localFile == null || localFile.length() <= 0) || (hdfsFile == null || hdfsFile.length() <= 0)) {
            return false;
        }
        try (FileSystem fs = getFileSystem()) {
            Path src = new Path(localFile);
            Path dst = new Path(hdfsFile);
            fs.copyFromLocalFile(src, dst);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public boolean fileExists(String dir) {
        boolean eFlag = false;
        try (FileSystem fs = getFileSystem()) {
            eFlag = fs.exists(new Path(dir));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return eFlag;
    }

    public boolean mkdir(String dir) {
        if (dir == null || dir.length() <= 0) {
            return false;
        }
        try (FileSystem fs = getFileSystem()) {
            if (!fs.exists(new Path(dir))) {
                fs.mkdirs(new Path(dir));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public List<String> listAll(String dir) {
        if (dir == null || dir.length() <= 0) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        try (FileSystem fs = getFileSystem()) {
            if (fs.exists(new Path(dir))) {
                FileStatus[] stats = fs.listStatus(new Path(dir));
                for (FileStatus file : stats) {
                    names.add(file.getPath().getName());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return names;
    }

    public boolean deleteDir(String dir) {
        if (dir == null || dir.length() <= 0) {
            return false;
        }
        try (FileSystem fs = getFileSystem()) {
            fs.delete(new Path(dir), true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public void downloadFile(String dstPath, String srcPath) {
        Path path = new Path(srcPath);
        File rootfile = new File(dstPath);
        if (!rootfile.exists()) {
            rootfile.mkdirs();
        }
        try (FileSystem fs = getFileSystem()) {
            if (fs.isFile(path)) {
                FSDataInputStream in = null;
                FileOutputStream out = null;
                try {
                    in = fs.open(path);
                    File srcfile = new File(rootfile, path.getName());
                    if (!srcfile.exists())
                        srcfile.createNewFile();
                    out = new FileOutputStream(srcfile);
                    IOUtils.copyBytes(in, out, 4096, false);
                } finally {
                    IOUtils.closeStream(in);
                    IOUtils.closeStream(out);
                }
            } else if (fs.isDirectory(path)) {
                File dstDir = new File(dstPath);
                if (!dstDir.exists()) {
                    dstDir.mkdirs();
                }
                String filePath = path.toString();
                String subPath[] = filePath.split("/");
                String newdstPath = dstPath + subPath[subPath.length - 1] + "/";
                System.out.println("newdstPath=======" + newdstPath);
                if (fs.exists(path) && fs.isDirectory(path)) {
                    FileStatus[] srcFileStatus = fs.listStatus(path);
                    if (srcFileStatus != null) {
                        for (FileStatus status : fs.listStatus(path)) {
                            downloadFile(newdstPath, status.getPath().toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
