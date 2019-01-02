package com.kaifantech.component.service.pi.path.distance;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.kaifantech.component.dao.ControlPIInfoDao;
import com.kaifantech.component.service.sys.IInitCacheService;
import com.kaifantech.util.log.AppFileLogger;
import com.ytgrading.util.AppTool;

@Component
@Scope("prototype")
public class DifferByMsg implements IInitCacheService {

	@Autowired
	private ControlPIInfoDao controlPIInfoDao;

	private boolean isInit = false;

	public boolean isInit() {
		return isInit;
	}

	public void initPIParam() {
		try {
			if (!isInit) {
				getParamFromDB();
			}
		} catch (Exception e) {
			this.isInit = false;
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		DifferByMsg differ = new DifferByMsg();
		// differ.init();
		differ.getClassNameByFile("C:\\Program Files (x86)\\Common Files\\Intel Corporation\\IAStorUtil", null, false);
	}

	public void getParamFromDB() throws Exception {
		AppFileLogger.piLogs("初始化防撞参数开始！");
		String packageName = "com.kaifantech.util.constant.pi";
		List<String> classNames = getClassName(packageName, false);
		if (!AppTool.isNull(classNames)) {
			AppFileLogger.piLogs("classNames.size():" + classNames.size());
		}
		if (classNames != null) {
			for (String className : classNames) {
				initAClass(className);
			}
		}
		this.isInit = true;
		AppFileLogger.piLogs("初始化防撞参数结束！");
	}

	public void initAClass(String className) throws Exception {
		Class<?> c;
		c = Class.forName(className);
		Field[] fs = c.getDeclaredFields();
		Object obj = c.newInstance();
		StringBuffer sb = new StringBuffer();
		for (Field field : fs) {
			field.set(obj, controlPIInfoDao.getValueBy(c.getSimpleName(), field.getName()));
		}
		sb.append(Modifier.toString(c.getModifiers()) + " class " + c.getSimpleName() + "{\n");
		for (Field field : fs) {
			sb.append("\t");
			sb.append(Modifier.toString(field.getModifiers()) + " ");
			sb.append(field.getType().getSimpleName() + " ");
			sb.append(field.getName() + " = " + field.getInt(obj) + ";\n");
		}
		sb.append("}");

		AppFileLogger.piLogs(sb.toString());
	}

	public List<String> getClassName(String packageName, boolean childPackage) throws Exception {
		List<String> fileNames = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String packagePath = packageName.replace(".", "/");
		URL url = loader.getResource(packagePath);
		if (url != null) {
			String type = url.getProtocol();
			if (type.equals("file")) {
				fileNames = getClassNameByFile(url.getPath(), null, childPackage);
			} else if (type.equals("jar")) {
				fileNames = getClassNameByJar(url.getPath(), childPackage);
			}
		} else {
			fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
		}
		return fileNames;
	}

	private List<String> getClassNameByJar(String jarPath, boolean childPackage) throws Exception {
		List<String> myClassName = new ArrayList<String>();
		String[] jarInfo = jarPath.split("!");
		String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
		String packagePath = jarInfo[1].substring(1);
		@SuppressWarnings("resource")
		JarFile jarFile = new JarFile(jarFilePath);
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry jarEntry = entrys.nextElement();
			String entryName = jarEntry.getName();
			if (entryName.endsWith(".class")) {
				if (childPackage) {
					if (entryName.startsWith(packagePath)) {
						entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
						myClassName.add(entryName);
					}
				} else {
					int index = entryName.lastIndexOf("/");
					String myPackagePath;
					if (index != -1) {
						myPackagePath = entryName.substring(0, index);
					} else {
						myPackagePath = entryName;
					}
					if (myPackagePath.equals(packagePath)) {
						entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
						myClassName.add(entryName);
					}
				}
			}
		}
		return myClassName;
	}

	/**
	 * 从所有jar中搜索该包，并获取该包下所有类
	 * 
	 * @param urls
	 *            URL集合
	 * @param packagePath
	 *            包路径
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 * @throws Exception
	 */
	private List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) throws Exception {
		List<String> myClassName = new ArrayList<String>();
		if (urls != null) {
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				String urlPath = url.getPath();
				// 不必搜索classes文件夹
				if (urlPath.endsWith("classes/")) {
					continue;
				}
				String jarPath = urlPath + "!/" + packagePath;
				myClassName.addAll(getClassNameByJar(jarPath, childPackage));
			}
		}
		return myClassName;
	}

	/**
	 * 获取某包下（包括该包的所有子包）所有类
	 * 
	 * @param packageName
	 *            包名
	 * @return 类的完整名称
	 * @throws Exception
	 */
	public List<String> getClassName(String packageName) throws Exception {
		return getClassName(packageName, true);
	}

	/**
	 * 从项目文件获取某包下所有类
	 * 
	 * @param filePath
	 *            文件路径
	 * @param className
	 *            类名集合
	 * @param childPackage
	 *            是否遍历子包
	 * @return 类的完整名称
	 */
	private List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {
		List<String> myClassName = new ArrayList<String>();
		String path = null;
		path = filePath.replaceAll("%20", " ");
		File file = new File(path);
		File[] childFiles = file.listFiles();
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				if (childPackage) {
					myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));
				}
			} else {
				String childFilePath = childFile.getPath();
				if (childFilePath.endsWith(".class")) {
					childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9,
							childFilePath.lastIndexOf("."));
					childFilePath = childFilePath.replace("\\", ".");
					myClassName.add(childFilePath);
				}
			}
		}

		return myClassName;
	}

	@Override
	public void init() {
		this.isInit = false;
	}

}
