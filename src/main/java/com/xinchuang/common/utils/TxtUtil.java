package com.xinchuang.common.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.core.io.BOMInputStream;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TxtUtil {

	//private static log log = logFactory.getlog(TxtUtil.class);
	/**
	 * 功能：Java读取txt文件的内容 步骤：1：先获得文件句柄 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
	 * 3：读取到输入流后，需要读取生成字节流 4：一行一行的输出。readline()。 备注：需要考虑的是异常情况
	 * 
	 * @param filePath
	 */
	public static String readTxtFile(String filePath) {
		try {
			String encoding = "UTF-8";
			StringBuffer str = new StringBuffer();
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				
			   FileInputStream fis = new FileInputStream(file);
			   //可检测多种类型，并剔除bom
			   BOMInputStream bomIn = new BOMInputStream(fis);
			   
			   //若检测到bom，则使用bom对应的编码			   
			   String _encoding = bomIn.getCharset();
			   InputStreamReader read = null;
			   if(_encoding.equals(encoding)) {
				   read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式 
			   }else{
				   read = new InputStreamReader(bomIn, _encoding);				
			   }
//				InputStreamReader read = new InputStreamReader(
//						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					str.append(lineTxt).append("\r\n");
				}
				read.close();
			} else {
				log.info("找不到指定的文件");
			}
			return str.toString();
		} catch (Exception e) {
			log.info("出错",e);
		}
		return "";
	}

	
	public static List<String> readTxtFile2List(String filePath) {
		try {
			List<String> list = Lists.newArrayList();
			String encoding = "UTF-8";
			//StringBuffer str = new StringBuffer();
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					//str.append(lineTxt).append("\r\n");
					list.add(lineTxt);
				}
				read.close();
			} else {
				log.info("找不到指定的文件");
			}
			return list;
		} catch (Exception e) {
			log.info("读取文件内容出错");
			e.printStackTrace();
		}
		return null;
	}
	public static boolean writeTxtFile(File fileName,String content)
			throws Exception {
		RandomAccessFile mm = null;
		boolean flag = false;
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(fileName);
			o.write(content.getBytes("utf-8"));
			o.close();
			// mm=new RandomAccessFile(fileName,"rw");
			// mm.writeBytes(content);
			flag = true;
		} catch (Exception e) {			
			log.info("出错",e);
		} finally {
			if (mm != null) {
				mm.close();
			}
		}
		
		return flag;
	}

	public static void contentToTxt(String filePath, String content) {
		String str = new String(); // 原有txt内容
		String s1 = new String();// 内容更新
		try {
			File f = new File(filePath);
			if (f.exists()) {
				//System.out.print("文件存在");
			} else {
				System.out.print("文件不存在");
				f.createNewFile();// 不存在则创建
			}
			BufferedReader input = new BufferedReader(new FileReader(f));

			while ((str = input.readLine()) != null) {
				s1 += str + "\r\n";
			}
			//log.info(s1);
			input.close();
			s1 += content;

			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			output.write(s1);
			output.close();
		} catch (Exception e) {
			log.info("出错",e);
		}
	}

	public static void appendFile(String filePath, String content) {
		try {
			File f = new File(filePath);
			if (f.exists()) {
				//System.out.print("文件存在");
			} else {
				System.out.print("文件不存在");
				f.createNewFile();// 不存在则创建
			}
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(filePath, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content.getBytes("UTF-8")+"\r\n");
			randomFile.close();
		} catch (Exception e) {			
			log.info("读取文件内容出错",e);
		}
	}
	
	public static String txt2String(File file){
        String content = "";
    	try{
    		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8"); // 建立一个输入流对象reader  
    		BufferedReader br = new BufferedReader(reader);//构造一个BufferedReader类来读取文件
    		String line = "";  
            line = br.readLine();
            while (line != null) {
            	content = content + line + "\r";
                line = br.readLine(); // 一次读入一行数据  
            }
            br.close();
    	}catch(Exception e){
    		log.info("出错",e);
        }
    	return content;
    }
	

/*	public static void main(String argv[]) {
		String filePath = "L:\\Apache\\htdocs\\res\\20121012.txt";
		// "res/";
		//readTxtFile(filePath);
		
		appendFile("c://test.txt","test");
		appendFile("c://test.txt","test2");
		appendFile("c://test.txt","test3");
//		File file = new File("c://test.txt");
//		try {
//			writeTxtFile(file,"test4");
//			writeTxtFile(file,"test5");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}*/
	
	
	//以2个数为一组排列组合 全都列出来
	private static int count = 0;
	private static char[] charArray = new char[] { '，', ',', '.','。','？','?','！',
			'!','：',
			':','；',';','、','—','?','·','「','『','“','‘','《','》','<','>','」','』','”','’','…','〔','〕','【','】','[',']' };

	public static void main(String[] args) {
		int length = 2;// 指定长度为4
		char[] comb = new char[length];
		//读取文本中的值 存入char中
		/*String filePath = "D:\\标点.txt";
		String a=readTxtFile(filePath);
		char[] b=a.toCharArray();
		for (int i = 0; i < b.length; i++) {
			System.out.println(b[i]);
		}
		System.out.println(a);*/
		showComb(charArray, comb, 0, length);

	}

	public static void showComb(char[] charArray, char[] comb, int i, int length) {

		for (int j = 0; j < charArray.length; j++) {
			comb[i] = charArray[j];
			if (i < length - 1) {
				showComb(charArray, comb, i + 1, length); // 继续递归
			} else {
				String str = charArrayToString(comb);
				if (!str.startsWith("0"))
					//System.out.println("==当前值=" + str + "===总数==" + ++count);
					System.out.println(str.replaceAll("…", "……").replaceAll("—", "——"));
			}
		}
	}

	public static String charArrayToString(char[] charArray) {
		StringBuffer sb = new StringBuffer();
		for (char ch : charArray) {
			sb.append(ch);
		}
		return sb.toString();
	}
	
	
}
