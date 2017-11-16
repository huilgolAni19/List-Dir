package net.vrllogistics.hikvision.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.vrllogistics.hikvision.R;
import net.vrllogistics.hikvision.activities.VideoPlayActivity;
import net.vrllogistics.hikvision.utility.MFile;

import java.util.List;

/**
 * Created by Anirudh on 06/06/17.
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>{

    List<MFile> mFiles;
    Context context;

    public RecycleViewAdapter(List<MFile> mFiles, Context context) {
        this.mFiles = mFiles;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_file,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final MFile mFile = mFiles.get(position);
        holder.thumbnail.setImageBitmap(mFile.getThumbnail());
        holder.textViewFileName.setText(mFile.getName());
        holder.textViewFileSize.setText("Size: "+mFile.getFileSize());
        holder.textViewVideoDuration.setText("Duration: "+mFile.getDuration());
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("FilePath",mFile.getFilePath());
                Intent ii = new Intent(context.getApplicationContext(),VideoPlayActivity.class);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // To startActivity for out side
                ii.putExtras(bundle);
                context.startActivity(ii);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout parentLayout;
        ImageView thumbnail;
        TextView textViewFileName,textViewFileSize,textViewVideoDuration;
        public MyViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            textViewFileName = (TextView) itemView.findViewById(R.id.textViewFileName);
            textViewVideoDuration = (TextView) itemView.findViewById(R.id.textViewVideoDuration);
            textViewFileSize = (TextView) itemView.findViewById(R.id.textViewFileSize);
        }
    }
}
