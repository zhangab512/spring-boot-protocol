package com.github.netty.core.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * Resource management (note: prefix all directories with/for operations)
 * @author wangzihao
 */
public class ResourceManager {
    private LoggerX logger = LoggerFactoryX.getLogger(getClass());
    private String rootPath;
    private ClassLoader classLoader;
    private String workspace;

    public ResourceManager(String rootPath) {
        this(rootPath,"");
    }

    public ResourceManager(String rootPath, String workspace) {
        this(rootPath,workspace,ResourceManager.class.getClassLoader());
    }

    public ResourceManager(String rootPath, String workspace,ClassLoader classLoader) {
        if(rootPath == null || rootPath.isEmpty()){
            throw new IllegalStateException("path empty");
        }
        if(rootPath.startsWith("file:") || rootPath.startsWith("FILE:")){
            this.rootPath = rootPath.replace("file:","").replace("FILE:","");
        }else {
            this.rootPath = rootPath;
        }
        if(workspace == null || workspace.equals("/")){
            workspace = "";
        }
        if(workspace.length() > 0 && workspace.charAt(0) != '/'){
            workspace = "/".concat(workspace);
        }
        this.workspace = workspace;
        this.classLoader = classLoader == null? getClass().getClassLoader():classLoader;
        logger.info("ResourceManager rootPath={},workspace={}",rootPath,workspace);
    }

    /**
     * Gets the number of folders in the path
     * @param path path
     * @return Number of folders
     */
    public int countDir(String path) {
        Objects.requireNonNull(path);
        if(path.isEmpty() || (path.charAt(path.length()-1) != '/')){
            path = path.concat("/");
        }
        String basePath = getRealPath(path);
        if (basePath == null) {
            return 0;
        }
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory()) {
            return 0;
        }
        String theFiles[] = theBaseDir.list();
        if (theFiles == null) {
            return 0;
        }

