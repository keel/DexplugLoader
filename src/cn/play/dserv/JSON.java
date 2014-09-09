/**
 * 
 */
package cn.play.dserv;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON工具,注意对象中不能有自我引用!本类不作此检测.本类不支持bean的解析(bean将直接输出toString).
 * @author keel
 *
 */
public class JSON {

	/**
	 * 
	 */
	public JSON() {
	}
	
	/**
	 * 检查Map是否存在指定的多个String key
	 * @param m Map
	 * @param keys String[]
	 * @return 少任意一个key则返回false
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean checkMapKeys(Map m,String[] keys){
		if (m == null || keys == null) {
			return false;
		}
		for (int i = 0; i < keys.length; i++) {
			if (!m.containsKey(keys[i])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 检查Map是否存在指定的多个String key，且类型分别为valueClass(instanceof)
	 * @param m Map
	 * @param keys String[] 
	 * @param valueClass Class[] 当为Object.class时可为任意类型
	 * @return 少任意一个key则返回false
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean checkMapTypes(Map m,String[] keys,Class[] valueClass){
		if (m == null || keys == null || valueClass == null || keys.length!=valueClass.length) {
			return false;
		}
		for (int i = 0; i < keys.length; i++) {
			//Object可为任意类型
			if (valueClass[i].equals(Object.class)) {
				continue;
			}
			if (!m.containsKey(keys[i]) || (!m.get(keys[i]).getClass().equals(valueClass[i]))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 定位到Json的某一个节点
	 * @param root HashMap<String,Object> 
	 * @param jsonPath  String[]
	 * @return 节点对应对象,若路径不存在则返回null
	 */
	@SuppressWarnings("unchecked")
	public static final Object findJsonNode(HashMap<String,Object> root,String[] jsonPath){
		if (jsonPath==null || jsonPath.length<1) {
			return null;
		}
		HashMap<String,Object> target =  root;
		//定位到需要更新的节点
		for (int i = 0; i < jsonPath.length; i++) {
			if (target == null) {
				return null;
			}
			target = (HashMap<String,Object>)(target.get(jsonPath[i]));
		}
		return target;
	}
	
	/**
	 * 将对象以JSON格式输出,不支持bean(bean将直接输出toString),注意对象中不能有自我引用!
	 * @param obj
	 * @return String
	 */
	public static final String write(Object obj){
		StringBuilder sb = new StringBuilder();
		value(sb,obj);
		return sb.toString();
	}
	
