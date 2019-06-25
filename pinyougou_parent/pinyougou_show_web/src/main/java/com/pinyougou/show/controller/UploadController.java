package com.pinyougou.show.controller;

import com.pinyougou.common.utils.FastDFSClient;
import com.pinyougou.entity.ResultInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVICE_URL}")
    private String FILE_SERVICE_URL;

    @RequestMapping("/upload")
    public ResultInfo upload(MultipartFile file){
        try {
            //1.图片原来的名字
            String oldName = file.getOriginalFilename();

            //2.获取后缀名，不带"." eg:jpg
            String extName = oldName.substring(oldName.lastIndexOf(".")+1);

            //3.创建FasDFS客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fdfs_client.conf");

            //4.上传到FastDFS
            String fileID = fastDFSClient.uploadFile(file.getBytes(), extName);
            System.out.println(fileID);

            //5.拼接文件url
            String url = FILE_SERVICE_URL + fileID;
            System.out.println(url);

            //6.返回文件url
            return new ResultInfo(true, url);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResultInfo(false, "上传失败");
        }
    }
}
