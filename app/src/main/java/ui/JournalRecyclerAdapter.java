package ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private static final String TAG ="TAG" ;
    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.journal_row,viewGroup,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder viewHolder, int position) {

        Journal journal=journalList.get(position);
        String imageUrl;

        viewHolder.title.setText(journal.getTitle());
        viewHolder.thoughts.setText(journal.getThought());
//        viewHolder.dateAdded.setText(journal.getTimeAdded().toString());
        viewHolder.name.setText(journal.getUserName());
        imageUrl=journal.getImageUrl();
        /*Using picasso library to download and show image*/
        Picasso.get().load(imageUrl).placeholder(R.drawable.photo1).fit().into(viewHolder.image);

        //1 hr ago....
        String timeAgo= (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds()*1000);
        viewHolder.dateAdded.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title,thoughts,dateAdded,name;
        public ImageView image;
        public ImageButton shareButton;
        String userId;
        String username;

        public ViewHolder(@NonNull View itemView,Context ctx) {
            super(itemView);
            context =ctx;

            title=itemView.findViewById(R.id.journal_title_list);
            thoughts=itemView.findViewById(R.id.journal_thoughts_list);
            dateAdded=itemView.findViewById(R.id.journal_timestamp);
            image=itemView.findViewById(R.id.journal_image_list);
            name=itemView.findViewById(R.id.journal_row_username);
            shareButton=itemView.findViewById(R.id.journal_row_shareButton);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    context.startActivity();
                    int position = getAdapterPosition();

                    Journal journal = journalList.get(position);

                    String imageUrl = journal.getImageUrl();

//                    shareImage(imageUrl, context, position, journalList);
                }
            });
        }
    }

    private void shareImage(String url, Context context, int position, List<Journal> journalList) {

        Picasso.get().load(url)
                .resize(675,405)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent shareIntent= new Intent((Intent.ACTION_SEND));
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"From Journal App");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, journalList.get(position).getTitle()
                                + "\n\n" + journalList.get(position).getThought());

                        shareIntent.setType("image/*");

                        Uri imageUri = getLocalBitmapUri(bitmap, context);

                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                        Log.d("TAG", "onBitmapLoaded: " + imageUri.toString());
                        context.startActivity(Intent.createChooser(shareIntent, "Send Image"));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

    }

    private Uri getLocalBitmapUri(Bitmap bitmap, Context context) {
        Uri bmpUri = null;
//        try {
//            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//                    "share_image_" + System.currentTimeMillis() + ".png");
//            FileOutputStream out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 10, out);
//            out.close();
//            bmpUri = Uri.fromFile(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bmpUri;

        try {
            Log.d(TAG, "External files: " + this.context.getExternalFilesDir(null).toString());

            File imagePath = new File(context.getExternalFilesDir(null), "tempImages");
            if (!imagePath.exists()) {
                boolean hasBeenCreated = imagePath.mkdirs();
                Log.d(TAG, "imagePath does exist. Was it created? " + hasBeenCreated);
            }
            File newFile = new File(imagePath,
                    "share_image_" + System.currentTimeMillis() + ".png");

            FileOutputStream out = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            String authorities = context.getPackageName() + ".fileprovider";
            bmpUri = FileProvider.getUriForFile(context, authorities, newFile); // Uri.fromFile(file);
            out.close();

        } catch (IOException e) {
            Log.d(TAG, "getLocalBitmapUri:      " + e.getMessage());
            e.printStackTrace();
        }
        return bmpUri;
    }
}
