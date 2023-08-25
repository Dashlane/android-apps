package com.github.devnied.emvnfccard.parser.apdu.annotation;

import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.model.CPLC;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.parser.apdu.IFile;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class AnnotationUtils {

	@SuppressWarnings("unchecked")
	private static final Class<? extends IFile>[] LISTE_CLASS = new Class[] { EmvTransactionRecord.class, CPLC.class };

	private static final AnnotationUtils INSTANCE = new AnnotationUtils();

	public static AnnotationUtils getInstance() {
		return INSTANCE;
	}

	private final Map<String, Map<ITag, AnnotationData>> map;
	private final Map<String, Set<AnnotationData>> mapSet;

	private AnnotationUtils() {
		map = new HashMap<String, Map<ITag, AnnotationData>>();
		mapSet = new HashMap<String, Set<AnnotationData>>();
		extractAnnotation();
	}

	private void extractAnnotation() {
		for (Class<? extends IFile> clazz : LISTE_CLASS) {

			Map<ITag, AnnotationData> maps = new HashMap<ITag, AnnotationData>();
			Set<AnnotationData> set = new TreeSet<AnnotationData>();

			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				AnnotationData param = new AnnotationData();
				field.setAccessible(true);
				param.setField(field);
				Data annotation = field.getAnnotation(Data.class);
				if (annotation != null) {
					param.initFromAnnotation(annotation);
					maps.put(param.getTag(), param);
					try {
						set.add((AnnotationData) param.clone());
					} catch (CloneNotSupportedException e) {
						
					}
				}
			}
			mapSet.put(clazz.getName(), set);
			map.put(clazz.getName(), maps);
		}
	}

	public Map<String, Set<AnnotationData>> getMapSet() {
		return mapSet;
	}

	public Map<String, Map<ITag, AnnotationData>> getMap() {
		return map;
	}

}
