package com.xinchuang.common.utils;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.StringUtils;

public class OoXmlStrictConverter {


        public static void main(String[] args) {
        XOF.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        try {
            Properties mappings = readMappings();
            InputStream is = new BufferedInputStream(new FileInputStream(new File("E:\\elasticsearch以及ocr\\20221021解析失败文件\\防护盾代理服务器设置20190411-更新.docx")));
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("E:\\elasticsearch以及ocr\\20221021解析失败文件\\防护盾代理服务器设置20190411-更新.docx")));
		    OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("E:\\elasticsearch以及ocr\\20221020上午版本未解析office文件\\test.docx")));
            //FileInputStream fileInputStream = new FileInputStream("E:\\elasticsearch以及ocr\\20221021解析失败文件\\防护盾代理服务器设置20190411-更新.docx");

            transform(is, os, mappings);
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }


    private static final XMLEventFactory XEF = XMLEventFactory.newInstance();
    private static final XMLInputFactory XIF = XMLInputFactory.newInstance();
    private static final XMLOutputFactory XOF = XMLOutputFactory.newInstance();

    public static void transform(InputStream inFile, OutputStream outFile, Properties mappings) throws Exception {
        System.out.println("transforming " + inFile + " to " + outFile);
        try(ZipInputStream zis = new ZipInputStream(inFile);
            ZipOutputStream zos = new ZipOutputStream(outFile);) {
            ZipEntry ze;
            while((ze = zis.getNextEntry()) != null) {
                ZipEntry newZipEntry = new ZipEntry(ze.getName());
                zos.putNextEntry(newZipEntry);
                FilterInputStream filterIs = new FilterInputStream(zis) {
                    @Override
                    public void close() throws IOException {
                    }
                };
                FilterOutputStream filterOs = new FilterOutputStream(zos) {
                    @Override
                    public void close() throws IOException {
                    }
                };
                if(isXml(ze.getName())) {
                    try {
                        XMLEventReader xer = XIF.createXMLEventReader(filterIs);
                        XMLEventWriter xew = XOF.createXMLEventWriter(filterOs);
                        int depth = 0;
                        while(xer.hasNext()) {
                            XMLEvent xe = xer.nextEvent();
                            if(xe.isStartElement()) {
                                StartElement se = xe.asStartElement();
                                xe = XEF.createStartElement(updateQName(se.getName(), mappings),
                                        processAttributes(se.getAttributes(), mappings, se.getName().getNamespaceURI(), (depth == 0)),
                                        processNamespaces(se.getNamespaces(), mappings));
                                depth++;
                            } else if(xe.isEndElement()) {
                                EndElement ee = xe.asEndElement();
                                xe = XEF.createEndElement(updateQName(ee.getName(), mappings),
                                        processNamespaces(ee.getNamespaces(), mappings));
                                depth--;
                            }
                            xew.add(xe);
                        }
                        xer.close();
                        xew.close();
                    } catch(Throwable t) {
                        throw new IOException("Problem paraing " + ze.getName(), t);
                    }
                } else {
                    copy(filterIs, filterOs);
                }
                zis.closeEntry();
                zos.closeEntry();
            }
        }
    }

    private static boolean isXml(final String fileName) {
        if(StringUtils.hasText(fileName)) {
            int pos = fileName.lastIndexOf(".");
            if(pos != -1) {
                String ext = fileName.substring(pos + 1).toLowerCase();
                return ext.equals("xml") || ext.equals("vml") || ext.equals("rels");
            }
        }
        return false;
    }

    private static final QName CONFORMANCE = new QName("conformance");

    private static Iterator<Attribute> processAttributes(final Iterator<Attribute> iter,
                                                         final Properties mappings, final String elementNamespaceUri, final boolean rootElement) {
        ArrayList<Attribute> list = new ArrayList<>();
        while(iter.hasNext()) {
            Attribute att = iter.next();
            QName qn = updateQName(att.getName(), mappings);
            if(rootElement && mappings.containsKey(elementNamespaceUri) && att.getName().equals(CONFORMANCE)) {
                //drop attribute
            } else {
                String value = att.getValue();
                String newValue = mappings.getProperty(value);
                list.add(XEF.createAttribute(qn, StringUtils.hasText(newValue)?newValue:value));
            }
        }
        return Collections.unmodifiableList(list).iterator();
    }

    private static Iterator<Namespace> processNamespaces(final Iterator<Namespace> iter,
                                                         final Properties mappings) {
        ArrayList<Namespace> list = new ArrayList<>();
        while(iter.hasNext()) {
            Namespace ns = iter.next();
            String namespaceUri = ns.getNamespaceURI();
            if(StringUtils.hasText(namespaceUri)) {
                String mappedUri = mappings.getProperty(namespaceUri);
                if(mappedUri != null) {
                    ns = StringUtils.hasText(ns.getPrefix()) ? XEF.createNamespace(ns.getPrefix(), mappedUri)
                            : XEF.createNamespace(mappedUri);
                }
            }
            list.add(ns);
        }
        return Collections.unmodifiableList(list).iterator();
    }

    private static QName updateQName(QName qn, Properties mappings) {
        String namespaceUri = qn.getNamespaceURI();
        if(StringUtils.hasText(namespaceUri)) {
            String mappedUri = mappings.getProperty(namespaceUri);
            if(mappedUri != null) {
                qn = StringUtils.hasText(qn.getPrefix()) ? new QName(mappedUri, qn.getLocalPart(), qn.getPrefix())
                        : new QName(mappedUri, qn.getLocalPart());
            }
        }
        return qn;
    }

    public static Properties readMappings() throws IOException {
        Properties props = new Properties();
        try(InputStream is = OoXmlStrictConverter.class.getResourceAsStream("/ooxml-strict-mappings.properties");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] vals = line.split("=");
                if(vals.length >= 2) {
                    props.setProperty(vals[0], vals[1]);
                } else if(vals.length == 1) {
                    props.setProperty(vals[0], "");
                }

            }
        }
        return props;
    }

    private static void copy(InputStream inp, OutputStream out) throws IOException {
        byte[] buff = new byte[4096];
        int count;
        while ((count = inp.read(buff)) != -1) {
            if (count > 0) {
                out.write(buff, 0, count);
            }
        }
    }
}


