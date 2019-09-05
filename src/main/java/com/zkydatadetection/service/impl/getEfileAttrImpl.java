package com.unis.zkydatadetection.service.impl;

import ch.qos.logback.classic.Logger;
import com.unis.zkydatadetection.service.getEfileAttr;
import it.sauronsoftware.jave.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service(value = "getEfileAttr")
public class getEfileAttrImpl implements getEfileAttr {
    private final Logger log = (Logger) LoggerFactory.getLogger("getEfileAttrImpl.class");
    private static MP3File mp3File;
    @Override
    public Map getImage(String efilepath) {
        Map imageMap = new HashMap();
        String ext = efilepath.substring(efilepath.lastIndexOf("."),efilepath.length());
        File file = new File(efilepath);//读取文件路径
        try{
            BufferedImage bi = ImageIO.read(new FileInputStream(file));
            Long filesize = file.length();
            int width = bi.getWidth();
            int height = bi.getHeight();
            String xs =  width+","+height;
            String fbl = width+"x"+ height;
            BufferedImage sourceImg = null;
            sourceImg = ImageIO.read(new FileInputStream(file));
            System.out.println(String.format("%.1f",file.length()/1024.0));// 源图大小
            System.out.println(sourceImg.getWidth()); // 源图宽度
            System.out.println(sourceImg.getHeight()); // 源图高度
            imageMap.put("efileges",ext);
            imageMap.put("efilesize",filesize);
            imageMap.put("fbl",fbl);
            imageMap.put("xs",xs);
            imageMap.put("width",sourceImg.getWidth());
            imageMap.put("height",sourceImg.getHeight());
        }catch (IOException e){
            e.printStackTrace();
            log.error("获取图片像素异常！");
        }
        return imageMap;
    }

    @Override
    public Map getAudio(String efilepath) {
        Map audioMap = new HashMap();
        File audioPath = new File(efilepath);
        System.out.println("----------------Loading...Head-----------------");
        try{
            mp3File = new MP3File(efilepath);//封装好的类
            MP3AudioHeader header = mp3File.getMP3AudioHeader();
            int sc = header.getTrackLength();
            System.out.println("时长: " + sc); //获得时长
            String btl = header.getBitRate();
            System.out.println("比特率: " + btl); //获得比特率
            System.out.println("音轨长度: " + header.getTrackLength()); //音轨长度
            String gs = header.getFormat();
            System.out.println("格式: " + gs); //格式，例 MPEG-1
            System.out.println("声道: " + header.getChannels()); //声道
            String cyl = header.getSampleRate();
            System.out.println("采样率: " + cyl); //采样率
            System.out.println("MPEG: " + header.getMpegLayer()); //MPEG
            System.out.println("MP3起始字节: " + header.getMp3StartByte()); //MP3起始字节
            System.out.println("精确的音轨长度: " + header.getPreciseTrackLength()); //精确的音轨长度
            audioMap.put("efilesize",audioPath.length());
            audioMap.put("sc",sc);
            audioMap.put("btl",btl);
            audioMap.put("efilegs",gs);
            audioMap.put("cyl",cyl);
        } catch (Exception e) {
            System.out.println("没有获取到任何信息");
        }
        return audioMap;
    }

    @Override
    public Map getVideo(String efilepath) {
        Map videoMap = new HashMap();
//        ReadVideo rv = new ReadVideo();
		File source = new File("F:/projectData/data/test/Wildlife.wmv");
//        rv.getVedioInfo("F:/projectData/data/test/F7B8912C47DC285FCE4B886ADDD1CC95.mp4");
		Encoder encoder = new Encoder();
		FileChannel fc = null;
		String size = "";
		try {
			it.sauronsoftware.jave.MultimediaInfo m = encoder.getInfo(source);
			long ls = m.getDuration();
			System.out.println("此视频时长为:" + ls / 60000 + "分" + (ls) / 1000 + "秒！");
			// 视频帧宽高
			System.out.println("此视频高度为:" + m.getVideo().getSize().getHeight());
			System.out.println("此视频宽度为:" + m.getVideo().getSize().getWidth());
			System.out.println("此视频格式为:" + m.getFormat());
			System.out.println("此视频数据速率为:" + m.getVideo().getBitRate());
			System.out.println("此视频帧速率为:" + m.getVideo().getFrameRate());
			System.out.println("此视频xx3为:" + m.getVideo().getDecoder());
			System.out.println("此视频音频比特率为:" + m.getAudio().getBitRate());
			System.out.println("此视频音频频道为:" + m.getAudio().getChannels());
			System.out.println("此视频xx6为:" + m.getAudio().getDecoder());
			System.out.println("此视频音频采样率为:" + m.getAudio().getSamplingRate());
			FileInputStream fis = new FileInputStream(source);
			fc = fis.getChannel();
			BigDecimal fileSize = new BigDecimal(fc.size());
			size = fileSize.divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP) + "MB";
			System.out.println("此视频大小为" + size);
            String ext = efilepath.substring(efilepath.lastIndexOf("."),efilepath.length());
			long sc = ls/1000;
			long efilesize = source.length();
			String spml = String.valueOf(efilesize/1024*8/sc);
            videoMap.put("efilesize",efilesize);
            videoMap.put("sc",ls);
            videoMap.put("spml",spml);
            videoMap.put("efilegs",ext);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != fc) {
				try {
					fc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        return videoMap;
    }

    public static void main(String[] args){
        String efilepath = "D:/data/B103/B103-KY/B103-KY-00001/B103-KY-00001-001/B103-KY-00001-001-001.tif";
//        String efilepath =  "D:/data/B103/B103-WS/B103-WS-00001/B103-WS-00001-001/B103-WS-00001-001-001.png";
        getImage1(efilepath);
    }

    public static void getImage1(String efilepath) {
        Map imageMap = new HashMap();
        String ext = efilepath.substring(efilepath.lastIndexOf("."),efilepath.length());
        File file = new File(efilepath);//读取文件路径
        try{
            File picture = new File(efilepath);
            BufferedImage sourceImg = ImageIO.read(new FileInputStream(picture));
            Long filesize = picture.length();
            int width = sourceImg.getWidth();
            int height = sourceImg.getHeight();
            String xs =  width+","+height;
            String fbl = width+"x"+ height;
            System.out.println(String.format("%.1f",picture.length()/1024.0));// 源图大小
            System.out.println(sourceImg.getWidth()); // 源图宽度
            System.out.println(sourceImg.getHeight()); // 源图高度
//	    BufferedImage sourceImg =ImageIO.read(new FileInputStream(picture));
            System.out.println(String.format("%.1f",picture.length()/1024.0));// 源图大小
            System.out.println(sourceImg.getWidth()); // 源图宽度
            System.out.println(sourceImg.getHeight()); // 源图高度
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
