package com.unis.zkydatadetection.Controller;


import ch.qos.logback.classic.Logger;
import com.unis.zkydatadetection.model.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.util.*;

@Slf4j
@Controller
@RequestMapping(value = "/tree")
public class TreeController {

	private final Logger log = (Logger) LoggerFactory.getLogger("TreeController.class");

	@Autowired
	JdbcTemplate jdbcTemplate;

	/**
	 * 档案门类tree
	 **/
	@ResponseBody
	@RequestMapping(value = "/getArcTree", produces = {"application/json;charset=UTF-8"})
	private List<Map<String, String>> getArcTree(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, String>> myList = new ArrayList<>();
		try {
			String sql = "select * from s_arc";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
			for (Map<String, Object> map : list) {
				Map<String, String> myMap = new HashMap<>();
				myMap.put("id", String.valueOf((Integer) map.get("libcode")));
				myMap.put("name", (String) map.get("chname"));
				myList.add(myMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return myList;
	}

	/**
	 * 磁盘目录tree
	 **/
	@ResponseBody
	@RequestMapping(value = "/getDirTree", produces = {"application/json;charset=UTF-8"})
	private List<Map<String, String>> getDirTree(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, String>> tree = null;
		try {
			tree = getTree("d:/data", null, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tree;
	}

	/**
	 * 所有磁盘根目录tree
	 **/
	@ResponseBody
	@RequestMapping(value = "/getDiskRootTree", produces = {"application/json;charset=UTF-8"})
	private List<Map<String, String>> getDiskRootTree(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, String>> tree = new ArrayList<>();
		try {
			File[] roots = File.listRoots();
			for (int i = 0; i < roots.length; i++) {
				Map<String, String> map = new LinkedHashMap<>();
				File file = roots[i];
				map.put("id", String.valueOf(i));
				map.put("pId", "");
				map.put("name", file.getAbsolutePath());
				map.put("path", file.getAbsolutePath());
				map.put("isParent","true");
				tree.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tree;
	}

	/**
	 * 获取子节点tree
	 * @param type 1 只要目录 2 目录+文件
	 **/
	@ResponseBody
	@RequestMapping(value = "/getChildrenNodes", produces = {"application/json;charset=UTF-8"})
	private List<Map<String, String>> getChildrenNodes(HttpServletRequest request, HttpServletResponse response,String id,String path,String type) {
		List<Map<String, String>> tree = new ArrayList<>();
		try {
			File root = new File(path);
			File[] filesList = null;
			if("1".equals(type)){
				filesList = root.listFiles();
			}else if("2".equals(type)){
				filesList = root.listFiles(getFileFilter());
			}
			for (int i = 0; i < filesList.length; i++) {
				Map<String, String> map = new LinkedHashMap<>();
				File file = filesList[i];
				if("1".equals(type)){
					if(file.isDirectory()){
						String absolutePath = file.getAbsolutePath();
						map.put("id", String.valueOf(id+i));
						map.put("pId", id);
						map.put("name", absolutePath.substring(absolutePath.lastIndexOf(File.separator)+1));
						map.put("path", absolutePath);
						map.put("isParent","true");
						tree.add(map);
					}
				}else if("2".equals(type)){
					if(file.exists()){
						String absolutePath = file.getAbsolutePath();
						map.put("id", String.valueOf(id+i));
						map.put("pId", id);
						map.put("name", absolutePath.substring(absolutePath.lastIndexOf(File.separator)+1));
						map.put("path", absolutePath);
						if(file.isDirectory()){
							map.put("isParent","true");
						}else{
							map.put("isParent","false");
						}
						tree.add(map);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tree;
	}

	/**
	 * 磁盘目录文件tree
	 * 允许格式 .xls,.xlsx,.zip
	 **/
	@ResponseBody
	@RequestMapping(value = "/getFileTree", produces = {"application/json;charset=UTF-8"})
	private List<Map<String, String>> getFileTree(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, String>> tree = null;
		try {
			List<String> ext = new ArrayList<>();
			ext.add("xls");
			ext.add("xlsx");
			ext.add("zip");
			tree = getTree("d:/data", ext, 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tree;
	}

	/**
	 * 获取指定路径下的文件结构tree<br/>
	 * 利用队列先进先出的特点，每取出一个节点，就放入其所有直接的子节点
	 * @param path 路径 示例：d:/phpPath
	 * @param ext  筛选文件扩展名
	 * @param type 1 只要目录 2 目录+文件
	 * @return 指定路径下的文件结构tree
	 **/
	private List<Map<String, String>> getTree(String path,List<String> ext,Integer type) {
		int id = 0;
		List<Map<String, String>> ret = new ArrayList<>();
		Queue<Map<String, String>> q = new ArrayDeque<>();
		//先处理根节点
		//LinkedHashMap可以保证有序
		Map<String, String> root = new LinkedHashMap<>();
		root.put("id", String.valueOf(id));
		root.put("pId", "");
		root.put("name", path.substring(path.lastIndexOf("/")+1));
		root.put("path", path);
		root.put("isParent","true");
		q.add(root);

		Map<String, String> curr;
		while (!q.isEmpty()) {
			//取出一个
			curr = q.poll();
			//放入结果集
			ret.add(curr);
			//获取子节点
			File currFile = new File(curr.get("path"));
			File[] files = currFile.listFiles();
			if (files != null) {
				for (File f : files) {
					if(!(type==1 && !f.isDirectory())){
						if(type==2){
							if(!f.isDirectory() && !ifHasExt(ext,f.getName())){
								continue;
							}
						}
						Map<String, String> attrs = new LinkedHashMap<>();
						attrs.put("id", String.valueOf(++id));
						attrs.put("pId", curr.get("id"));
						attrs.put("name", f.getName());
						attrs.put("path", f.getAbsolutePath());
						if(f.isDirectory()){
							attrs.put("isParent","true");
						}
						//放入队列
						q.add(attrs);
					}
				}
			}
		}
		return ret;
	}

	private Boolean ifHasExt(List<String> ext, String fileName) {
		Boolean b = false;
		String testExt = fileName.substring(fileName.lastIndexOf(".")+1);
		for (String e : ext) {
			if (e.equalsIgnoreCase(testExt)) {
				b = true;
				break;
			}
		}
		return b;
	}

	private FileFilter getFileFilter(){
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				if(file.exists() && !file.isDirectory()){
					String fileName = file.getName();
					String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
					if(ext.equalsIgnoreCase("xls") || ext.equalsIgnoreCase("xlsx") || ext.equalsIgnoreCase("zip")){
						return true;
					}
				}
				if(file.isDirectory()){
					return true;
				}
				return false;
			}
		};
		return fileFilter;
	}
}
