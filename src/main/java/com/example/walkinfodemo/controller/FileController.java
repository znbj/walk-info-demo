package com.example.walkinfodemo.controller;

import cn.hutool.core.util.StrUtil;
import com.example.walkinfodemo.common.JsonResult;
import com.example.walkinfodemo.utils.FileServerUtil;
import com.wf.captcha.SpecCaptcha;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${config.upload-location}")
    private String uploadLocation;  // 文件上传磁盘位置
    @Value("${config.upload-uuid-name:false}")
    private Boolean uploadUuidName;  // 文件上传是否使用uuid命名
    /**
     * 验证码
     * @param request
     * @return
     */
    @GetMapping("/captcha")
    public JsonResult captcha(HttpServletRequest request) {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        return JsonResult.ok().setData(specCaptcha.toBase64()).put("text", specCaptcha.text().toLowerCase());
    }



    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public JsonResult upload(@RequestParam MultipartFile file, HttpServletRequest request) {
        try {
            File upload = FileServerUtil.upload(file, getUploadDir(), uploadUuidName);
            return getUploadResult(upload, file.getOriginalFilename(), request);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.error("上传失败").put("error", e.toString());
        }
    }

    @ApiOperation("上传base64文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "base64", value = "base64", required = true, dataType = "string")
    })
    @PostMapping("/upload/base64")
    public JsonResult uploadBase64(String base64, String fileName, HttpServletRequest request) {
        try {
            File upload = FileServerUtil.upload(base64, fileName, getUploadDir());
            return getUploadResult(upload, fileName, request);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonResult.error("上传失败").put("error", e.toString());
        }
    }
    @ApiOperation("下载文件")
    @GetMapping("/download/{dir}/{name:.+}")
    public void download(@PathVariable("dir") String dir, @PathVariable("name") String name,
                         HttpServletResponse response, HttpServletRequest request) {
        FileServerUtil.preview(new File(getUploadDir(), dir + "/" + name), true, null, getPdfOutDir(), response, request);
    }



    @ApiOperation("查询全部文件")
    @GetMapping("/list")
    public JsonResult list(String directory, String sort, String order) {
        if (directory == null || directory.equals("/")) directory = "";
        File file = new File(getUploadDir(), directory);
        if (!directory.isEmpty() && !directory.endsWith("/")) directory = directory + "/";
        List<Map<String, Object>> list = FileServerUtil.list(file, "file/" + directory, "file/thumbnail/" + directory);
        // 设置默认排序规则
        if (sort == null || sort.trim().isEmpty()) {
            sort = "updateTime";
            if (order == null || order.trim().isEmpty()) order = "desc";
        }
        // 根据传递的参数排序
        String finalSort = sort, finalOrder = order;
        if ("length".equals(sort) || "updateTime".equals(sort)) {
            list.sort((o1, o2) -> {
                if ("desc".equals(finalOrder)) {
                    return ((Long) o2.get(finalSort)).compareTo((Long) o1.get(finalSort));
                } else {
                    return ((Long) o1.get(finalSort)).compareTo((Long) o2.get(finalSort));
                }
            });
        } else if ("name".equals(sort)) {
            list.sort((o1, o2) -> {
                if ("desc".equals(finalOrder)) {
                    return ((String) o2.get(finalSort)).compareTo((String) o1.get(finalSort));
                } else {
                    return ((String) o1.get(finalSort)).compareTo((String) o2.get(finalSort));
                }
            });
        }
        // 把文件夹排在前面
        list.sort((o1, o2) -> ((Boolean) o2.get("isDirectory")).compareTo((Boolean) o1.get("isDirectory")));
        return JsonResult.ok().setData(list);
    }
    @ApiOperation("删除文件")
    @DeleteMapping("/remove")
    public JsonResult remove(String path) {
        if (path != null && !path.trim().isEmpty()) {
            File file = new File(getUploadDir(), path);
            if (file.delete()) new File(getUploadSmDir(), path).delete();
        }
        return JsonResult.ok("删除成功");
    }

    /* 封装上传成功的返回结果 */
    private JsonResult getUploadResult(File file, String fileName, HttpServletRequest request) {
        String url = file.getAbsolutePath().substring(getUploadDir().length() - 1).replace("\\", "/");
        String requestURL = StrUtil.removeSuffix(request.getRequestURL(), "/api/file/upload");
        requestURL = StrUtil.removeSuffix(requestURL, "/api/file/upload");
        return JsonResult.ok("上传成功")
                .put("url", url).put("location", requestURL +"/file"+ url)
                .put("fileName", StrUtil.isBlank(fileName) ? file.getName() : fileName)
                .put("dir", "/" + StrUtil.removeSuffix(file.getParentFile().getName(), "/"));
    }

    /* 文件存放位置 */
    private String getBaseDir() {
        return uploadLocation;
    }

    /* 文件上传目录位置 */
    private String getUploadDir() {
        return getBaseDir();
    }

    /* 缩略图存放位置 */
    private String getUploadSmDir() {
        return getBaseDir() + "thumbnail/";
    }

    /* office预览生成pdf缓存位置 */
    private String getPdfOutDir() {
        return getBaseDir() + "pdf/";
    }
}
