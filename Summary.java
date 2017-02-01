package Summary;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.nio.channels.FileChannel;
import java.awt.event.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class Summary {
	
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
	double[] grayMeans = new double[totalFrame];
	double[] means = new double[totalFrame];
	double[] entropys = new double[totalFrame];
	double[] SDs = new double[totalFrame];
	double[] grayMeanDiff = new double[totalFrame-1];
	double[] meanDiff = new double[totalFrame-1];
	double[] entropyDiff = new double[totalFrame-1];
	double[] SDDiff = new double[totalFrame-1];
	int[] MVDiff = new int[totalFrame-1];
	int[] finalFrames;
	int[][] lastYvalue;
	double avgMeanDiff = 0;
	double avgGrayMeanDiff = 0;
	double avgEntropyDiff = 0;
	double avgSDDiff = 0;
	double avgMVDiff = 0;
	LinkedList<shot> list = new LinkedList<shot>();
	LinkedList<shot> summary = new LinkedList<shot>();
	final int BLOCK_SIZE = 8;
	OutputStream os ;
	
	private AudioInputStream audioInputStream = null;
	final int AUDIO_SIZE_PER_FRAME = (int)22050/15;
	private byte[] audioBuffer = new byte[AUDIO_SIZE_PER_FRAME];
	int[] AudioSums = new int[totalFrame];
	double avgAudioSum = 0;
	
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
	
	public void initialize(String inputVideo, String inputAudio) {
		width = 480;
		height = 270;
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
		
		try {
			FileInputStream inputStream = new FileInputStream(inputAudio);
			InputStream waveStream = new BufferedInputStream(inputStream);
			audioInputStream = AudioSystem.getAudioInputStream(waveStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void readEachFrame(int count) throws IOException
	{
		int width = 480;
		int height = 270;
		final int numOfPixels = 480*270;
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
		
		int audio_offset = 0;
		int audio_numRead = 0;
		try {
		    while (audio_offset < audioBuffer.length && (audio_numRead = audioInputStream.read(audioBuffer, audio_offset, audioBuffer.length - audio_offset)) >= 0) {
		    	audio_offset += audio_numRead;
		    }
		} catch (IOException e1) {
		    try {
				throw new PlayWaveException(e1);
			} catch (PlayWaveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (int i = 0; i < AUDIO_SIZE_PER_FRAME; i++) {
			AudioSums[count] += (int)(audioBuffer[i] & 0xff);
		}
		AudioSums[count] = AudioSums[count]/AUDIO_SIZE_PER_FRAME;

		int ind = 0;
		int[] grayDis = new int[256];
		int[][] gray = new int[width][height];
		int[][] RGBDis = new int[3][256];
		int[][][] RGB = new int[3][width][height];
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
				
				grayDis[gray[x][y]] += 1;
				
				RGB[0][x][y] = (int)(r & 0xff);
				RGB[1][x][y] = (int)(g & 0xff);
				RGB[2][x][y] = (int)(b & 0xff);

				RGBDis[0][RGB[0][x][y]] += 1;
				RGBDis[1][RGB[1][x][y]] += 1;
				RGBDis[2][RGB[2][x][y]] += 1;
								
				ind++;
			}
		}
		
		if (count == 0) {
			lastYvalue = gray.clone();
		} else {
			MVDiff[count-1] = GetMotionVectorDifference(lastYvalue, gray, width, height);
			lastYvalue = gray.clone();
		}
		
		int temp = 0;
		int redTemp = 0;
		int greenTemp = 0;
		int blueTemp = 0;
		double redEntropy = 0;
		double greenEntropy = 0;
		double blueEntropy = 0;
		for(int i = 0; i < 256;i++)
		{
			temp = temp + (i*grayDis[i]);
			redTemp = redTemp + (i*RGBDis[0][i]);
			greenTemp = greenTemp + (i*RGBDis[1][i]);
			blueTemp = blueTemp + (i*RGBDis[2][i]);
			double redPi = (double)RGBDis[0][i]/(double)numOfPixels;
			double greenPi = (double)RGBDis[1][i]/(double)numOfPixels;
			double bluePi = (double)RGBDis[2][i]/(double)numOfPixels;
			
			//Red Entropy
			if(RGBDis[0][i]==0)
				redEntropy = redEntropy + 0;
			else
				redEntropy = redEntropy + redPi*(Math.log(redPi)/Math.log(2));
			
			//Green Entropy
			if(RGBDis[1][i]==0)
				greenEntropy = greenEntropy + 0;
			else
				greenEntropy = greenEntropy + greenPi*(Math.log(greenPi)/Math.log(2));
			
			//Blue Entropy
			if(RGBDis[2][i]==0)
				blueEntropy = blueEntropy + 0;
			else
				blueEntropy = blueEntropy + bluePi*(Math.log(bluePi)/Math.log(2));
			
		}
		double grayMean = temp/numOfPixels;
		/*double redMean = redTemp/numOfPixels;
		double greenMean = greenTemp/numOfPixels;
		double blueMean = blueTemp/numOfPixels;*/
		double mean = ((redTemp+greenTemp+blueTemp)/numOfPixels)/3;
		redEntropy = 0- redEntropy;
		greenEntropy = 0- greenEntropy;
		blueEntropy = 0- blueEntropy;
		grayMeans[count] = grayMean;
		means[count] = mean;
		entropys[count] = (redEntropy+greenEntropy+blueEntropy)/3;
		
		double SD = 0;
		/*double redSD = 0;
		double greenSD = 0;
		double blueSD = 0;*/
		for(int i = 0; i<256; i++)
		{
			SD += Math.pow((i - mean),2)*grayDis[i];
			/*redSD += Math.pow((i - mean),2)*RGBDis[0][i];
			greenSD += Math.pow((i - mean),2)*RGBDis[1][i];
			blueSD += Math.pow((i - mean),2)*RGBDis[2][i];*/
		}
		SD = Math.sqrt((SD)/(width*height));
		/*redSD = Math.sqrt((redSD)/(width*height));
		greenSD = Math.sqrt((greenSD)/(width*height));
		blueSD = Math.sqrt((blueSD)/(width*height));*/
		SDs[count] = SD;
		
	}
	
	public int keyFrame(int shot,int start, int end) 
	{
		int numOfKeys = 0;
		for(int i = start; i< end; i++)
		{
			//System.out.println(meanDiff[i]+"       "+SDDiff[i]);
			if(grayMeanDiff[i]>avgGrayMeanDiff && SDDiff[i]>avgSDDiff)
				numOfKeys++;
		}
		if(debug)
			System.out.println("Shot: "+shot+"    #KeyFrames: "+numOfKeys);
		return numOfKeys;
	}
	
	public int GetMotionVectorDifference(int[][] Ori, int[][] Cur, int width, int hight) {
		int sumDmv = 0;
		Random rd = new Random();
		for (int i = 0; i < 120; i++) {
			int iSample_x = rd.nextInt(width - BLOCK_SIZE);
			int iSample_y = rd.nextInt(hight - BLOCK_SIZE);
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
	
	public int motionVector(int shot,int start, int end)
	{
		int MV = 0;
		for(int i = start; i<end; i++)
		{
			if(MVDiff[i]>avgMVDiff)
				MV++;
		}
		if(debug)
			System.out.println("Shot: "+shot+"    #MV: "+MV);
		return MV;
	}
	
	public int entropyChange(int shot,int start, int end) 
	{
		int numOfFrames = 0;
		for(int i = start; i< end; i++)
		{
			if(entropyDiff[i]>avgEntropyDiff)
				numOfFrames++;
		}
		if(debug)
			System.out.println("Shot: "+shot+"    #entropyChange: "+numOfFrames);
		return numOfFrames;
	}
	
	public int audioValue(int shot, int start, int end) {
		int audio = 0;
		for (int i = start; i < end; i++) {
			if(AudioSums[i] > avgAudioSum) {
				audio++;
			}
		}
		if(debug)
			System.out.println("Shot: "+shot+"    #Audio: "+audio);
		return audio;
	}
	
	public void summarization(String inputVideo, String inputAudio) throws IOException
	{
		readFile(inputVideo,inputAudio);
		readEachFrame(0);
		
		for(int i = 1; i < totalFrame; i++)
		{
			readEachFrame(i);
			meanDiff[i-1] = Math.abs(means[i]-means[i-1]);
			grayMeanDiff[i-1] = Math.abs(grayMeans[i]-grayMeans[i-1]);
			entropyDiff[i-1] = Math.abs(entropys[i]-entropys[i-1]);
			SDDiff[i-1] = Math.abs(SDs[i]-SDs[i-1]);
			avgMeanDiff += meanDiff[i-1];
			avgGrayMeanDiff += grayMeanDiff[i-1];
			avgEntropyDiff += entropyDiff[i-1];
			avgSDDiff += SDDiff[i-1];
			avgMVDiff += MVDiff[i-1];
		}
		avgMeanDiff = avgMeanDiff/(totalFrame-1);
		avgGrayMeanDiff = avgGrayMeanDiff/(totalFrame-1);
		avgEntropyDiff = avgEntropyDiff/(totalFrame-1);
		avgSDDiff = avgSDDiff/(totalFrame-1);
		avgMVDiff = avgMVDiff/(totalFrame-1);
		
		int shot = 0;
		/*int second = 0;
		int preSecond = -1;
		int frameCount = 0;
		int preShot = 0;*/
		int startFrame = 0;
		int endFrame = 0;
		for(int i = 0;i<entropyDiff.length;i++)
		{
			if(meanDiff[i] > avgMeanDiff && entropyDiff[i] > avgEntropyDiff)
			{
				if ((i - startFrame) > 15) { //If shot length is grater than 1 second, then assign a new shot.
					endFrame = i;
					if(debug)
						System.out.println("Shot: "+shot+"    StartFrame: "+startFrame+"    endFrame: "+endFrame);
					
					list.add(new shot(shot,startFrame,endFrame));
					list.getLast().numOfKeyFrames += keyFrame(shot,startFrame,endFrame);
					list.getLast().numOfEntropyChange += entropyChange(shot,startFrame,endFrame);
					list.getLast().MV += motionVector(shot,startFrame,endFrame);
					list.getLast().audio += audioValue(shot,startFrame,endFrame);
					list.getLast().setImportance();
					
					if(debug)
					{
						System.out.println("Shot: "+shot+"    #Importance: "+list.getLast().iImportance);
						System.out.println("-----------------------------------------------------------------");
					}
					//System.out.println("Frame "+(i+1)+" Time: "+second+"s");
					shot++;
					startFrame = i+1;
				}
			}
			//System.out.println(shot+"   "+frameCount);
			
			if(i == entropyDiff.length-1)
			{
				if(debug)
					System.out.println("Shot: "+shot+"    StartFrame: "+startFrame+"    endFrame: "+(totalFrame-1));
				list.add(new shot(shot,startFrame,(totalFrame-1)));
				list.getLast().numOfKeyFrames = keyFrame(shot,startFrame,(totalFrame-1));
				list.getLast().numOfEntropyChange = entropyChange(shot,startFrame,(totalFrame-1));
				list.getLast().MV = motionVector(shot,startFrame,(totalFrame-1));
				list.getLast().audio = audioValue(shot,startFrame,(totalFrame-1));
				list.getLast().setImportance();
				if(debug)
					System.out.println("Shot: "+shot+"    #Importance: "+list.getLast().iImportance);
			}
			
		}
		int length = 0;
		Collections.sort(list);
		for(int i = 0; i<list.size();i++)
		{
			shot temp = list.get(i);
			length += temp.getLength();
			if(debug)
			{
				System.out.println("Shot: "+temp.ishotNumber);
				System.out.println("StartFrame: "+temp.iStartFrame+"    endFrame: "+temp.iEndFrame+"    lenght: "+temp.getLength());
				System.out.println("#KeyFrames: "+temp.numOfKeyFrames+"    #entropyChange: "+temp.numOfEntropyChange+"    #MV: "+temp.MV);
				System.out.println("Importance: "+temp.iImportance);
				System.out.println("------------------------------------------");
			}
			if(summary.size()==0)
				summary.add(temp);
			else
			{
				for(int j = 0; j< summary.size();j++)
				{
					if(temp.ishotNumber < summary.get(j).ishotNumber)
					{
						summary.add(j, temp);
						break;
					}
					else
					{
						if(j == summary.size()-1)
						{
							summary.addLast(temp);
							break;
						}
					}
				}
			}
			if(length>=1350)
				break;
		}
		if(debug)
			System.out.println("length: "+length);
		finalFrames = new int[length];
		int count = 0;
		for(int i = 0; i<summary.size();i++)
		{
			int num = 0;
			shot temp = summary.get(i);
			while(num<temp.iLength)
			{
				finalFrames[count] = temp.iStartFrame+num;
				num++;
				count++;
			}	
		}
		
		if(debug)
		{
			System.out.println(finalFrames.length);
			for(int i = 0; i<finalFrames.length;i++)
			System.out.println(finalFrames[i]);
		}
		genVideo();
		genAudio(inputAudio);
	}
	
	public void genVideo() throws IOException
	{
		
		os  = new FileOutputStream("summary_video.rgb");
		FileChannel fc = input.getChannel();
		for(int i = 0;i<finalFrames.length;i++)
		{
			fc.position((long)(finalFrames[i])* width*height*3);
			writeVideo();
		}
		os.close();
	}
	
	public void writeVideo() throws IOException
	{
		int offset = 0;
		int numRead = 0;
		width = 480;
		height = 270;
		len = width*height*3;
		bytes = new byte[(int)len];
		while (offset < bytes.length && (numRead=input.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		os.write(bytes, 0, bytes.length);
	}
	
	public void genAudio(String inputAudio)
	{
		WavFile wavFile;
		try {
			wavFile = WavFile.openWavFile(new File(inputAudio));
			int numChannels = wavFile.getNumChannels();
			long samplerate = wavFile.getSampleRate();
			int validbit  = wavFile.getValidBits();
			double unit =  ((double)samplerate)/15.0;
			double duration  = unit * (finalFrames.length);
			WavFile output = WavFile.newWavFile(new File("summary.wav"), numChannels, (long)duration, validbit, samplerate);
			double[][] buffer = new double[numChannels][1600];
			for(int i = 0; i< finalFrames.length;i++)
			{
				wavFile.moveToWaveFile(((long)(finalFrames[i])*(long)unit));
				wavFile.readFrames(buffer, 1600);
				output.writeFrames(buffer, 1600);
			}
			wavFile.close();
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WavFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		Summary ren = new Summary();
		// initializes the playSound Object
		ren.summarization(args[0],args[1]);
		ren.initialize("summary_video.rgb","summary.wav");
		ren.playWAV("summary.wav");
		ren.playVid();
	}
	
	private class shot implements Comparable<shot>  {
		private int ishotNumber;
		private int iStartFrame;
		private int iEndFrame;
		private int iLength;
		private int numOfKeyFrames = 0;
		private int numOfEntropyChange = 0;
		private int MV = 0;
		private int audio = 0;
		private double iImportance;
		
		public shot(int shotNumber, int start, int end) {
			this.ishotNumber = shotNumber;
			this.iStartFrame = start;
			this.iEndFrame = end;
			this.iLength = end - start + 1;
			this.iImportance = 0;
		}
		
		public int getLength() {
			return iLength;
		}
		
		public void setImportance() {
			iImportance = (double)numOfKeyFrames/(this.iLength)*2+(double)numOfEntropyChange/(this.iLength)*3
						+(double)MV/(this.iLength)*3+(double)audio/(this.iLength)*2;
		}

		@Override
		public int compareTo(shot o) {
			if(o.iImportance - this.iImportance < 0)
				return -1;
			else if(o.iImportance - this.iImportance > 0)
				return 1;
			else
				return 0;
		}
		
		
	}
}