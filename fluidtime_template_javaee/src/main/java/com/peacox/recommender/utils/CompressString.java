package com.peacox.recommender.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class CompressString {
	
	public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        System.out.println("String length : " + str.length());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new GZIPOutputStream(out), "UTF-8"));
        writer.write(str);
//        GZIPOutputStream gzip = new GZIPOutputStream(out);
//        gzip.write(str.getBytes("UTF-8"));
//        gzip.close();
        writer.close();
        String outStr = new String(Base64.encodeBase64(out.toByteArray()));
        System.out.println("Output String length : " + outStr.length());
        return outStr;
     }
    
    public static String decompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        System.out.println("Input String length : " + str.length());
        byte[] compressedData = Base64.decodeBase64(str.getBytes());
        BufferedInputStream reader = new BufferedInputStream(
                new GZIPInputStream(new ByteArrayInputStream(compressedData)));
//        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str.getBytes("ISO-8859-1")));
//        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
//        String outStr = "";
//        String line;
//        while ((line=bf.readLine())!=null) {
//          outStr += line;
//        }
        String outStr = IOUtils.toString(reader, "UTF-8");
        System.out.println("Output String lenght : " + outStr.length());
        return outStr;
     }
  

}
