package com.k99k.tools.android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * @author keel
 *
 */
public final class StringUtil {
	
	private StringUtil(){
		
	}
	
	/**
	 * 查找String中并关键词，并替换为指定的String
	 * @param sb 原StringBuilder
	 * @param keywords 
	 * @param replaceStr 用于替换的String
	 * @return
	 */
	public static final StringBuilder replaceKeyWords(StringBuilder sb,String[] keywords,String replaceStr){
		for (int i = 0; i < keywords.length; i++) {
			String s = keywords[i];
			for (int j = sb.indexOf(s); j > 0; j = sb.indexOf(s)) {
				sb.replace(j, j+s.length(), replaceStr);
			}
		}
		return sb;
	}
	
	/**
	 * 处理文本中的链接,使之加上a标签,使用正则实现
	 * @return
	 */
	public static final StringBuffer findUrl(StringBuilder sb){
		Pattern pattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:]+");
		Matcher matcher = pattern.matcher(sb);
		StringBuffer buffer = new StringBuffer();
		while(matcher.find()){   
//			for (int i = 0; i < matcher.groupCount(); i++) {
//				System.out.println("["+i+"]"+matcher.group(i));
//			}
			
			//System.out.println(matcher.group());
			String s = matcher.group(1);
			matcher.appendReplacement(buffer, "<a href=\""+s+"\">"+s+"</a>");           
		}
		matcher.appendTail(buffer);
		return buffer;
	}
	
	/**
	 * Object转为String,如果是null则转为""
	 * @param obj
	 * @return String (not null)
	 */
	public static final String objToStrNotNull(Object obj){
		String s = (obj == null)?"":obj.toString();
		return s;
	}
	
	/**
	 * Object转为String,如果是null则转为指定的defaultStr
	 * @param obj
	 * @param defaultStr
	 * @return
	 */
	public static final String toStrNotNull(Object obj,String defaultStr){
		String s = (obj == null)?defaultStr:obj.toString();
		return s;
	}
	
	/**
	 * Object转为非负整数int,若为null或其他,返回-1
	 * @param obj
	 * @return int
	 */
	public static final int objToNonNegativeInt(Object obj){
		if(obj != null){
			String s = obj.toString();
			if(s.matches("[0-9]*")){
				return Integer.parseInt(s);
			}
		}
		return -1;
	}
	
