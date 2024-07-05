    private Bitmap alignFaceUsingEulerAngle(Bitmap bitmap, Face face) {
        // Get the roll angle in degrees
        float rollAngle = face.getHeadEulerAngleZ();

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postRotate(-rollAngle, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);

        // Recreate the new bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }









//complete if u wish to see

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

public class FaceAlignment {

    // Step 1: Configure the face detector
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .build();
    FaceDetector detector = FaceDetection.getClient(options);

    // Step 2: Detect faces and get Euler angles
    public void detectAndAlignFace(InputImage image, Bitmap bitmap, FaceAlignmentCallback callback) {
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        Face face = faces.get(0);
                        Bitmap alignedBitmap = alignFaceUsingEulerAngle(bitmap, face);
                        callback.onFaceAligned(alignedBitmap);
                    } else {
                        callback.onFaceAligned(null);
                    }
                })
                .addOnFailureListener(e -> callback.onFaceAligned(null));
    }

    // Step 3: Align face using roll angle
    private Bitmap alignFaceUsingEulerAngle(Bitmap bitmap, Face face) {
        // Get the roll angle in degrees
        float rollAngle = face.getHeadEulerAngleZ();

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postRotate(-rollAngle, bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);

        // Recreate the new bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Callback interface for face alignment
    public interface FaceAlignmentCallback {
        void onFaceAligned(Bitmap alignedBitmap);
    }

    // Example usage
    public void exampleUsage(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        detectAndAlignFace(inputImage, bitmap, alignedBitmap -> {
            if (alignedBitmap != null) {
                // Pass the alignedBitmap to the TFLite model
            }
        });
    }
}