        int count = 0;
        String rootPath = basePath.concat(File.separator);
        for (String filename : theFiles) {
            File testFile = new File(rootPath.concat(filename));
            if (testFile.isDirectory()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the number of files in the path
     * @param path path
     * @return The number of files
     */
    public int countFile(String path) {
        Objects.requireNonNull(path);
        if(path.isEmpty() || (path.charAt(path.length()-1) != '/')){
            path = path.concat("/");
        }
        String basePath = getRealPath(path);
        if (basePath == null) {
            return 0;
        }
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory()) {
            return 0;
        }
        String theFiles[] = theBaseDir.list();
        if (theFiles == null) {
            return 0;
        }

        int count = 0;
        String rootPath = basePath.concat(File.separator);
        for (String filename : theFiles) {
            File testFile = new File(rootPath.concat(filename));
            if (testFile.isFile()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get all directories under path
     * @param path path
     * @return Directory. If not, return NULL
     */
    public Set<String> getResourcePaths(String path) {
        Objects.requireNonNull(path);
        if(path.isEmpty() || (path.charAt(path.length()-1) != '/')){
            path = path.concat("/");
        }
        String basePath = getRealPath(path);
        if (basePath == null) {
            return null;
        }
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory()) {
            return null;
        }
        String theFiles[] = theBaseDir.list();
        if (theFiles == null) {
            return null;
        }

        Set<String> thePaths = null;
        String rootPath = basePath.concat(File.separator);
        for (String filename : theFiles) {
            File testFile = new File(rootPath.concat(filename));
            if (testFile.isFile()) {
                if(thePaths == null){
                    thePaths = new HashSet<>();
                }
                thePaths.add(filename);
            } else if (testFile.isDirectory()) {
                if(thePaths == null){
                    thePaths = new HashSet<>();
                }
                thePaths.add(filename.concat("/"));
            }
        }
        return thePaths;
    }

    /**
     * Access to resources
     * @param path Relative paths
     * @return The url address
     * @throws MalformedURLException MalformedURLException
     */
    public URL getResource(String path) throws MalformedURLException {
        if(rootPath == null || rootPath.length() == 0){
            return null;
        }
        if (path.isEmpty() || path.charAt(0) != '/') {
            throw new MalformedURLException("Path '" + path + "' does not start with '/'");
        }

        String realPath = getRealPath(path);
        if(realPath == null){
            return null;
        }

        File file = new File(realPath);
        if(file.exists()){
            return new URL("file:".concat(realPath));
        }
        return null;
    }

    /**
     * Gets the data input stream
     * @param path path
     * @return InputStream
     */
    public InputStream getResourceAsStream(String path) {
        try {
            if (path.isEmpty() || path.charAt(0) != '/') {
                logger.warn("Path '{}' does not start with '/'",path);
                return null;
            }

            URL url = getResource(path);
            if(url == null){
                return null;
            }
            return url.openStream();
        } catch (IOException e) {
            logger.warn("Throwing exception when getResourceAsStream of {}, case {} ",path,e.getMessage());
            return null;
        }
    }

    /**
     * Get the real path
     * @param path Relative paths
     * @return RealPath
     */
    public String getRealPath(String path) {
        if(path == null || path.isEmpty()){
            path = File.separator;
        }
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = File.separator.concat(path);
        }
        String realPath;
        if(workspace.isEmpty()){
            realPath = rootPath.concat(path);
        }else {
            realPath = rootPath.concat(workspace).concat(path);
        }
        return realPath;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Write files
     * @param inputStream data
     * @param targetPath targetPath
     * @param targetFileName targetFileName
     * @throws IOException IOException
     */
    public void writeFile(InputStream inputStream, String targetPath, String targetFileName) throws IOException {
        IOUtil.writeFile(inputStream, getRealPath(targetPath),targetFileName,false,8192);
    }

    /**
     * Write files
     * @param dataIterator data
     * @param targetPath targetPath
     * @param targetFileName targetFileName
     * @throws IOException IOException
     */
    public void writeFile(Iterator<ByteBuffer> dataIterator, String targetPath, String targetFileName) throws IOException {
        IOUtil.writeFile(dataIterator, getRealPath(targetPath),targetFileName,false);
    }

    /**
     * Write files
     * @param data data
     * @param targetPath targetPath
     * @param targetFileName targetFileName
     * @throws IOException IOException
     */
    public void writeFile(byte[]data, String targetPath, String targetFileName) throws IOException {
        IOUtil.writeFile(data, getRealPath(targetPath),targetFileName,false);
    }

    /**
     * Write files
     * @param data data
     * @param targetPath targetPath
     * @param targetFileName targetFileName
     * @param append Whether to concatenate old data
     * @throws IOException IOException
     */
    public void writeFile(byte[]data, String targetPath, String targetFileName, boolean append) throws IOException {
        IOUtil.writeFile(data, getRealPath(targetPath),targetFileName,append);
    }

    /**
     * Write file (note: close it after using)
     * @param targetPath targetPath
     * @param targetFileName targetFileName
     * @param append Whether to concatenate old data
     * @throws IOException IOException
     * @return FileOutputStream
     */
    public FileOutputStream writeFile(String targetPath, String targetFileName, boolean append) throws IOException {
        return IOUtil.writeFile(getRealPath(targetPath),targetFileName,append);
    }

    /**
     * Read the file (note: close it after using)
     * @param sourcePath sourcePath
     * @param sourceFileName sourceFileName
     * @return FileInputStream
     * @throws FileNotFoundException FileNotFoundException
     */
    public FileInputStream readFile(String sourcePath,String sourceFileName) throws FileNotFoundException {
        return IOUtil.readFile(getRealPath(sourcePath),sourceFileName);
    }

    /**
     * Copy files
     * @param sourcePath sourcePath
     * @param sourceFileName sourceFileName
     * @param targetPath targetPath
     * @param targetFileName targetFileName
     * @param buffCapacity buffCapacity
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     */
    public void copyTo(String sourcePath,String sourceFileName,
                                    String targetPath,String targetFileName,int buffCapacity) throws FileNotFoundException,IOException {
        IOUtil.copyTo(getRealPath(sourcePath),sourceFileName,
                getRealPath(targetPath),targetFileName,false,buffCapacity);
    }

    /**
     * Create file (can be created with parent file)
     * @param path The file path
     * @return <code>true</code> if and only if the directory was created,
     *          along with all necessary parent directories; <code>false</code>
     *          otherwise
     */
    public boolean mkdirs(String path) {
        if(path == null || path.isEmpty()) {
            return false;
        }
        if(path.charAt(0) != '/'){
            throw new IllegalArgumentException("Path '"+path+"' must start with '/'");
        }
        return new File(getRealPath(path)).mkdirs();
    }

    /**
     * Delete the directory
     * @param path path
     * @return boolean success
     */
    public boolean delete(String path) {
        if(path == null){
            throw new NullPointerException("path");
        }
        if(path.isEmpty() || path.charAt(0) != '/'){
            throw new IllegalArgumentException("Path '"+path+"' must start with '/'");
        }
        return IOUtil.deleteDir(new File(getRealPath(path)));
    }

    /**
     * Delete all subdirectories under directory
     * @param path path
     */
    public void deleteChild(String path) {
        if(path == null){
            throw new NullPointerException("path");
        }
        if(path.isEmpty() || path.charAt(0) != '/'){
            throw new IllegalArgumentException("Path '"+path+"' must start with '/'");
        }
        IOUtil.deleteDirChild(new File(getRealPath(path)));
    }

    /**
     * Get workspace
     * @return workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Get the root path
     * @return rootPath
     */
    public String getRootPath() {
        return rootPath;
    }

    @Override
    public String toString() {
        return "ResourceManager{" +
                "rootPath='" + rootPath + '\'' +
                ", workspace='" + workspace + '\'' +
                '}';
    }
}
