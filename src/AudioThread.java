import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;

//import utils.Utils;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

import java.util.function.Supplier;

public class AudioThread extends Thread{
	static final int BUFFER_SIZE = 512;
	static final int BUFFER_COUNT = 0;
	
	private final Supplier<short[]> bufferSupplier;
	
	private final int[] buffers = new int[BUFFER_COUNT];
	private final long device = alcOpenDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));
	private final long context = alcCreateContext(device, new int[1]);
	private final int source;
	
	private int bufferIndex;
	
	private boolean closed;
	private boolean running;
	
	AudioThread(Supplier<short[]> bufferSupplier) {
		this.bufferSupplier = bufferSupplier;
		alcMakeContextCurrent(context);
		AL.createCapabilities(ALC.createCapabilities(device));
		source = alGenSources();
		
		for (int i = 0; i < BUFFER_COUNT; ++i) {
			//buffer samples
			bufferSamples(new short[0]);
		}
		alSourcePlay(source);
		// catch internal exceptions
		catchInternalException();
		start();
	}
	
	@Override
	public synchronized void run() {
		while (!closed) {
			while (!running) {
				try {
					wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int processedBufs = alGetSourcei(source, AL_BUFFERS_PROCESSED);
			for (int i = 0; i < processedBufs; ++i) {
				short[] samples = bufferSupplier.get();
					if (samples == null) {
						running = false;
						break;
					}

				alDeleteBuffers(alSourceUnqueueBuffers(source));
				buffers[bufferIndex] = alGenBuffers();
				bufferSamples(samples);
				
			}
			if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
				alSourcePlay(source);
			}
			catchInternalException();
		}
		alDeleteSources(source);
		alDeleteBuffers(buffers);
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
	synchronized void triggerPlayback() {
		running = true;
		notify();
	}
	
	void close() {
		closed = true;
		triggerPlayback();
	}
	
	private void bufferSamples(short[] samples) {
		int buf = buffers[++bufferIndex];
		alBufferData(buf, AL_FORMAT_MONO16, samples, Synth.AudioInfo.SAMPLE_RATE);
		alSourceQueueBuffers(source, buf);
		bufferIndex %= BUFFER_COUNT; 
	}
	
	private void catchInternalException() {
		int err = alcGetError(device);
		if (err != ALC_NO_ERROR) {
			throw new CustomException(err);
		}
	}
	

}
