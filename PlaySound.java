package Summary;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Clip;
/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound extends Thread implements LineListener {

    private InputStream waveStream;
    private boolean bIfPause = true;
    private boolean bIfStop  = false;
    private boolean ends     = false;
    private AudioInputStream audioInputStream = null;
    private Clip audioClip;
    private long frameInd;
   
    /**
     * CONSTRUCTOR
     * @throws PlayWaveException 
     */
    public PlaySound(InputStream waveStream)  {
    	//this.waveStream = waveStream;
    	this.waveStream = new BufferedInputStream(waveStream);
    	try {
			init();
		} catch (PlayWaveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void init() throws PlayWaveException {
    	audioInputStream = null;
		try {
		    audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
		} catch (UnsupportedAudioFileException e1) {
		    throw new PlayWaveException(e1);
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}
		
	
		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(Clip.class, audioFormat);
	
		// opens the audio channel
		
		try {
		   audioClip = (Clip) AudioSystem.getLine(info);
		   audioClip.addLineListener((LineListener) this);
		    audioClip.open(audioInputStream);
		} catch (LineUnavailableException ex) {
            System.out.println("Audio line for playing back is unavailable.");
            ex.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void run() {
    	// Starts the music :P	
		while (!ends) {
			if (bIfPause) {
				audioClip.stop();
				/*if(bIfStop){
					audioClip.setMicrosecondPosition(0);
				}*/
			} else {
				if (!audioClip.isRunning()) {
					long offset = (long)frameInd/100;
					long indx =(long)(frameInd*66+ offset*67);
					audioClip.setMicrosecondPosition(indx*1000);
					audioClip.start();
					long position=audioClip.getMicrosecondPosition();
					//System.out.println(position);
				}
				
				try {
                    Thread.sleep(30);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
			}
		}
		audioClip.close();
	}
    
	public void myPlay(long frameId) {
		bIfPause = false;
		bIfStop  = false;
		frameInd = (long) frameId;
    }
	
	public void myPause() {
		bIfPause = true;
	}
	
	public void myStop() {
		bIfPause = true;
		bIfStop  = true;
	}
	
	public void myEnd() {
		ends = true;
	}
	public boolean IfPause() {
		return bIfPause;
	}
	
	public boolean IfStop() {
		return bIfPause && bIfStop;
	}

	@Override
	public void update(LineEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}