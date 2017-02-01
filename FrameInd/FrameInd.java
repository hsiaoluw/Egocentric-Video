package FrameInd;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;


public class FrameInd {

	
	int width, ref_width ;
    int height,ref_height; 
	byte[] bytes, ref_bytes, ref_bytes_ori;
	int whole_pixels;
	long len, len_ref ;
	static FileInputStream is, is2;
	OutputStream os ;
	static PlaySound sound;
	int iFrameCnt=0;
	int rs  = 4;
	int num_bin = 256>>rs;
	int [][] reference= new int[3][num_bin]; 
	int [][] scanned  = new int[3][num_bin];
	int difference=0;
	int find_indx=-1;
	int BlockSize=10;
	int searchWindow = 10;
	int min_dist_diff=9999999;
	int min_dist_frame =-1;
	int frame_interval =5;
	int max_framecnt=4499;
	double x_ratio=1;
	double y_ratio=1;
	int begin, end;
	public void read_one_img(int type){ // type=0 scann image, type=1 copy to output file , type=2 set reference 
		int offset = 0;
		int numRead = 0;
		try{
			
			if(type==0 || type ==1){
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}}
			if(type== 2){
				while (offset < ref_bytes_ori.length && (numRead=is2.read(ref_bytes_ori, offset, ref_bytes_ori.length-offset)) >= 0) {
					offset += numRead;
				}
			 //subsample
				int indx=0;
				
				for(int y=0;y<height; y++){
					for(int x=0;x<width;x++){
					   ref_bytes[indx] =                ref_bytes_ori[(int)(y*y_ratio*ref_width+ x*x_ratio)]; 
					   ref_bytes[indx+height*width] =   ref_bytes_ori[(int) (((int)(y*y_ratio*ref_width+ x*x_ratio))+ref_width*ref_height)];
					   ref_bytes[indx+2*height*width] = ref_bytes_ori[(int) (((int)(y*y_ratio*ref_width+ x*x_ratio))+ref_width*ref_height*2)];
							   indx++;
					}
				}
			}
			int ind = 0;
			for(int y = 0; y < height; y++){
	
				for(int x = 0; x < width; x++){
					if(type==0 || type==1){
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
					//int Yscan = ( (  76 *   ((int)(r&0xff)) + 150 *   ((int)(g&0xff)) +  29 *    ((int)(b&0xff)) ) >> 8) ;
					//cost+= Math.abs(Yref-Yscan);
					
					if(type==0){
						//scanned[0][Yscan>>rs]++;
					scanned[0][((int)(r&0xff))>>rs]++;
					scanned[1][((int)(g&0xff))>>rs]++;
					scanned[2][((int)(b&0xff))>>rs]++;
						}}
					if(type==2){
						byte r = ref_bytes[ind];
					    byte g = ref_bytes[ind+height*width];
						byte b = ref_bytes[ind+height*width*2]; 
					    //int Yref = ( (  76 *    ((int)(r&0xff))  + 150 *   ((int)(g&0xff)) +  29 *     ((int)(b&0xff)) ) >> 8) ;
						//reference[0][Yref>>rs]++;
						reference[0][((int)(r&0xff))>>rs]++;
						reference[1][((int)(g&0xff))>>rs]++;
						reference[2][((int)(b&0xff))>>rs]++;
						
						
					}
					ind++;
				}
			}
			
			if(type==1){
			os.write(bytes, 0, bytes.length); }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		difference=0;
		if(type==2){
			for(int i=0;i<3;i++){
				for(int j=0;j<num_bin;j++){
					
					System.out.println(reference[i][j]);
				}
			}
			
		}
		
		if(type==0){
		for(int i=0;i<3;i++){
			for(int j=0;j<num_bin;j++){
				difference+=Math.abs (scanned[i][j] - reference[i][j]);
				//if(iFrameCnt==310)System.out.println(scanned[i][j]+" "+reference[i][j]);
				scanned[i][j]=0;
			}
		}}
	}
	public void initialize(String[] args){
		 width = 480;height = 270;iFrameCnt =0; whole_pixels = width*height;
		 ref_width = 1280 ;
		 ref_height = 720;
		 if(args.length > 3) {
			 ref_width =  Integer.parseInt(args[3]);
			 ref_height = Integer.parseInt(args[4]);
		 }
		 x_ratio = (double) ref_width/ (double) width;
		 y_ratio = (double) ref_height/ (double) height;
		 
		try {
			File file = new File(args[0]);
			is = new FileInputStream(file);
			len = width*height*3;
			len_ref = ref_width* ref_height*3;
			bytes = new byte[(int)len];
			ref_bytes = new byte[(int)len];
			ref_bytes_ori = new byte[(int)len_ref];
			File file2 = new File(args[2]);
			is2 = new FileInputStream(file2);
			
			for(int i=0;i<3;i++){
				for(int j=0;j<num_bin;j++){
					reference[i][j]=0;
					scanned[i][j]=0;
				}
			}
			read_one_img(2);
			
			FileChannel     fc = is.getChannel();
			max_framecnt=(int)fc.size()/bytes.length;
		
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
		
    public void check_one_block(int ori_x, int ori_y, int windowsize){
    	int min_cost = 999999;
    	int cost =0;
    	int ind =0;
    	for(int i= ori_x-windowsize;i< ori_x+windowsize; i++ ){
    		for(int j= ori_y-windowsize; j< ori_y+windowsize; j++){
    			cost=0;
    			ind = j*width+i;
    			for(int y=j; y<j+BlockSize; y++){
    			for(int x= i; x<i+BlockSize; x++){
    				    ind = y*width+x;
    					int Yref =  ( (  76 * ref_bytes[ind] + 150 *ref_bytes[ind+whole_pixels] +  29 * ref_bytes[ind+whole_pixels*2] ) >> 8) ;
    					int Yscan = ( (  76 *     bytes[ind] + 150 *    bytes[ind+whole_pixels] +  29 *     bytes[ind+whole_pixels*2] ) >> 8) ;
    					cost+= Math.abs(Yref-Yscan);
    					if(cost>min_cost) break;
    			  }
    			  if(cost>min_cost)  break;
    			}
    			
    			if(cost<min_cost) min_cost =cost;
    		}
    	}
    	
    	difference =min_cost;
    }
		
	
	public void ScanVid(){
		int min=999999;
		while(iFrameCnt<max_framecnt-1){
			read_one_img(0);
			try{
			FileChannel     fc = is.getChannel();
			if(iFrameCnt>0) {fc.position((long)(iFrameCnt)* bytes.length);}
			iFrameCnt+=frame_interval;
			}
			catch (IOException e1) {
                max_framecnt=iFrameCnt-frame_interval;
				e1.printStackTrace();
			}
		//	System.out.println(difference+" whole pixels: "+whole_pixels/4);
			if(min_dist_diff > difference) {min_dist_diff= difference; min_dist_frame = iFrameCnt-frame_interval; }
			if(difference<(whole_pixels/2)) { //level 1 check, check histogram
				// level 2 check ,  check central block difference
				check_one_block(width/2, height/2,20);
				
				if(difference< BlockSize*BlockSize*10){
					find_indx = iFrameCnt;
					System.out.println("time: "+(float)iFrameCnt/15.0);
					if(min>difference){ min=difference;	System.out.println(difference+" min "+iFrameCnt);}
					min_dist_diff= difference; min_dist_frame = iFrameCnt-frame_interval;
					break;
				}
			}
		}
		int seconds = (int) Math.floor((float)(min_dist_frame/15));
		int minutes = (int) Math.floor((float)(seconds/60)); 
		System.out.println("difference:"+min_dist_diff+" , min at frame: "+min_dist_frame+ ", most possible time: "+ minutes +":"+ (seconds%60)/10 +""+ (seconds%60)%10);
	}
	
	public void gen_Vid(){
		
		begin = min_dist_frame -frame_interval*8;  if(begin<0) begin=0;
		end   = min_dist_frame + frame_interval*7; if(end>max_framecnt) end=max_framecnt;
		try {
			os  = new FileOutputStream("query_video.rgb");
			FileChannel     fc = is.getChannel();
			fc.position((long)(begin)* bytes.length);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		int search_ind = begin;
		while(search_ind<end){
			read_one_img(1);// read from origianal video and write to query_video.rgb 
			search_ind++;
		}
	}
	public void gen_Aud(String[] args){
		// Open the wav file specified as the first argument
					WavFile wavFile;
					try {
						wavFile = WavFile.openWavFile(new File(args[1]));
							
						// Display information about the wav file
						wavFile.display();	// Get the number of audio channels in the wav file
						int numChannels = wavFile.getNumChannels();
						long samplerate = wavFile.getSampleRate();
						int validbit  = wavFile.getValidBits();
						double unit =  ((double)samplerate)/15.0;
						double duration  = unit * (end- begin+1);
						WavFile wavFile_write = WavFile.newWavFile(new File("query.wav"), numChannels, (long)duration, validbit, samplerate);
						// Create a buffer of 100 frames
						double[][] buffer = new double[numChannels][1600];
							
						wavFile.moveToWaveFile(((long)begin*(long)unit));
							
						for(int i=0;i<= end-begin;i++){
						// Read frames into buffer
								int	framesRead  = wavFile.readFrames(buffer, 1600);
								int framesWrite = wavFile_write.writeFrames(buffer, 1600);
						}
							
						 wavFile.close();
						 wavFile_write.close();
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (WavFileException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

	}
   
	public static void main(String[] args) {
		if (args.length < 3) {
		    System.err.println("usage: java -jar AVPlayer.jar [RGB file] [WAV file] [reference file] [reference width(optional)] [reference height(optional)]");
		    return;
		}
		
		FrameInd ren = new FrameInd();
		// initializes
		ren.initialize(args);
		ren.ScanVid();
		ren.gen_Vid();
		ren.gen_Aud(args);
		
		AVPlayer query = new AVPlayer();
		query.initialize("query_video.rgb", "query.wav");
		query.playWAV("query.wav");
		query.playVid();
		
	}
}
