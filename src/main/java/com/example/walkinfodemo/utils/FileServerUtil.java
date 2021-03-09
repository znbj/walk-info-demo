package com.example.walkinfodemo.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传下载工具类
 * Created by wangfan on 2018-12-14 08:38
 */
public class FileServerUtil {
    // 需要设置输出编码的content-type
    private final static List<String> SET_CHARSET_CONTENT_TYPES = Arrays.asList("application/json", "application/javascript");

    /**
     * 上传文件
     *
     * @param file      MultipartFile
     * @param directory 文件保存的目录
     * @param uuidName  是否用uuid命名
     * @return File
     */
    public static File upload(MultipartFile file, String directory, boolean uuidName) throws IOException, IllegalStateException {
        File outFile = getUploadFile(file.getOriginalFilename(), directory, uuidName);
        if (!outFile.getParentFile().exists()) {
            if (!outFile.getParentFile().mkdirs()) throw new RuntimeException("make directory fail");
        }
        file.transferTo(outFile);
        return outFile;
    }

    /**
     * 上传文件base64格式
     *
     * @param base64    base64编码字符
     * @param fileName  文件名称, 为空使用uuid命名
     * @param directory 文件保存的目录
     * @return File
     */
    public static File upload(String base64, String fileName, String directory) throws FileNotFoundException, IORuntimeException {
        if (base64 == null || base64.trim().isEmpty()) throw new RuntimeException("base64 is empty");
        String suffix = base64.substring(11, base64.indexOf(";"));  // 获取文件格式
        File outFile = getUploadFile(suffix, directory, fileName == null || fileName.trim().isEmpty());
        byte[] bytes = Base64.getDecoder().decode(base64.substring(base64.indexOf(";") + 8).getBytes());
        IoUtil.write(new FileOutputStream(outFile), true, bytes);
        return outFile;
    }

    /**
     * 获取上传文件位置
     *
     * @param fileName  文件名称
     * @param directory 上传目录
     * @param uuidName  是否使用uuid命名
     * @return File
     */
    public static File getUploadFile(String fileName, String directory, boolean uuidName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd/");
        String suffix, dir = sdf.format(new Date());  // 使用日期作为上传子目录
        if (fileName == null) {
            uuidName = true;
            suffix = "";
        } else {
            suffix = fileName.substring(fileName.lastIndexOf("."));  // 获取文件后缀
        }
        File file;
        if (uuidName) {  // uuid命名
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            file = new File(directory, dir + uuid + suffix);
        } else {  // 使用原名称，存在相同则加(1)
            file = new File(directory, dir + fileName);
            String prefix = fileName.substring(0, fileName.lastIndexOf("."));  // 获取文件名称
            int sameSize = 2;
            while (file.exists()) {
                file = new File(directory, dir + prefix + "(" + sameSize + ")" + suffix);
                sameSize++;
            }
        }
        return file;
    }

    /**
     * 预览文件, 支持断点续传
     *
     * @param file     文件
     * @param response HttpServletResponse
     * @param request  HttpServletRequest
     */
    public static void preview(File file, String pdfDir, String officeHome, HttpServletResponse response, HttpServletRequest request) {
        preview(file, false, pdfDir, officeHome, response, request);
    }

