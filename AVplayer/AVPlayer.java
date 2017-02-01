package AVplayer;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import java.nio.channels.FileChannel;
import java.awt.event.*;

public class AVPlayer {

	static JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	static BufferedImage img;
	static int width ;
	static int height; 
	static byte[] bytes;
	long len ;
	static FileInputStream is;
	JButton btPlay;
	JButton btPause;
	JButton btStop;
	JButton btDblSpd;
	JButton btDblBack;
	static boolean bIfPause = true;
	static boolean bIfStop  = false;
	static boolean doubleSpeed = false;
	static boolean back =false;
	static PlaySound sound;
	 int iFrameCnt=0;
	 int max_framecnt =0;
	 public void read_one_img(){
		int offset = 0;
		int numRead = 0;
		try{
			if(back) {
						
						try {
							iFrameCnt--; 
							FileChannel     fc = is.getChannel();
							if(iFrameCnt>0) {fc.position((long)(iFrameCnt-1)* bytes.length);}
							else { iFrameCnt=0; fc.position(0);}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
			} else {iFrameCnt++;}
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			int ind = 0;
			for(int y = 0; y < height; y++){
	
				for(int x = 0; x < width; x++){
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					//byte g = bytes[ind++];
					//byte b = bytes[ind++]; 
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x,y,pix);
					//ind+=3;
					 ind++;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void initialize(String[] args){
		 width = 480;
		//width = 1280;
		height = 270;
		//height =720;
		 iFrameCnt =0;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		try {
			File file = new File(args[0]);
			is = new FileInputStream(file);
			len = width*height*3;
			bytes = new byte[(int)len];
			read_one_img();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 

		// Use labels to display the images
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    //	sound.myEnd();
		        System.gc();
		    }
		});
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		FileChannel     fc = is.getChannel();
		try {
			max_framecnt=(int)fc.size()/bytes.length;
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		JLabel lbText1 = new JLabel("Video: " + args[0]);
		lbText1.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel lbText2 = new JLabel("Audio: " + args[1]);
		lbText2.setHorizontalAlignment(SwingConstants.LEFT);
		lbIm1 = new JLabel(new ImageIcon(img));

		btPlay = new JButton("Play ");		
		btPause = new JButton("Pause");		
		btStop = new JButton("Stop");	
		btDblSpd = new JButton(">>");
		btDblBack = new JButton("<<");
		btPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e1) {
				bIfPause = false;
				bIfStop  = false;
				doubleSpeed =false;
				back = false;
				if (sound.IfPause() ) {
					sound.myPlay(iFrameCnt);
				}
			}
		});
		btDblBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e1) {
				bIfPause = false;
				bIfStop  = false;
				doubleSpeed =true;
				back = true;
				if (!sound.IfPause()) {
					sound.myPause();
				}
			}
		});
		
		btDblSpd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e1) {
				bIfPause = false;
				bIfStop  = false;
				doubleSpeed =true;
				back = false;
				if (!sound.IfPause()) {
					sound.myPause();
				}
			}
		});
		
		
		btPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bIfPause = true;
				if (!sound.IfPause()) {
					sound.myPause();
				}
				System.out.println(iFrameCnt);
			}
		});
		btStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bIfPause = true;
				if (!sound.IfStop()) {
					sound.myStop();
				}
				FileChannel     fc = is.getChannel();
				try {
					fc.position(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				read_one_img();
				iFrameCnt =0;
			}
		});
		
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		frame.getContentPane().add(lbIm1, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		frame.getContentPane().add(btPlay, c);
		
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 3;
		frame.getContentPane().add(btDblBack, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 3;
		frame.getContentPane().add(btDblSpd, c);
		
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 3;
		frame.getContentPane().add(btPause, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 4;
		c.gridy = 3;
		frame.getContentPane().add(btStop, c);
		
		
		frame.pack();
		frame.setVisible(true);
		 
	}
	int offset_num=0;
	public void playVid(){
		
		while(true){
			if(!bIfPause){
				//ren.playVid();
				long starttime = System.currentTimeMillis();
					
						img.flush();
						read_one_img();
						frame.repaint();
				long endtime  = System.currentTimeMillis();
					 try{
						 if(!doubleSpeed){
							 if(offset_num>99)
							 { Thread.sleep(135-(endtime-starttime)); offset_num=0;}
							 else { Thread.sleep(66-(endtime-starttime)); offset_num++;}
						}
						 else {
							 if(offset_num>99)
							 { Thread.sleep(67-(endtime-starttime)); offset_num=0;}
							 else { Thread.sleep(33-(endtime-starttime)); offset_num++;}
						 }
						
			          } catch (Exception exc){}
					 
					 if(iFrameCnt==max_framecnt-1){
							if (!sound.IfPause()) {
								sound.myPause();
							}
						}
			}
			else { 
				frame.repaint();
				//System.out.println(iFrameCnt+" "+max_framecnt);
				
			}
		}
	}
	
	public void playWAV(String filename){
		// opens the inputStream
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// initializes the playSound Object
		sound = new PlaySound(inputStream);
		sound.start();
	}
	
   
	public static void main(String[] args) {
		if (args.length < 2) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
		    return;
		}
		AVPlayer ren = new AVPlayer();
		// initializes the playSound Object
		ren.initialize(args);
		ren.playWAV(args[1]);
		ren.playVid();
		
		
	}

}