package com.topsec.tsm.sim.auth.web;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.topsec.tal.base.util.ObjectUtils;

/**
 * 验证码生成
 * @author hp
 *
 */
public class ValidateCodeServlet extends HttpServlet{
	
	private String choose="23456789abcdefghkmnpqrstuvwxyzABCDEFGHKLMNPQRSTUVWXYZ";
	private Random rand=new Random();
	private int width = 70;
	private int height = 20 ;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("image/jpeg") ;
		char display[]={'0',' ','0',' ','0',' ','0'},code[]={'0','0','0','0'},temp ;
		for(int i=0;i<4;i++){
			temp=choose.charAt(rand.nextInt(choose.length()));
			display[i*2]=temp;
			code[i]=temp;
		}
		String random=String.valueOf(display);
		HttpSession session = req.getSession() ;
		session.setAttribute("validCode",String.valueOf(code));
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		//以下填充背景颜色
		g.setColor(Color.decode("#B7CFEE"));
		g.fillRect(0, 0, width, height);
		//设置字体颜色
		g.setColor(Color.RED);
		Font font=new Font("Arial",Font.BOLD,16);
		g.setFont(font);
		g.drawString(random,5,15);
		g.dispose();
		ServletOutputStream outStream = resp.getOutputStream();
		try {/*
			JPEGImageEncoder encoder =JPEGCodec.createJPEGEncoder(outStream);
			encoder.encode(image);*/
			ImageIO.write(image, "JPEG", outStream);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ObjectUtils.close(outStream) ;
		}
	}
}