    /**
     * 预览文件, 支持断点续传
     *
     * @param file          文件
     * @param forceDownload 是否强制下载
     * @param response      HttpServletResponse
     * @param request       HttpServletRequest
     */
    public static void preview(File file, boolean forceDownload, String pdfDir, String officeHome, HttpServletResponse response, HttpServletRequest request) {
        cross(response);
        if (file == null || !file.exists()) {
            outNotFund(response);
            return;
        }
        if (forceDownload) {
            setDownloadHeader(response, file.getName());
        } else {
            if (OpenOfficeUtil.canConverter(file.getName())) {  // 支持word、excel等预览
                File pdfFile = OpenOfficeUtil.converterToPDF(file.getAbsolutePath(), pdfDir, officeHome);
                if (pdfFile != null) file = pdfFile;
            }
            String contentType = getContentType(file);  // 获取文件类型
            if (contentType != null) {
                // 设置编码类型
                if (contentType.startsWith("text/") || SET_CHARSET_CONTENT_TYPES.contains(contentType)) {
                    try {
                        String charset = JChardetFacadeUtil.detectCodepage(file.toURI().toURL());
                        if (charset != null) response.setCharacterEncoding(charset);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                response.setContentType(contentType);
                //response.setHeader("Cache-Control", "max-age=3600");
                response.setHeader("Cache-Control", "public");
            } else {
                setDownloadHeader(response, file.getName());
            }
        }
        output(file, response, request);
    }

    /**
     * 预览缩略图
     *
     * @param file      原文件
     * @param thumbnail 缩略图文件
     * @param size      缩略图文件的最大值(kb)
     * @param response  HttpServletResponse
     * @param request   HttpServletRequest
     */
    public static void previewThumbnail(File file, File thumbnail, Integer size, String officeHome, HttpServletResponse response, HttpServletRequest request) {
        if (!thumbnail.exists() && isImage(file)) {  // 如果是图片并且缩略图不存在则生成
            long fileSize = file.length();
            if ((fileSize / 1024) > size) {  // 大于60kb生成60kb的缩略图
                try {
                    if (thumbnail.getParentFile().mkdirs()) {
                        ImgUtil.scale(file, thumbnail, 100f / (fileSize / 1024f));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                preview(file, null, officeHome, response, request);
                return;
            }
        }
        preview(thumbnail, null, officeHome, response, request);
    }

    /**
     * 输出文件流, 支持断点续传
     *
     * @param file     文件
     * @param response HttpServletResponse
     * @param request  HttpServletRequest
     */
    public static void output(File file, HttpServletResponse response, HttpServletRequest request) {
        long length = file.length();  // 文件总大小
        long start = 0, to = length - 1;  // 开始读取位置, 结束读取位置
        long lastModified = file.lastModified();  // 文件修改时间
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", "\"" + length + "-" + lastModified + "\"");
        response.setHeader("Last-Modified", new Date(lastModified).toString());
        String range = request.getHeader("Range");
        if (range != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            String[] ranges = range.replace("bytes=", "").split("-");
            start = Long.parseLong(ranges[0].trim());
            if (ranges.length > 1) to = Long.parseLong(ranges[1].trim());
            response.addHeader("Content-Range", "bytes " + start + "-" + to + "/" + length);
        }
        response.setHeader("Content-Length", String.valueOf(to - start + 1));
        try {
            output(file, response.getOutputStream(), 2048, start, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出文件流
     *
     * @param file 文件
     * @param os   输出流
     */
    public static void output(File file, OutputStream os) {
        output(file, os, null);
    }

    /**
     * 输出文件流
     *
     * @param file 文件
     * @param os   输出流
     * @param size 读取缓冲区大小
     */
    public static void output(File file, OutputStream os, Integer size) {
        output(file, os, size, null, null);
    }

    /**
     * 输出文件流, 支持分片
     *
     * @param file  文件
     * @param os    输出流
     * @param size  读取缓冲区大小
     * @param start 开始位置
     * @param to    结束位置
     */
    public static void output(File file, OutputStream os, Integer size, Long start, Long to) {
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            if (start != null) {
                long skip = is.skip(start);
                if (skip < start) System.out.println("ERROR: skip fail: skipped=" + skip + ", start: " + start);
                to = to - start + 1;
            }
            byte[] bytes = new byte[size == null ? 2048 : size];
            int len;
            if (to == null) {
                while ((len = is.read(bytes)) != -1) os.write(bytes, 0, len);
            } else {
                while (to > 0 && (len = is.read(bytes)) != -1) {
                    os.write(bytes, 0, to < len ? (int) ((long) to) : len);
                    to -= len;
                }
            }
            os.flush();
        } catch (IOException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * 读取上传目录中的文件列表
     *
     * @param directory    文件目录
     * @param baseURL      文件访问地址前缀
     * @param thumbnailURL 文件缩略图访问地址前缀
     * @return List
     */
    public static List<Map<String, Object>> list(File directory, String baseURL, String thumbnailURL) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (!directory.exists() || !directory.isDirectory()) return list;
        File[] files = directory.listFiles();
        if (files == null) return list;
        for (File f : files) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", f.getName());
            map.put("isDirectory", f.isDirectory());
            if (!f.isDirectory()) {
                map.put("length", f.length());
                map.put("url", baseURL + f.getName());
                if (isImage(f)) map.put("thumbnail", thumbnailURL + f.getName());
            } else {
                map.put("length", 0L);
            }
            map.put("updateTime", f.lastModified());
            list.add(map);
        }
        return list;
    }

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return String
     */
    public static String getContentType(File file) {
        String contentType = null;
        try {
            contentType = new Tika().detect(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentType;
    }

    /**
     * 判断文件是否是图片类型
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isImage(File file) {
        String contentType = getContentType(file);
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 设置下载文件的header
     *
     * @param response HttpServletResponse
     * @param fileName 文件名称
     */
    public static void setDownloadHeader(HttpServletResponse response, String fileName) {
        response.setContentType("application/force-download");
        try {
            fileName = URLEncoder.encode(fileName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
    }

    /**
     * 输出404错误页面
     *
     * @param response HttpServletResponse
     */
    public static void outNotFund(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        outMessage("404 Not Found", null, response);
    }

    /**
     * 输出错误页面
     *
     * @param title    标题
     * @param message  内容
     * @param response HttpServletResponse
     */
    public static void outMessage(String title, String message, HttpServletResponse response) {
        response.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.write("<!doctype html>");
            writer.write("<title>" + title + "</title>");
            writer.write("<h1 style=\"text-align: center\">" + title + "</h1>");
            if (message != null) writer.write(message);
            writer.write("<hr/><p style=\"text-align: center\">Easy File Server</p>");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 支持跨域请求
     *
     * @param response HttpServletResponse
     */
    public static void cross(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, x-requested-with, X-Custom-Header, Authorization");
    }

}
