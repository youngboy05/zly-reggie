package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.imgpath}")  // 通过 @Value 注解加载配置文件中自定义的配置项
    private String bashpath;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        // 利用UUID生成新的唯一文件名
        String originalFilename = file.getOriginalFilename();
        String xname = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + xname;
        File imgpath = new File(bashpath);
        if (!imgpath.exists()){
            imgpath.mkdirs();
        }
        try {
            file.transferTo(new File(bashpath + filename)); // 将上传的文件转存到指定目录
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename); //返回文件名用于将其添加到菜品表中
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            // 创建字节流读取图片
            FileInputStream fis = new FileInputStream(new File(bashpath + name));
            // 获取输出流写回图片
            ServletOutputStream sos = response.getOutputStream();
            // 设置传回文件的类型
            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fis.read(bytes)) != -1){
                sos.write(bytes,0,len);
                sos.flush();
            }
            fis.close();
            sos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
