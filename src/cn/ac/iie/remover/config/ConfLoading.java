package cn.ac.iie.remover.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class ConfLoading {
	private static Logger log = LoggerFactory.getLogger(ConfLoading.class);

	public static void helpInfo(Class<?> _class) {
		Field[] fields = _class.getDeclaredFields();

		log.info("config items:");
		for (Field f : fields) {
			String info = "{" + f.getName() + "}(required:{"
					+ (f.getAnnotation(FieldMeta.class).isOptional() ? "no" : "yes") + "})--{"
					+ f.getAnnotation(FieldMeta.class).desc() + "}";
			log.info(info);
		}
	}

	public static void init(Class<?> _class, String confFile) throws FileNotFoundException, IOException,
			NumberFormatException, IllegalArgumentException, IllegalAccessException {
		log.info("Starting to read config file.");

		Properties prop = new Properties();
		prop.load(new InputStreamReader(new FileInputStream(confFile), "UTF-8"));

		Field[] fields = _class.getDeclaredFields();
		Map<String, Field> nameToFieldMap = new HashMap<String, Field>();

		for (Field f : fields) {
			nameToFieldMap.put(f.getName().toLowerCase(), f);
		}

		for (Map.Entry<Object, Object> en : prop.entrySet()) {
			if (nameToFieldMap.containsKey(en.getKey().toString().toLowerCase())) {
				log.info("{}={}", en.getKey(), en.getValue());
				Field f = nameToFieldMap.get(en.getKey().toString().toLowerCase());
				f.setAccessible(true);
				if (f.getType() == int.class) {
					f.setInt(null, Integer.parseInt(en.getValue().toString()));
				} else if (f.getType() == String.class) {
					f.set(null, en.getValue().toString());
				} else if (f.getType() == long.class) {
					f.set(null, Long.parseLong(en.getValue().toString()));
				} else if (f.getType() == double.class) {
					f.set(null, Double.parseDouble(en.getValue().toString()));
				} else if (f.getType() == boolean.class) {
					f.set(null, Boolean.parseBoolean(en.getValue().toString()));
				} else if (f.getType() == float.class) {
					f.set(null, Float.parseFloat(en.getValue().toString()));
				} else if (f.getType() == List.class) {
					f.set(null, (List<?>)JSON.parse(en.getValue().toString()));
				} else if (f.getType() == Map.class) {
					f.set(null, (Map<?, ?>)JSON.parse(en.getValue().toString()));
				} else {
					throw new RuntimeException("Unknow datatype exception :" + f.getType());
				}
			} else {
				log.info("Undefined config item:{}={}", en.getKey(), en.getValue());
			}
		}

		for (Map.Entry<Object, Object> en : prop.entrySet()) {
			nameToFieldMap.remove(en.getKey().toString().toLowerCase());
		}

		if (nameToFieldMap.size() > 0) {
			for (Map.Entry<String, Field> en : nameToFieldMap.entrySet()) {
				en.getValue().setAccessible(true);
				FieldMeta fm = (FieldMeta) en.getValue().getAnnotation(FieldMeta.class);
				if (fm.isOptional() == false) {
					log.error("Config item:" + en.getValue().getName() + " is required.");
					throw new RuntimeException("Config item:" + en.getValue().getName() + " is required.");
				}
				log.info("{}={} (default value)", en.getValue().getName(), en.getValue().get(null));
			}
		}
	}
}
