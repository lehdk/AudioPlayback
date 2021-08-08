import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Main {

    private static boolean shouldRun = false;
    
    public static void main(String[] args) {
        System.out.println("Launching Audio");
        
        try {
            AudioFormat format = getAudioFormat();

            // Setting up input audio
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            if(!AudioSystem.isLineSupported(targetInfo)) {
                System.err.println("Target line not supported!");
                System.exit(-1);
            }

            final TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open();
            System.out.println("TargetLine opened.");
            targetLine.start();

            // Setting up output audio
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
            if(!AudioSystem.isLineSupported(sourceInfo)) {
                System.err.println("Source line not supported!");
                System.exit(-1);
            }
            
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceDataLine.open();
            sourceDataLine.start();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    AudioInputStream audioInputStream = new AudioInputStream(targetLine);
                    
                    // TEST
                    final int BUFFER_SIZE = 4096;
                    byte[] samples = new byte[BUFFER_SIZE];
                    try {
                        shouldRun = true;
                        while (shouldRun) {
                            sourceDataLine.write(samples, 0, audioInputStream.read(samples, 0, BUFFER_SIZE));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            System.out.println("Thread started.");

            Scanner scanner = new Scanner(System.in);
            String msg = scanner.nextLine();
            scanner.close();
            
            shouldRun = false;
            
            sourceDataLine.stop();
            targetLine.stop();
            System.out.println("Stopped");

            sourceDataLine.close();
            targetLine.close();

        } catch (LineUnavailableException lue) {
            lue.printStackTrace();
        }

    }

    private static AudioFormat getAudioFormat() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
    }

}