//	/**
//	 * 复制HashMap
//	 * @param map
//	 * @return hashMap with the same keys and values.
//	 */
//	@SuppressWarnings("unchecked")
//	public static final HashMap hashMapClone(HashMap map){
//		HashMap target = new HashMap();
//		target.putAll(map);
//        return target;
//	}
	
	/**
	 * 判断一个字符串是不是数字组成,可以为负数
	 * @param s 字符。
	 * @return
	 */
	public static final boolean isDigits(String s){
		if(s==null||s.length()==0)return false;
		int begin = (s.charAt(0) == '-')?1:0;
		for(int i=begin;i<s.length();i++){
			if(!Character.isDigit(s.charAt(i)))return false;
		}
		return true;
	}
	/**
	 * 判断一个Object的toString是不是数字组成,可以为负数
	 * @param o Object
	 * @return
	 */
	public static final boolean isDigits(Object o){
		if (o == null) {
			return false;
		}
		String s = o.toString();
		if(s.length()==0)return false;
		//认可负数
		int i = (s.charAt(0)=='-')?1:0;
		for(int j=s.length();i<j;i++){
			if(!Character.isDigit(s.charAt(i)))return false;
		}
		return true;
	}
	/**
	 * 判断String是否达到指定长度(>=len),(先经过trim处理)
	 * @param str
	 * @param len
	 * @return
	 */
	public static final boolean isStringWithLen(String str,int len){
		if (str == null) {
			return false;
		}
		if (str.trim().length() < len) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断String是否达到指定长度(>=len),(先经过trim处理)
	 * @param obj
	 * @param len
	 * @return
	 */
	public static final boolean isStringWithLen(Object obj,int len){
		if (obj == null) {
			return false;
		}
		if (obj.toString().trim().length() < len) {
			return false;
		}
		return true;
	}
	
	public static final int[] stringToIntArray(String arrString,String separator){
		try {
			String s = arrString.replaceAll("\\s", "");
			String[] sa = s.split(separator);
			int[] ia = new int[sa.length];
			for (int i = 0; i < sa.length; i++) {
				if (isDigits(sa[i])) {
					ia[i] = Integer.parseInt(sa[i]);
				}else{
					//出现非数字,直接返回
					return null;
				}
			}
			return ia;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * 得到一个当前时间的格式化日期
	 * @param format yyyyMMdd 或yyyyMM等
	 * @return
	 */
	public static final String getFormatDateString(String format){
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		java.util.Date d = new java.util.Date();
		return sdf.format(d);
	}
	/**
	 * 得到一个格式化日期
	 * @param format yyyyMMdd 或yyyyMM等
	 * @param java.util.Date
	 * @return
	 */
	public static final String getFormatDateString(String format,java.util.Date date){
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * 得到一个格式化日期
	 * @param format yyyyMMdd 或yyyyMM等
	 * @param long
	 * @return
	 */
	public static final String getFormatDateString(String format,long date){
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		return sdf.format(new java.util.Date(date));
	}
	/**
	 * 把字符串中的HTML代码转换成页面显示的代码,(" " 换成"&amp;nbsp;"，\n换成&lt;br /&gt;，  < 换成 &amp;lt; ，> 换成 &amp;gt;， " 换成 &amp;quot;)
	 * @param 字符串 str
	 * @return String 替换后的字符传
	 */
	public static final String repstr(String str)	{
		if(str==null)return "";
		str=str.replaceAll(" ", "&nbsp;");
		str=str.replaceAll("<", "&lt;");
		str=str.replaceAll(">", "&gt;");
		str=str.replaceAll("\"", "&quot;");
		str=str.replaceAll("\n", "<br />");
		return str;
	}
	
	/**
	 * 把页面的HTML代码转换成页面显示的代码,(< 换成 &amp;lt; ，> 换成 &amp;gt;， " 换成 &amp;quot;)
	 * @param String 替换前的字符串
	 * @return String 替换后的字符串
	 */
	public static final String repstr1(String str){
		if(str==null)return "";
		str=str.replaceAll("<", "&lt;");
		str=str.replaceAll(">", "&gt;");
		str=str.replaceAll("\"", "&quot;");
		return str;
	}
	
	/**
	 * 把页面显示的代码替换成HTML代码,repstr1方法的反向方法
	 * @param String 替换前的字符串
	 * @return String 替换后的字符串
	 */
	public static final String repstr2(String str){
		if(str==null)return "";
		str=str.replaceAll("&lt;", "<");
		str=str.replaceAll("&gt;", ">");
		str=str.replaceAll("&quot;", "\"");
		return str;
	}
	
	/**
	 * 把字符串里的换行、尖括号、双引号去掉，用于在HTML头的keywords里显示
	 * @param str
	 * @return
	 */
	public static final String clearHtmlTagAndBlank(String str){
		if(str==null)return "";
		str=str.replaceAll("<", "");
		str=str.replaceAll(">", "");
		str=str.replaceAll("\"", "");
		str=str.replaceAll("\\s", "");
		return str;
	}
	
	/**
	 * 去掉<>和"，屏蔽HTML
	 * @param str
	 * @return
	 */
	public static final String clearHTML(String str){
		if(str==null)return "";
		str=str.replaceAll("<", "&lt;");
		str=str.replaceAll(">", "&gt;");
		str=str.replaceAll("\"", "&quot;");
		return str;
	}
	
	public static void main(String[] args) {
		String s = "23,234234,2342,21341234,22";
		int[] ia = stringToIntArray("23,32,342,3,,",",");
		if (ia==null) {
			System.out.println("null");
		}else{
	
			for (int i = 0; i < ia.length; i++) {
				System.out.println(ia[i]);
			}
		}
		
		System.out.println(s.matches("(\\d+,)*(\\d+)"));
	}
}