	private static final void map(StringBuilder sb,Map map) {
        sb.append("{");
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            value(sb,e.getKey());
            sb.append(":");
            value(sb,e.getValue());
            if (it.hasNext()) sb.append(',');
        }
        sb.append("}");
    }
	
	@SuppressWarnings("unchecked")
	private static final void value(StringBuilder sb, Object object) {
		if (object == null) {
			sb.append("null");
			return;
		} else {
			if (object instanceof Number) {
				sb.append(object);
			} else if (object instanceof String) {
				jsonString(sb, object);
			} else if (object instanceof Map)
				map(sb, (Map) object);
			else if (object instanceof Boolean)
				sb.append((Boolean)object?"true":"false");
			else if (object instanceof Collection)
				array(sb, ((Collection) object).iterator());
			else if (object.getClass().isArray())
				array(sb, object);
			else if (object instanceof Iterator)
				array(sb, (Iterator) object);
			else {
				jsonString(sb, object);
			};
		}
	}
	 
	 @SuppressWarnings("unchecked")
	private static final void array(StringBuilder sb, Iterator it) {
		sb.append("[");
		while (it.hasNext()) {
			value(sb, it.next());
			if (it.hasNext())
				sb.append(",");
		}
		sb.append("]");
	}

	private static final void array(StringBuilder sb, Object object) {
		sb.append("[");
		int length = Array.getLength(object);
		for (int i = 0; i < length; ++i) {
			value(sb, Array.get(object, i));
			if (i < length - 1)
				sb.append(',');
		}
		sb.append("]");
	}

	public static final void jsonString(StringBuilder sb,Object obj) {
        sb.append('"');
        CharacterIterator it = new StringCharacterIterator(obj.toString());
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == '"') {sb.append("\\\"");continue;}
            else if (c == '\\') {sb.append("\\\\");continue;}
            else if (c == '/') {sb.append("\\/");continue;}
            else if (c == '\b') {sb.append("\\b");continue;}
            else if (c == '\f') {sb.append("\\f");continue;}
            else if (c == '\n') {sb.append("\\n");continue;}
            else if (c == '\r') {sb.append("\\r");continue;}
            else if (c == '\t') {sb.append("\\t");continue;}
            else if (Character.isISOControl(c)) {
                unicode(sb,c);continue;
            } else {
            	sb.append(c);
            }
        }
        sb.append('"');
    }
	private final static char[] hex = "0123456789ABCDEF".toCharArray();
	
	private static final void unicode(StringBuilder sb, char c) {
		sb.append("\\u");
		int n = c;
		for (int i = 0; i < 4; ++i) {
			int digit = (n & 0xf000) >> 12;
			sb.append(hex[digit]);
			n <<= 4;
		}
	}
	
	/**
	 * 格式化JSON输出
	 * @param obj 目标对象
	 * @param formatDeep 格式化深度
	 * @return 格式化后的String
	 */
	public static final String writeFormat(Object obj,int formatDeep){
		StringBuilder sb = new StringBuilder();
		int deep = 0;
		value(sb,obj,deep,formatDeep);
		return sb.toString();
	}
	
	/**
	 * 格式化输出JSON,默认深度为5
	 * @param obj 目标对象
	 * @return 格式化后的String
	 */
	public static final String writeFormat(Object obj){
		return writeFormat(obj,5);
	}
	
    /**
     * 加入缩进
     * @param sb
     * @param blankCount
     */
    private static final void addBlankFormat(StringBuilder sb,int blankCount){
    	for (int i = 0; i < blankCount; i++) {
    		sb.append("\t");
		}
    }
	
	/**
	 * 添加格式化字符串
	 * @param sb 原StringBuilder
	 * @param deeper 深度加深或保持或减少的标记,(分别对应1,0,-1)
	 * @param deep 当前深度
	 * @param formatDeep 格式化深度
	 * @return
	 */
	private static final int addFormat(StringBuilder sb, int deeper, int deep,
			int formatDeep) {
		if (deep < formatDeep) {
			sb.append("\r\n");
			if (deeper > 0) {
				deep++;
			} else if (deeper < 0) {
				deep--;
			}
			addBlankFormat(sb, deep);
		} else if (deep == formatDeep) {
			if (deeper > 0) {
				// System.out.println(buf);
				// System.out.println("-------- deep:"+deep+" deeper:"+deeper);
				deep++;
			} else if (deeper < 0) {
				// System.out.println(buf);
				// System.out.println("-------- deep:"+deep+" deeper:"+deeper);
				sb.append("\r\n");
				deep--;
				addBlankFormat(sb, deep);
			}
		} else {
			// System.out.println(buf);
			// System.out.println(">>>>> deep:"+deep+" deeper:"+deeper);
			if (deeper < 0) {
				deep--;
			}
		}
		return deep;
	}

	@SuppressWarnings("unchecked")
	private static final int value(StringBuilder sb, Object object, int deep,
			int formatDeep) {
		if (object == null) {
			sb.append("null");
			return deep;
		} else {
			if (object instanceof Number) {
				sb.append(object);
				return deep;
			} else if (object instanceof String) {
				jsonString(sb, object);
				return deep;
			} else if (object instanceof Map) {
				deep = map(sb, (Map) object, deep, formatDeep);
				return deep;
			} else if (object instanceof Boolean) {
				sb.append((Boolean) object ? "true" : "false");
				return deep;
			} else if (object instanceof Collection) {
				deep = array(sb, ((Collection) object).iterator(), deep,
						formatDeep);
				return deep;
			} else if (object.getClass().isArray()) {
				deep = array(sb, object, deep, formatDeep);
				return deep;
			} else if (object instanceof Iterator) {
				deep = array(sb, (Iterator) object, deep, formatDeep);
				return deep;
			} else {
				jsonString(sb, object);
				return deep;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static final int map(StringBuilder sb, Map map, int deep,
			int formatDeep) {
		sb.append("{");
		deep = addFormat(sb, 1, deep, formatDeep);
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			deep = value(sb, e.getKey(), deep, formatDeep);
			sb.append(":");
			deep = value(sb, e.getValue(), deep, formatDeep);
			if (it.hasNext()) {
				sb.append(',');
				deep = addFormat(sb, 0, deep, formatDeep);
			}
		}
		deep = addFormat(sb, -1, deep, formatDeep);
		sb.append("}");
		return deep;
	}

	@SuppressWarnings("unchecked")
	private static final int array(StringBuilder sb, Iterator it, int deep,
			int formatDeep) {
		sb.append("[");
		deep = addFormat(sb, 1, deep, formatDeep);
		while (it.hasNext()) {
			deep = value(sb, it.next(), deep, formatDeep);
			if (it.hasNext()) {
				sb.append(",");
				deep = addFormat(sb, 0, deep, formatDeep);
			}
		}
		deep = addFormat(sb, -1, deep, formatDeep);
		sb.append("]");
		return deep;
	}

	private static final int array(StringBuilder sb, Object object, int deep,
			int formatDeep) {
		sb.append("[");
		deep = addFormat(sb, 1, deep, formatDeep);
		int length = Array.getLength(object);
		for (int i = 0; i < length; ++i) {
			deep = value(sb, Array.get(object, i), deep, formatDeep);
			if (i < length - 1) {
				sb.append(',');
				deep = addFormat(sb, 0, deep, formatDeep);
			}
			;
		}
		deep = addFormat(sb, -1, deep, formatDeep);
		sb.append("]");
		return deep;
	}
	
	//------------------------------
	//以下为jsonReader
	//------------------------------
	
	private static final HashMap<Character,Character> escapes = new HashMap<Character,Character>();
    static {
        escapes.put(new Character('"'), new Character('"'));
        escapes.put(new Character('\\'), new Character('\\'));
        escapes.put(new Character('/'), new Character('/'));
        escapes.put(new Character('b'), new Character('\b'));
        escapes.put(new Character('f'), new Character('\f'));
        escapes.put(new Character('n'), new Character('\n'));
        escapes.put(new Character('r'), new Character('\r'));
        escapes.put(new Character('t'), new Character('\t'));
    }
    
//    private static final int FIRST = 0;
//    private static final int CURRENT = 1;
//    private static final int NEXT = 2;
    private static final Object OBJECT_END = new Object();
    private static final Object ARRAY_END = new Object();
    private static final Object COLON = new Object();
    private static final Object COMMA = new Object();
	
    private static final char unicode(CharacterIterator it) {
        int value = 0;
        for (int i = 0; i < 4; ++i) {
        	char c = it.next();
            switch (c) {
            case '0': case '1': case '2': case '3': case '4': 
            case '5': case '6': case '7': case '8': case '9':
                value = (value << 4) + c - '0';
                break;
            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                value = (value << 4) + c - 'k';
                break;
            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                value = (value << 4) + c - 'K';
                break;
            }
        }
        return (char) value;
    }
    
    
    public static final Object read(String string) {
        Object o = null;
		try {
			StringBuilder buf = new StringBuilder();
			o = readFirst(string,buf);
		} catch (Exception e) {
			o = null;
		}
        return o;
    }
    
    private static final Object readFirst(String string,StringBuilder buf) {
    	CharacterIterator it = new StringCharacterIterator(string);
    	it.first();
		return read(it,buf);
    }
    
    private static final Object read(CharacterIterator it,StringBuilder buf) {
//		char c = 0;
//		switch (start) {
//		case NEXT:
//			c = it.next();
//			break;
//		case FIRST:
//			c = it.first();
//			break;
////		case CURRENT:
////			c = it.current();
////			break;
//		default:
//			//标记错误
//			return null;
//		}
    	char c = it.current();
    	
    	Object token = null;
		//忽略空白
        while (Character.isWhitespace(c)) {
           c = it.next();
        }
        //c = it.next();
        switch (c) {
            case '"': token = string(it,buf); break;
            case '[': token = array(it,buf); break;
            case ']': token = ARRAY_END; break;
            case ',': token = COMMA; break;
            case '{': token = object(it,buf); break;
            case '}': token = OBJECT_END; break;
            case ':': token = COLON; break;
            case 't':
            	it.next(); it.next(); it.next(); // assumed r-u-e
                token = Boolean.TRUE;
                break;
            case'f':
            	it.next(); it.next(); it.next(); it.next(); // assumed a-l-s-e
                token = Boolean.FALSE;
                break;
            case 'n':
            	it.next(); it.next(); it.next(); // assumed u-l-l
                token = null;
                break;
            default:
                //c = it.previous();
                if (Character.isDigit(c) || c == '-') {
                    token = number(it,buf);
                }
        }
        // System.out.println("token: " + token); // enable this line to see the token stream
        return token;
    }
    
    private static final Object string(CharacterIterator it,StringBuilder buf) {
        buf.setLength(0);
        char c = it.next();
        while (c != '"') {
            if (c == '\\') {
            	c = it.next();
                if (c == 'u') {
                	buf.append(unicode(it));
                	c = it.next();
                } else {
                    Object value = escapes.get(new Character(c));
                    if (value != null) {
                    	buf.append(((Character) value).charValue());
                    	c = it.next();
                    }
                }
            } else {
            	 buf.append(c);
                 c = it.next();
            }
        }
        return buf.toString();
    }
    
    private static final Object array(CharacterIterator it,StringBuilder buf) {
    	ArrayList<Object> ret = new ArrayList<Object>();
    	it.next();
    	Object token = read(it,buf);
        while (token != ARRAY_END) {
            ret.add(token);
            it.next();
            token = read(it,buf);
            if (token == COMMA) {
            	it.next();
            	token = read(it,buf);
            }
        }
        return ret;
    }
    
    private static final Object object(CharacterIterator it,StringBuilder buf) {
    	HashMap<Object,Object> ret = new HashMap<Object,Object>();
    	it.next();
    	Object key = read(it,buf);
    	Object token = key;
        while (token != OBJECT_END) {
        	it.next();
        	token = read(it,buf); // should be a colon
            if (token != OBJECT_END) {
            	it.next();
            	token = read(it,buf);
                ret.put(key,token);
                it.next();
                token = read(it,buf); 
                if (token == COMMA) {
                	//读到,号
                	it.next();
                	token = read(it,buf);
                	key = token;
                }
            }
        }
        return ret;
    }
    

    private static final Object number(CharacterIterator it,StringBuilder buf) {
        int length = 0;
        boolean isFloatingPoint = false;
        buf.setLength(0);
        char c = it.current();
        if (c == '-') {
        	buf.append(c);
            c = it.next();
        }
        length += addDigits(it,c,buf);
        if (c == '.') {
        	buf.append(c);
            c = it.next();
            length += addDigits(it,c,buf);
            isFloatingPoint = true;
        }
        if (c == 'e' || c == 'E') {
        	buf.append(c);
            c = it.next();
            if (c == '+' || c == '-') {
            	buf.append(c);
                c = it.next();
            }
            addDigits(it,c,buf);
            isFloatingPoint = true;
        }
 
        String s = buf.toString();
        return isFloatingPoint 
            ? (length < 17) ? (Object)Double.valueOf(s) : new BigDecimal(s)
            : (length < 19) ? (Object)Long.valueOf(s) : new BigInteger(s);
    }
    
    private static final int addDigits(CharacterIterator it,char c,StringBuilder buf) {
        int ret;
        for (ret = 0; Character.isDigit(c); ++ret) {
        	buf.append(c);
            c = it.next();
        }
        it.previous();
        return ret;
    }
    
    public static void main(String[] args) {
    	
		String s = "{\"sub\" \n\r : 	 {\"sub11\":1111,\"list\":[\"ewwnn\",\"sdfasd在劫难逃\"],\"122\":{\"sub222\":\"sadfnn\",\"l2\":[\"ewwnn\",\"sdfasd在劫难逃\"]}},\"mmm\":\"val\"}";
		s = "{\"abc\":[{\"mm\":{}},{\"nn\":\"kk\"}],\"eee\":\"fff\"		\r\n }  ";
		//s = KIoc.readTxtInUTF8("f:/works/workspace_keel/KHunter/WebContent/WEB-INF/kobj.json");
		System.out.println(s);
		Object o = read(s);
		System.out.println(o);
		
		/*
		CharacterIterator it = new StringCharacterIterator(s);
		char c = it.first();
		System.out.print(c);
		while (c != it.DONE) {
			c = it.next();
			System.out.print(c);
		}*/
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		HashMap m = new HashMap();
//		HashMap subMap = new HashMap();
//		HashMap subMap2 = new HashMap();
//		ArrayList l = new ArrayList();
//		l.add("ewwnn");
//		l.add("sdfasd在劫难逃");
//		String[] sa = new String[]{"fsdfs","ssss","df sdf;sf "};
//		m.put("mmm", "val");
//		subMap.put("sub11", 1111);
//		subMap.put("s22", 22222);
//		subMap.put("list", l);
//		subMap.put("s22", subMap2);
//		m.put("sub", subMap);
//		subMap2.put("sub222", "sadfnn");
//		subMap2.put("l2", l);
//		System.out.println(JSON.write(m));
//		System.out.println("-------------");
//		System.out.println(JSON.writeFormat(m, 4));
//		
//	}

}
