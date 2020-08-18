package com.qx.tools.controller;


import com.qx.tools.utils.CSVUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 加解密请求接口实现
 *
 * @author zhangqinghua
 */
@Slf4j
@RestController
@RequestMapping("/facade/tool")
public class EnDecryptFacadeController {

    private static final long MAX_SIZE = 5 * 1024;

    /**
     * 文件上传+逻辑处理+文件下载
     *
     * @param file
     * @author: gw
     * @date: 2020/8/18 1:00 下午
     */
    @PostMapping(value = "/batchDecryptWithFile")
    public void batchDecryptWithFile(@RequestParam(value = "file") MultipartFile file,
                                     HttpServletResponse response) {
        String checkResult = checkBatchWithFileRequest(file);
        if (null != checkResult) {
            return;
        }
        try {
            List<String> contents = fileToList(file);
            String fileName = "batchEncrypt";
            String[] head = new String[]{"手机号密文", "手机号明文"};
            List<String[]> data = new ArrayList<>();
            String[] a = new String[]{"18813047883", "188yhsi7883"};
            data.add(a);
            File batchEncrypt = CSVUtils.makeTempCSV(fileName, head, data);
            CSVUtils.downloadFile(response, batchEncrypt, fileName);
        } catch (IOException e) {
            log.error("batchDecryptWithFile has error !", e);
        }
    }


    private String checkBatchWithFileRequest(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        if (!file.isEmpty()) {
            if (file.getSize() > MAX_SIZE) {
                return "上传文件-file大小不可超过5K";
            }
            int begin = originalFilename.indexOf(".");
            int last = originalFilename.length();
            String suffix = originalFilename.substring(begin, last);
            if (!suffix.endsWith(".txt")) {
                return "上传文件-file不是txt格式";
            }
        } else {
            return "上传文件-file内容不能为空";
        }
        return null;
    }

    public List<String> fileToList(MultipartFile file) {
        List<String> contents = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            File tempFile = new File("temp" + File.separator + "enDecrypt" + File.separator + file.getOriginalFilename());
            File parentFile = tempFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            FileUtils.copyInputStreamToFile(inputStream, tempFile);

            LineIterator iterator = FileUtils.lineIterator(tempFile, "UTf-8");
            while (iterator.hasNext()) {
                String content = iterator.nextLine();
                if (StringUtils.isBlank(content)) {
                    continue;
                }
                contents.add(content);
            }
        } catch (Exception e) {
            log.error("fileToList has error !", e);
        }
        return contents;
    }
}
