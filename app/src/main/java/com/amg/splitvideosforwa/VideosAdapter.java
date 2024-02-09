package com.amg.splitvideosforwa;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/* loaded from: classes.dex */
public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<VideoFile> files;
    List<VideoFile> selected_files;
    public VideosAdapter(List<VideoFile> files,List<VideoFile> selected_files, Context context) {
        this.files = files;
        this.selected_files = selected_files;
        this.context = context;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder audioViewHolder = (VideoViewHolder) holder;
        final VideoFile videoFile = this.files.get(position);
        audioViewHolder.name.setText(videoFile.getName());
        audioViewHolder.lastModified.setText(videoFile.getLastModification());
        audioViewHolder.size.setText(videoFile.getSize());
        audioViewHolder.share.setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.VideosAdapter.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Uri fromFile;
                Intent intent = new Intent("android.intent.action.SEND");
                Context applicationContext = VideosAdapter.this.context.getApplicationContext();
                fromFile = FileProvider.getUriForFile(applicationContext, VideosAdapter.this.context.getPackageName() + ".provider", videoFile.getFile());
                intent.setType("video/");
                intent.putExtra("android.intent.extra.STREAM", fromFile);
                VideosAdapter.this.context.startActivity(Intent.createChooser(intent, "Share audio"));
            }
        });
        audioViewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                files.remove(videoFile);
                videoFile.getFile().delete();
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, files.size());
            }
        });
        audioViewHolder.layout.setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.VideosAdapter.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp4");
                    Uri uriForFile = FileProvider.getUriForFile(VideosAdapter.this.context, "com.amg.splitvideosforwa.provider", videoFile.getFile());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uriForFile, mimeTypeFromExtension);
                    VideosAdapter.this.context.startActivity(intent);
                } catch (Exception e) {
                    Log.e(e.toString(), e.getMessage());
                }
            }
        });


        if(selected_files.contains(files.get(position)))
            audioViewHolder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_selected_state));
        else
            audioViewHolder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.list_item_normal_state));
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.files.size();
    }

    /* loaded from: classes.dex */
    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        public TextView lastModified;
        public LinearLayout layout;
        public TextView name;
        public ImageButton share,delete;
        public TextView size;

        public VideoViewHolder(View itemView) {
            super(itemView);
            this.layout = (LinearLayout) itemView.findViewById(R.id.audio_layout);
            this.name = (TextView) itemView.findViewById(R.id.title);
            this.size = (TextView) itemView.findViewById(R.id.size);
            this.share = (ImageButton) itemView.findViewById(R.id.share);
            this.delete = (ImageButton)itemView.findViewById(R.id.delete);
            this.lastModified = (TextView) itemView.findViewById(R.id.modification);
        }
    }
}
