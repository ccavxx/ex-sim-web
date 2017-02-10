package com.topsec.tsm.sim.sysconfig.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/sysconfig")
public class ReportFooterImageController {

	private static String upLoadPath = "user\\report\\";
	private static int MaxUpLoadSize = 1024 * 1024;
	private static int MaxSize = 200 * 1024;
	private static int MaxWidth = 164;
	private static int MaxHeight = 25;
	private static int MinWidth = 35;
	private static int MinHeight = 15;
	private static String selfFileName = "selfPicture";

	/**
	 *功能描述：展现报表页脚图片
	 * 
	 * @author: ZhouZhijie
	 *@param reportId
	 *@throws Exception
	 */
	@RequestMapping("/getReportFooterImage")
	@ResponseBody
	public Object getReprotFooterImage(String reportId) throws Exception {
		JSONObject result = new JSONObject();

//		SystemConfig sysconf = null;//sysConfigService.getConfig(reportId);
//		if (sysconf == null) {
//			// 如果sysconf为null，说明用户没有改变过报表页脚图片，所以采用默认的报表页脚图片
//			result.put("imagePath", "/img/icons/reportFooterImage/topsec.jpg");
//			result.put("result", "success");
//		} else {
//			result.put("imagePath", "/user/reportFooterImage/convert/selfPicture.jpg");
//			result.put("result", "success");
//		}

		return result;
	}

	@RequestMapping("/modifyReportFooterImage")
	@ResponseBody
	public Object modifyReportFooterImage(HttpServletRequest request, @RequestParam("upLoad") MultipartFile file) throws Exception {
		JSONObject result = new JSONObject();
		try {

			String fileName = file.getOriginalFilename();
			String name = fileName.substring(fileName.lastIndexOf("\\") + 1);
			if (file.getSize() > ReportFooterImageController.MaxUpLoadSize) { // 上传图片大于1M
				result.put("result", "toLarge");
				return result;
			}
			String fileType = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
			if (!(fileType.equals("jpg") || fileType.equals("png") || fileType.equals("bmp"))) {
				result.put("result", "formatWrong");
				return result;
			}
			String realPath = request.getSession().getServletContext().getRealPath("/");
			File dir = new File((realPath + "\\" + upLoadPath).replace('\\', '/'));
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File srcPicFile = new File((dir + "\\" + ReportFooterImageController.selfFileName + ".jpg").replace('\\', '/'));
			file.transferTo(srcPicFile);

			BufferedImage buffImg = ImageIO.read(srcPicFile);
			buffImg = ImageIO.read(srcPicFile);
			try {
				if ((buffImg.getHeight() < ReportFooterImageController.MinHeight)
						|| (buffImg.getWidth() < ReportFooterImageController.MinWidth || (buffImg.getHeight() > 500) || (buffImg.getWidth() > 500))) {

					result.put("result", "toBig");
					return result;
				}
			} catch (Exception e) {
				e.printStackTrace();
				result.put("result", "toBig");
				return result;
			}

			File destDir = new File((realPath + "\\" + upLoadPath + "\\convert").replace('\\', '/'));
			File destPicFile = new File((destDir + "\\" + selfFileName + ".jpg").replace('\\', '/'));
			
			if (file.getSize() > ReportFooterImageController.MaxSize) {
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				createSmallPicture(srcPicFile, destPicFile, ReportFooterImageController.MaxHeight, ReportFooterImageController.MaxWidth);
				srcPicFile = destPicFile;
			}

			if ((buffImg.getHeight() > ReportFooterImageController.MaxHeight) || (buffImg.getWidth() > ReportFooterImageController.MaxWidth)) {

				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				createSmallPicture(srcPicFile, destPicFile, ReportFooterImageController.MaxHeight, ReportFooterImageController.MaxWidth);
				srcPicFile = destPicFile;
			}else{
				createSmallPicture(srcPicFile, destPicFile, ReportFooterImageController.MaxHeight, ReportFooterImageController.MaxWidth);
			}
			
			//把用户自定义的报表图片信息存储到数据库，只有Id字段是有用的（我们只用Id就可以），其他信息可以伪造。
			String conftojson = "{\"result\":\"sucess\"}";
			//SystemConfig sysconf = new SystemConfig("report_footer_image", "报表页脚图片配置", "ReprotFooterImage", conftojson, new Date());
			//sysConfigService.modifyConfig(sysconf);
			
			result.put("result", "success");
			return result;

		} catch (Exception e) {
			e.printStackTrace();

			result.put("result", "fail");
			return result;
		}

	}

	private static boolean createSmallPicture(File srcPicFile, File destPicFile, int picMiniHeight, int picMiniWidth) throws IOException {
		BufferedImage srcBi = ImageIO.read(srcPicFile);
		if (srcBi == null)
			return false;
		
		int destW = srcBi.getWidth();
		int destH = srcBi.getHeight();
		
		if (destW == 0 || destH == 0)// 防止空图片
			return false;

		destW = picMiniWidth;
		destH = picMiniHeight;

		BufferedImage newImg = new BufferedImage(destW, destH, BufferedImage.TYPE_INT_RGB);
		newImg.createGraphics().drawImage(srcBi, 0, 0, destW, destH, null);

		ImageWriter writer = null;

		Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
		if (iter.hasNext()) {

			writer = (ImageWriter) iter.next();

		} else {
			throw new IOException("no ImageWriter for jepg");
		}
		ImageOutputStream ios = ImageIO.createImageOutputStream(destPicFile);

		writer.setOutput(ios);

		JPEGImageWriteParam iwparam = (JPEGImageWriteParam) writer.getDefaultWriteParam();

		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		int picJpegQuality = 80;
		iwparam.setCompressionQuality((float) (picJpegQuality / 100.0));

		writer.write(null, new IIOImage(newImg, null, null), iwparam);
		ios.flush();
		writer.dispose();
		ios.close();
		return true;
	}
	
	//恢复默认图片,只要把数据库中 sim_sysconfig表中Id=report_footer_image的字段删除即可
	@RequestMapping("recoverImage")
	@ResponseBody
	public Object recoverImage() throws Exception {
		JSONObject result=new JSONObject();
		//sysConfigService.deleteById("report_footer_image");
		result.put("result", "success");
		return result;
	}

}
