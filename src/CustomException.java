import static org.lwjgl.openal.AL10.*;

public class CustomException extends RuntimeException {
	
	CustomException(int errorCode) {
		super("Internal " + (errorCode == AL_INVALID_NAME ? : errorCode == AL_INVALID_ENUM ? "invalid enum" : errorCode == AL_INVALID_VALUE 
				? "invalid value" : errorCode == AL_INVALID_OPERATION ? "invalid operation" : "unknown") + " OpenAL exception.");
	}
}
