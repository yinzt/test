package com.xinchuang.common.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYinUtil {

	/**
	 * 获取汉字串拼音，英文字符不变
	 * @param chinese 汉字串
	 * @return 汉语拼音
	 */
	public static String getFullSpell(String chinese) {
		StringBuffer pybf = new StringBuffer();
		char[] arr = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		if(arr.length>0) {
			if(arr[0] >128) {
				try {
					String[] tmp = PinyinHelper.toHanyuPinyinStringArray(arr[0], defaultFormat);
					if(tmp != null && tmp.length > 0){
						pybf.append(tmp[0]);
					}
					//pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[0], defaultFormat)[0]);
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}else {
				pybf.append(arr[0]);
			}
		}
		if(pybf.toString().length()>0) {
			return pybf.toString().substring(0, 1);
		}else {
			return pybf.toString();
		}
		
	}
	public static void main(String[] args) {
		String fileName = "《国网安徽电力专有云资源申请表》模板-20200506-V2.docx";
		String a=getFullSpell("《国网安徽电力专有云资源申请表模板-20200506-V2.docx");
		System.out.println(a);
		
	}
}
