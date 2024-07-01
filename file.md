add following code to your project to implement multiple selection

in getView method in galleryAdapter
        imageView.setOnClickListener(v -> onImageClickListener.onImageClick(imagePath, imageView));
        imageView.setOnLongClickListener(v -> onImageLongClickListener.onImageLongClick(imagePath, imageView));

one of above two lines is there already, only there is slight change

then add these two inside the same class
public interface OnImageClickListener {
        void onImageClick(String imagePath, ImageView imV);
    }
    public interface OnImageLongClickListener{
        boolean onImageLongClick(String imagePath, ImageView imV);
    }

again one of them is already there just a small change

declare these two variables

    private OnImageClickListener onImageClickListener;
    private OnImageLongClickListener onImageLongClickListener;

one must be already there

add these two lines in the constructor

        this.onImageClickListener = (OnImageClickListener) context;
        this.onImageLongClickListener = (OnImageLongClickListener) context;

one is there but a slight change

also remove the third argument from the constructor
as a consequence also remove it from main activity onCreate function where new galleryAdapter is created

implement the other interface in main activity like so

public class MainActivity extends AppCompatActivity implements GalleryAdapter.OnImageClickListener, GalleryAdapter.OnImageLongClickListener

create 2 new variables in the main activity 

    public boolean stateSelection = false;
    public Set<String> selected = new HashSet<String>();

finally these 2 functions inside the main activity

    @Override
    public void onImageClick(String imagePath, ImageView imV) {
        if(stateSelection){
            if(selected.contains(imagePath)){
                selected.remove(imagePath);
                imV.setBackgroundColor(Color.TRANSPARENT);
                imV.setColorFilter(null);
                if(selected.isEmpty()){
                    stateSelection = false;
                }
            }else{
                selected.add(imagePath);
                imV.setBackgroundColor(Color.parseColor("#AA0000FF"));
                imV.setColorFilter(Color.argb(150,0,0,0));
            }
            return;
        }
        Toast.makeText(this, "Clicked on image: " + imagePath, Toast.LENGTH_SHORT).show();

        Intent openPhoto = new Intent(this, image_view.class);
        openPhoto.putExtra("filePath", imagePath);
        startActivity(openPhoto);
    }
    @Override
    public boolean onImageLongClick(String imagePath, ImageView imV) {
        Log.d("samsung", "long clicked "+imagePath);
        stateSelection = true;
        if(selected.contains(imagePath)){
            selected.remove(imagePath);
            imV.setBackgroundColor(Color.TRANSPARENT);
            imV.setColorFilter(null);
            if(selected.isEmpty()){
                stateSelection = false;
            }
        }else{
            selected.add(imagePath);
            imV.setBackgroundColor(Color.parseColor("#AA0000FF"));
            imV.setColorFilter(Color.argb(150,0,0,0));
        }
        return true;
    }


one function is already there with changes

thanking you
me
