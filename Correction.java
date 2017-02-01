package Correction;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.nio.channels.FileChannel;
import java.awt.event.*;
import java.util.Random;


public class Correction {
	
	final static int totalFrame = 4500;
	static JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	static BufferedImage img;
	static int width;
	static int height; 
	static byte[] bytes;
	long len;
	static FileInputStream is;
	static FileInputStream input;
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
	int iFrameCnt;
	int[] MVDiff = new int[totalFrame-1];
	int[][] lastYvalue;
	final int BLOCK_SIZE = 8;
	OutputStream os ;
	
	static int CORRECTION_RANGE = 8;
	
	static boolean debug = true;
	 
	public boolean read_one_img() {
		int offset = 0;
		int numRead = 0;
		try{
			if (back) {	
				try {
					iFrameCnt--; 
					FileChannel fc = is.getChannel();
					if (iFrameCnt > 0) {
						fc.position((long)(iFrameCnt - 1) * bytes.length);
					} else { 
						iFrameCnt = 0; 
						fc.position(0);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
			} else {
				iFrameCnt++;
			}
			
			while (offset < bytes.length) {
				if ((numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				} else {
					return false;
				}
			}
			int ind = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public void initialize(String inputVideo, String inputAudio, int screen_width, int screen_height) {
		width = screen_width;
		height = screen_height;
		iFrameCnt = 0;
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		try {
			File file = new File(inputVideo);
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

		JLabel lbText1 = new JLabel("Video: " + inputVideo);
		lbText1.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel lbText2 = new JLabel("Audio: " + inputAudio);
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
				if (sound.IfPause()) {
					sound.myPlay(iFrameCnt);
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
			}
		});
		
		btStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StopAndReset();
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
	
	public void readFile(String inputVideo, String inputAudio)
	{
		try {
			File file = new File(inputVideo);
			input = new FileInputStream(file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
	}
	
	public void readEachFrame(int count) throws IOException
	{
		long len = width*height*3;
		byte[] bytes = new byte[(int)len];

		int offset = 0;
		int numRead = 0;
		try {
			while (offset < bytes.length && (numRead=input.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int ind = 0;
		int[][] gray = new int[width][height];
		for(int y = 0; y < height; y++){

			for(int x = 0; x < width; x++){
				int temp = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2];
				
				temp = ((int)(r & 0xff)*30 + (int)(g & 0xff)*59 + (int)(b & 0xff)*11 + 50) / 100;
				if(temp > 255)
					gray[x][y] = 255;
				else if(temp<0)
					gray[x][y] = 0;
				else
					gray[x][y] = temp;
				
				ind++;
			}
		}
		
		if (count == 0) {
			lastYvalue = gray.clone();
		} else {
			MVDiff[count-1] = GetMotionVectorDifference(lastYvalue, gray, width, height);
			lastYvalue = gray.clone();
		}
		
		
	}
	
	public int GetMotionVectorDifference(int[][] Ori, int[][] Cur, int width, int hight) {
		int sumDmv = 0;
		Random rd = new Random();
		int iSample_x = 0;
		int iSample_y = 0;
		for (int i = 0; i < 120; i++) {
			iSample_x = rd.nextInt(width - BLOCK_SIZE);
			iSample_y = rd.nextInt(hight - BLOCK_SIZE);
			int MinDifference = 999999999;
			int Min_x = 0;
			int Min_y = 0;
			int start_y = iSample_y < BLOCK_SIZE? 0 : iSample_y - BLOCK_SIZE;
			int end_y = iSample_y + 2 * BLOCK_SIZE <= hight? iSample_y + BLOCK_SIZE : hight - BLOCK_SIZE;
			int start_x = iSample_x < BLOCK_SIZE? 0 : iSample_x - BLOCK_SIZE;
			int end_x = iSample_x + 2 * BLOCK_SIZE <= width? iSample_x + BLOCK_SIZE : width - BLOCK_SIZE;
			for (int y = start_y; y < end_y ; y++) {
				for (int x = start_x; x < end_x; x++) {
					int difference = 0;
					for (int sy = iSample_y, y_count = 0; y_count < BLOCK_SIZE; sy++, y_count++) {
						for (int sx = iSample_x, x_count = 0; x_count < BLOCK_SIZE; sx++, x_count++) {
							difference += Math.abs(Cur[sx][sy] - Ori[x + x_count][y + y_count]);
						}
					}
					if (difference < MinDifference) {
						Min_x = x;
						Min_y = y;
						MinDifference = difference;
					}
				}
			}
			sumDmv += Math.sqrt(Math.pow(Min_x - iSample_x, 2) + Math.pow(Min_y - iSample_y, 2));
		}
		return sumDmv;
	}
	
	public int[] GetMotionVector(int[][] Ori, int[][] Cur, int width, int hight) {
		int sumDmv = 0;
		int iSample_x = 0;
		int iSample_y = 0;
		int sum_MV_x = 0;
		int sum_MV_y = 0;
		int iCount = 0;
		do {			
			int MinDifference = 999999999;
			int Min_x = 0;
			int Min_y = 0;
			int start_y = iSample_y < BLOCK_SIZE? 0 : iSample_y - BLOCK_SIZE;
			int end_y = iSample_y + 2 * BLOCK_SIZE <= hight? iSample_y + BLOCK_SIZE : hight - BLOCK_SIZE;
			int start_x = iSample_x < BLOCK_SIZE? 0 : iSample_x - BLOCK_SIZE;
			int end_x = iSample_x + 2 * BLOCK_SIZE <= width? iSample_x + BLOCK_SIZE : width - BLOCK_SIZE;
			for (int y = start_y; y < end_y ; y++) {
				for (int x = start_x; x < end_x; x++) {
					int difference = 0;
					for (int sy = iSample_y, y_count = 0; y_count < BLOCK_SIZE; sy++, y_count++) {
						for (int sx = iSample_x, x_count = 0; x_count < BLOCK_SIZE; sx++, x_count++) {
							difference += Math.abs(Cur[sx][sy] - Ori[x + x_count][y + y_count]);
						}
					}
					if (difference < MinDifference) {
						Min_x = x;
						Min_y = y;
						MinDifference = difference;
						//System.out.println("Min_x: " + Min_x + " Min_y: " + Min_y + " difference: " + difference);
					}
				}
			}
			//System.out.println("Min_x: " + Min_x + " Min_y: " + Min_y);
			/*
			for (int sy = iSample_y, y_count = 0; y_count < BLOCK_SIZE; sy++, y_count++) {
				for (int sx = iSample_x, x_count = 0; x_count < BLOCK_SIZE; sx++, x_count++) {
					System.out.println("Current[" + sx + "," + sy + "]: " + Cur[sx][sy] + " Origin: " + Ori[Min_x + x_count][Min_y + y_count]);
				}
			}*/
			sum_MV_x += Min_x - iSample_x;
			sum_MV_y += Min_y - iSample_y;
			sumDmv += Math.sqrt(Math.pow(Min_x - iSample_x, 2) + Math.pow(Min_y - iSample_y, 2));
			//System.out.println("iSample_x: " + iSample_x + " iSample_y: " + iSample_y + "Min_x: " + Min_x + " Min_y: " + Min_y);
			iCount ++;
			if (iSample_y == 0 && iSample_x < width - 2 * BLOCK_SIZE) {
				iSample_x += BLOCK_SIZE;
			} else if (iSample_y < hight - 2 * BLOCK_SIZE && iSample_x == width - 2 * BLOCK_SIZE) {
				iSample_y += BLOCK_SIZE;
			} else if (iSample_x > 0) {
				iSample_x -= BLOCK_SIZE;
			} else {
				iSample_y -= BLOCK_SIZE;
			}
			/*
			 * if (iSample_y == 0 && iSample_x < width - BLOCK_SIZE - 1) {
				iSample_x += 1;
			} else if (iSample_y < height - BLOCK_SIZE - 1 && iSample_x == width - BLOCK_SIZE - 1) {
				iSample_y += 1;
			} else if (iSample_x > 0) {
				iSample_x -= 1;
			} else {
				iSample_y -= 1;
			}
			 */
		} while (!(iSample_y == 0 && iSample_x == 0));
		
		int[] ayMV = new int[3];
		ayMV[0] = sumDmv / iCount;
		ayMV[1] = sum_MV_x / iCount;
		ayMV[2] = sum_MV_y / iCount;
		return ayMV;
	}
	
	public void CorrectingQuality(String inputVideo, String inputAudio) throws IOException {
		width = 480;
		height = 270;
		readFile(inputVideo,inputAudio);
		int orig_width = width;
		int orig_height = height;
		long len = orig_width*orig_height*3;
		int[] ayMV = new int[3];
		int new_width = orig_width - 2 * CORRECTION_RANGE;
		int new_height = orig_height - 2 * CORRECTION_RANGE;
		byte[] new_bytes = new byte[new_width*new_height*3];
		os  = new FileOutputStream("correction_video.rgb");
		for(int i = 0; i < totalFrame; i++)
		{
			byte[] bytes = new byte[(int)len];
			int offset = 0;
			int numRead = 0;
			try {
				while (offset < bytes.length && (numRead=input.read(bytes, offset, bytes.length-offset)) >= 0) {
					offset += numRead;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int ind = 0;
			int[][][] RGB = new int[3][orig_width][orig_height];
			int[][] gray = new int[orig_width][orig_height];
			for(int y = 0; y < orig_height; y++){
				for(int x = 0; x < orig_width; x++){
					int temp = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+orig_height*orig_width];
					byte b = bytes[ind+orig_height*orig_width*2];
					
					temp = ((int)(r & 0xff)*30 + (int)(g & 0xff)*59 + (int)(b & 0xff)*11 + 50) / 100;
					if(temp > 255)
						gray[x][y] = 255;
					else if(temp<0)
						gray[x][y] = 0;
					else
						gray[x][y] = temp;

					RGB[0][x][y] = (int)(r & 0xff);
					RGB[1][x][y] = (int)(g & 0xff);
					RGB[2][x][y] = (int)(b & 0xff);
									
					ind++;
				}
			}
			
			int iStart_x = CORRECTION_RANGE;
			int iStart_y = CORRECTION_RANGE;
			if (i == 0) {
				lastYvalue = gray.clone();
			} else {
				ayMV = GetMotionVector(lastYvalue, gray, orig_width, orig_height);
				if (ayMV[0] < 2 * CORRECTION_RANGE) {
					iStart_x = iStart_x - (ayMV[1]/2);
					iStart_y = iStart_y - (ayMV[2]/2);
					System.out.println("Selected correct frame: "+i+" x: "+ayMV[1]/2 + " y: " + ayMV[2]/2);
				} 
				lastYvalue = gray.clone();
			}
			int new_index = 0;
			for(int y = 0, orig_y = iStart_y; y < new_height; y++, orig_y++) {
				for(int x = 0, orig_x = iStart_x; x < new_width; x++, orig_x++) {
					new_bytes[new_index] = (byte) RGB[0][orig_x][orig_y];
					new_bytes[new_index+new_height*new_width] = (byte) RGB[1][orig_x][orig_y];
					new_bytes[new_index+new_height*new_width*2] = (byte) RGB[2][orig_x][orig_y];
					new_index++;
				}
			}
			os.write(new_bytes, 0, new_bytes.length);
		}
		os.close();
	}
	
	public void playVid() {
		int offset_num = 0;
		long starttime;
		while (true) {
			if (!bIfPause) {
				//ren.playVid();
				starttime = System.currentTimeMillis();
				img.flush();
				if (read_one_img()) {
					frame.repaint();
					long endtime  = System.currentTimeMillis();
					try {
						if (!doubleSpeed) {
							if (offset_num > 99) { 
								Thread.sleep(135 - (endtime - starttime)); 
								offset_num = 0;
							} else { 
								Thread.sleep(66 - (endtime - starttime)); 
								offset_num++;
							}
						} else {
							if (offset_num > 99) { 
								Thread.sleep(67 - (endtime - starttime)); 
								offset_num = 0;
							} else { 
								Thread.sleep(33 - (endtime - starttime)); 
								offset_num++;
							}
						}
					} catch (Exception exc) {}
				} else {
					StopAndReset();
				}
			} else { 
				frame.repaint();
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
	
    private void StopAndReset() {
    	bIfPause = true;
		if (!sound.IfStop()) {
			sound.myStop();
		}
		FileChannel fc = is.getChannel();
		try {
			fc.position(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		read_one_img();
		iFrameCnt = 0;
    }
    
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file]");
		    return;
		}
		Correction ren = new Correction();
		// initializes the playSound Object
		ren.CorrectingQuality(args[0],args[1]);
		ren.initialize("correction_video.rgb",args[1], 480 - 2 * CORRECTION_RANGE, 270 - 2 * CORRECTION_RANGE);
		ren.playWAV(args[1]);
		ren.playVid();
	}

}