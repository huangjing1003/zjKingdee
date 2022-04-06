package com.kingdee.eas.custom.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import com.kingdee.util.StringUtils;
import org.apache.tools.ant.util.DateUtils;

/***
 * 配置文件读取工具类
 * @author hj
 */
public class ConfigUtils {
	private static Properties properties = null;
	private static boolean isConfigLoaded = false;


	/**
	 * 获取配置文件的地址
	 * @return
	 */
	public static String getDefaultLtpaConfig() {
		String property = System.getProperty("EAS_INSTANCE_CONFIG_PATH").trim();
		return property+ "/portalConfig/systemConfig.properties";
	}
	

	private static boolean isConfigLoaded() {
		return isConfigLoaded;
	}
	/**
	 * 读取配置文件 .properties
	 * @return
	 */
	public static  Map<String, String>  loadConfig() {
		String configFile = getDefaultLtpaConfig();
		HashMap<String,String> map = new HashMap<String, String>();//返回的结果
		if(!StringUtils.isEmpty(configFile)){
			if (isConfigLoaded()){
				return null;
			}else{
				properties = new Properties();
				InputStream is = null;
				try {
					is = new FileInputStream(configFile);
					
					properties.load(is);
					Set<Entry<Object,Object>> entrySet = properties.entrySet();
					//使用迭代器循环封装map，直接获取key和value
					Iterator<Entry<Object, Object>> iterator = entrySet.iterator();
					while(iterator.hasNext()){
						Entry<Object, Object> next = iterator.next();
						String key = next.getKey().toString();
						String value = next.getValue().toString();
						//封装需要返回的map结果
						map.put(key, value);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
					System.out.println("---------读取配置文件失败---------");
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else{
			return null;
		}
		System.out.println("=========读取的配置文件为："+map);
		return map;
	}
	
}
