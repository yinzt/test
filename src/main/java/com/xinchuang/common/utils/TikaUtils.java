package com.xinchuang.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mail.RFC822Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.springframework.util.StringUtils;
import org.xml.sax.ContentHandler;

import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
/**
 * @author d'm'l 文件提取
 *
 */
@Slf4j
public class TikaUtils {

//	public static void main(String[] args) throws IOException, TikaException {
//		String excelPath = "D:\\cache\\00数据安全防护盾测试用例初稿v1.5-190317 (1).xlsx";
//		String excelContent = parseExcel(excelPath);
//		System.out.println(excelContent);
//	}









	/**
	 * 图片 文字提取
	 *
	 * @param filePath
	 * @return
	 */
	public static String parsePicture(String filePath,String ocrUrl) {
		try {
			//String imgtype = filePath.trim().substring(filePath.lastIndexOf(".") + 1);
			//String paramdata = OcrUtils.GetBase64StrFromImage(filePath, "", null);
			//String result = OcrUtils.requestOcrServer(imgtype, paramdata,ocrUrl);
			//String result = OcrUtils.requestOcrServerV2(imgtype, paramdata,ocrUrl);
			return OcrUtils.heHeOcr(filePath,ocrUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * txt文本提取
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getContext(String filePath) {
		try {
			File file = new File(filePath);
			BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
			Metadata metadata = new Metadata();
			FileInputStream fileInputStream = new FileInputStream(file);
			ParseContext parseContext = new ParseContext();

			TXTParser txtParser = new TXTParser();
			txtParser.parse(fileInputStream, handler, metadata, parseContext);

			return handler.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
     /**
	 * pdf解析
	 * 
	 * @param filePath
	 * @return
	 */
	public static String parsePdf(String filePath){
		return parsePdf(filePath,null);
	}
	/**
	 * pdf解析
	 * 
	 * @param filePath
	 * @param ocrUrl
	 * @return
	 */
	public static String parsePdf(String filePath,String ocrUrl) {
		try {
			if(StringUtils.isEmpty(ocrUrl)){
				BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
				Metadata metadata = new Metadata();

				FileInputStream inputstream = new FileInputStream(new File(filePath));
				ParseContext pcontext = new ParseContext();
				PDFParser pdfparser = new PDFParser();
				pdfparser.parse(inputstream, handler, metadata, pcontext);
				log.info("filePath:{},tika解析pdf文件结果:{}",filePath,handler.toString());
				// System.out.println("Contents of the PDF :" + handler.toString());
				return handler.toString();
			}
			return OcrUtils.heHeOcr(filePath, ocrUrl);
			// 元数据提取
//			System.out.println("Metadata of the PDF:");
//			String[] metadataNames = metadata.names();
//			for (String name : metadataNames) {
//				System.out.println(name + " : " + metadata.get(name));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * excel文本内容提取
	 * 
	 * @param filePath
	 * @return
	 */
	public static String parseExcel(String filePath) {
		try {
			String parseToString = "";
			List<String> cx = new ArrayList<String>();
			ArrayList<Object> resultData = new ArrayList<>();
			FileInputStream fis = new FileInputStream(filePath);
			Workbook workbook = null;
			// 判断excel的两种格式xls,xlsx
			if (filePath.toLowerCase().endsWith("xlsx")) {
				workbook = new XSSFWorkbook(fis);
			} else if (filePath.toLowerCase().endsWith("xls")) {
				workbook = new HSSFWorkbook(fis);
			}
			int sheetCount = workbook.getNumberOfSheets();
			for (int i = 0; i < sheetCount; i++) {
				// 得到第1个sheet
				Sheet sheet = workbook.getSheetAt(i);
				// 得到行的迭代器
				Iterator<Row> rowIterator = sheet.iterator();
				// 循环每一行
				while (rowIterator.hasNext()) {
					// 得到一行对象
					Row row = rowIterator.next();
					// 跳过第一行标题
					if (row.getRowNum() == 0) {
						continue;
					}
					ArrayList<Object> cellData = new ArrayList<>();
					// 循环每一列
					for (int j = 0; j < row.getLastCellNum(); j++) {
						// 得到单元格对象
						Cell cell = row.getCell(j);
						if (cell == null) {
							continue;
						}
						// 检查数据类型
						switch (cell.getCellTypeEnum()) {
						case STRING:
							cellData.add(cell.getStringCellValue());
							break;
						case BLANK:
							cellData.add("");
							break;
						case NUMERIC:
							if (HSSFDateUtil.isCellDateFormatted(cell)) // 日期判断
							{
								SimpleDateFormat sdf = null;
								sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date date = cell.getDateCellValue();
								cellData.add(sdf.format(date));
							} else { // 不是日期则为数字
								cellData.add(cell.getNumericCellValue());
							}
							break;
						}
					}
					resultData.add(cellData);
				}
			}
			fis.close();
			parseToString = resultData.toString();

			return parseToString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 解析word文档
	 * 
	 * @param filePath
	 * @return
	 */
	public static String parseWord(InputStream buffer,String mine) 
	{
		String  wordStr=null;
		if(buffer==null||mine==null)return null;
		try {
			//Pattern re=new Pattern.compile("doc$",Pattern.CASE_INSENSITIVE);
			if(mine!=null&&Pattern.matches("doc$", mine.toLowerCase())){
				WordExtractor ex = new WordExtractor(buffer);
				wordStr = ex.getText();
				
			}else 
			{
				ZipSecureFile.setMinInflateRatio(-1.0d);
				XWPFDocument xdoc = new XWPFDocument(new FileInputStream(mine));
				XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
				wordStr = extractor.getText();

			}	
		} catch (Exception e) 
		{
				e.printStackTrace();

			HWPFDocument hwpfDocument = null;
			try {
				hwpfDocument = new HWPFDocument(new FileInputStream(mine));
				WordExtractor wordExtractor = new WordExtractor(hwpfDocument);
				// 文档文本内容
				wordStr = wordExtractor.getText();
			} catch (Exception ex) {
				log.error("HWPFDocument parseWord method error,mine:{}",mine,ex);
			}
		}
		if(StringUtils.isEmpty(wordStr)){
			try{
				Parser parser = new OfficeParser();
				//BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
				InputStream is = new BufferedInputStream(new FileInputStream(mine));
				Metadata meta = new Metadata();
				meta.add(Metadata.CONTENT_ENCODING, "utf-8");
				ContentHandler iHandler = new BodyContentHandler(1000 * 1024 * 1024);
				parser.parse(is, iHandler, meta, new ParseContext());
				return iHandler.toString();
			} catch (Exception ex){
				log.error("AutoDetectParser parseWord method error,mine:{}",mine,ex);
				return wordStr;
			}
		}
		return wordStr;	
	}



	public static String readWord(String path) throws Exception {

//    WordExtractor extractor = new WordExtractor(is);

		String content = null;
		File file = new File(path);
		if (file.exists() && file.isFile()) {
			InputStream is = null;
			XWPFDocument xwpfDocument = null;
			POIXMLTextExtractor extractor = null;
			HWPFDocument hwpfDocument = null;
			WordExtractor wordExtractor = null;
			try {
//				is = new FileInputStream(file);
//				hwpfDocument = new HWPFDocument(is);
//				wordExtractor = new WordExtractor(hwpfDocument);
//				// 文档文本内容
//				content = wordExtractor.getText();

				is = new FileInputStream(file);
				xwpfDocument = new XWPFDocument(is);
				extractor = new XWPFWordExtractor(xwpfDocument);
				// 文档文本内容
				content = extractor.getText();

//          // 文档图片内容
//          List<XWPFPictureData> pictures = docx.getAllPictures();
//          for (XWPFPictureData picture : pictures) {
//            byte[] bytev = picture.getData();
//            // 输出图片到磁盘
//            FileOutputStream out = new FileOutputStream(
//                "D:\\temp\\temp\\" + UUID.randomUUID() + picture.getFileName());
//            out.write(bytev);
//            out.close();
//          }

			} catch (Exception e) {//较低版本的word文件
				e.printStackTrace();
				is = new FileInputStream(file);
				hwpfDocument = new HWPFDocument(is);
				wordExtractor = new WordExtractor(hwpfDocument);
				// 文档文本内容
				content = wordExtractor.getText();
			} finally {
				try {
					if (extractor != null) {
						extractor.close();
					}
					if (xwpfDocument != null) {
						xwpfDocument.close();
					}
					if (wordExtractor != null) {
						wordExtractor.close();
					}
					if (hwpfDocument != null) {
						hwpfDocument.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (IOException e) {
				}
			}
		}
		return content;
	}


	public static void main(String[] args) throws Exception {

		String text = "目前支持的在线预览的格式主要包括：word、ppt、excel、txt、pdf、mp3、mp5、rmvb等主流文件格式。但暂不支持预览，压缩/执行格式的文件。";
		System.out.println(text.length());
		//application/vnd.openxmlformats-officedocument.wordprocessingml.document
		//application/vnd.openxmlformats-officedocument.wordprocessingml.document
		String fileName = "E:\\elasticsearch以及ocr\\20221020上午版本未解析office文件\\电子文档安全管理系统-系统管理员使用手册.docx";
		String fileName2 = "E:\\elasticsearch以及ocr\\20221019解析失败文件\\112-关注名单回溯接口-需求文档v1.1.docx";
		String fileName3 = "E:\\elasticsearch以及ocr\\20221021解析失败文件\\防护盾代理服务器设置20190411-更新.docx";
		Tika tika = new Tika();
		File file = new File(fileName);
		System.out.println(tika.detect(file));
//
//
//
//		Long start = System.currentTimeMillis();
//		Parser parser = new AutoDetectParser();
//		//BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
//		InputStream is = new BufferedInputStream(new FileInputStream(new File("E:\\elasticsearch以及ocr\\20221020上午版本未解析office文件\\J190486.doc")));
//		OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("E:\\elasticsearch以及ocr\\20221020上午版本未解析office文件\\result.txt")));
//		Metadata meta = new Metadata();
//		meta.add(Metadata.CONTENT_ENCODING, "utf-8");
//		ContentHandler iHandler = new BodyContentHandler(1000 * 1024 * 1024);
//		parser.parse(is, iHandler, meta, new ParseContext());
//
//		Long end = System.currentTimeMillis();
//		Long used = (end-start)/1000;
//		System.out.println("耗时："+used+"秒");
//		System.out.println(iHandler.toString());
//
//		File file = new File(fileName);
//		Tika tika = new Tika();
//		System.out.println(tika.detect(file));
//
//
//		FileNameMap fileNameMap = URLConnection.getFileNameMap();
//		String mimeType = fileNameMap.getContentTypeFor(file.getName());
//		System.out.println(mimeType);

		//System.out.println(Pattern.matches("doc", fileName.toLowerCase()));
		System.out.println(parseWord(fileName3));
	}
	public static String parseWord(String filePath) {
		 
		InputStream buffer=null;
		try {
		 buffer = new FileInputStream(new File(filePath));
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		return parseWord(buffer,filePath);
	}
	public static String _parseWord(String filePath) {
		String wordStr = "";
		try {
			if (filePath.endsWith("doc") || filePath.endsWith("DOC")) {
				InputStream is = new FileInputStream(new File(filePath));
				WordExtractor ex = new WordExtractor(is);
				wordStr = ex.getText();
				is.close();
			} else {
				File file = new File(filePath);
				FileInputStream fis = new FileInputStream(file);
				XWPFDocument xdoc = new XWPFDocument(fis);
				XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
				wordStr = extractor.getText();
				fis.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordStr;
	}

	/**
	 * 
	 * @param filePath
	 * @param
	 * @return
	 * @throws IOException
	 */
	public static String getPPTContent(String filePath) throws IOException {
		Tika tika = new Tika();
		// 获取格式
		File file = new File(filePath);
		String detect = tika.detect(file);
		String pptContent = "";
		if ("application/vnd.ms-powerpoint".equals(detect)) {
			pptContent = getTextFromPPT(filePath);
		} else if ("application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(detect)) {
			pptContent = getTextFromPPTX(filePath);
		}
		return pptContent;
	}

	/**
	 * 用来读取ppt文件
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String getTextFromPPT(String filePath) throws IOException {
		FileInputStream in = new FileInputStream(filePath);
		PowerPointExtractor extractor = new PowerPointExtractor(in);
		String content = extractor.getText();
		extractor.close();
		return content;
	}

	/**
	 * 用来读取pptx文件
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String getTextFromPPTX(String filePath) throws IOException {
		String resultString = null;
		StringBuilder sb = new StringBuilder();
		FileInputStream in = new FileInputStream(filePath);
		try {
			XMLSlideShow xmlSlideShow = new XMLSlideShow(in);
			List<XSLFSlide> slides = xmlSlideShow.getSlides();
			for (XSLFSlide slide : slides) {
				CTSlide rawSlide = slide.getXmlObject();
				CTGroupShape gs = rawSlide.getCSld().getSpTree();
				CTShape[] shapes = gs.getSpArray();
				for (CTShape shape : shapes) {
					CTTextBody tb = shape.getTxBody();
					if (null == tb) {
						continue;
					}
					CTTextParagraph[] paras = tb.getPArray();
					for (CTTextParagraph textParagraph : paras) {
						CTRegularTextRun[] textRuns = textParagraph.getRArray();
						for (CTRegularTextRun textRun : textRuns) {
							sb.append(textRun.getT());
						}
					}
				}
			}
			resultString = sb.toString();
			xmlSlideShow.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	}
}
