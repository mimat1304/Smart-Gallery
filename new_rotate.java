import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

public class FaceAlignment {

    // Configure the face detector
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .build();
    FaceDetector detector = FaceDetection.getClient(options);

    // Detect faces and get Euler angles
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

    // Rotate bounds and crop the face using roll angle
    private Bitmap alignFaceUsingEulerAngle(Bitmap bitmap, Face face) {
        // Get the roll angle in degrees
        float rollAngle = face.getHeadEulerAngleZ();

        // Get the bounding box of the face
        Rect bounds = face.getBoundingBox();

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.setRotate(-rollAngle, bounds.centerX(), bounds.centerY());

        // Create a new bitmap with the same size as the original
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        // Draw the original bitmap onto the new bitmap using the rotation matrix
        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.drawBitmap(bitmap, matrix, new Paint());

        // Crop the face using the adjusted bounding box
        int left = Math.max(0, bounds.left);
        int top = Math.max(0, bounds.top);
        int right = Math.min(rotatedBitmap.getWidth(), bounds.right);
        int bottom = Math.min(rotatedBitmap.getHeight(), bounds.bottom);

        return Bitmap.createBitmap(rotatedBitmap, left, top, right - left, bottom - top);
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
