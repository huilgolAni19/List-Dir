package net.vrllogistics.hikvision.activities;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import net.vrllogistics.hikvision.R;
import net.vrllogistics.hikvision.adapters.RecycleViewAdapter;
import net.vrllogistics.hikvision.utility.MFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView imageViewHeader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        initCollapsingToolbar();
        init();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        new createData().execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    public List<MFile> createData() {
        List<MFile> mFileList = new ArrayList<>();

        String path = Environment.getExternalStorageDirectory().toString()+"/HikVision";

        File directory = new File(path);
        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {
            Log.e("Files", "FileName:" + files[i].getName());
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(files[i].getPath(),
                    MediaStore.Images.Thumbnails.MINI_KIND);
            MFile mFile = new MFile();
            mFile.setFilePath(files[i].getPath());
            mFile.setThumbnail(bitmap);
            mFile.setName(files[i].getName());
            float bytes = files[i].length();
            float kilobytes = (bytes / 1024);
            float megabytes = (kilobytes / 1024);

            String size;
            if (megabytes > 1024) {
                float gb = (megabytes / 1024);
                size =  String.format("%.2f",gb)+" GB";
            } else {
                size  =  String.format("%.2f",megabytes)+" MB";
            }
            mFile.setFileSize(size);
            //String videoDuration = files[i]
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(files[i].getPath());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInmillisec = Long.parseLong( time );
            long duration = timeInmillisec / 1000;
            long hours = duration / 3600;
            long minutes = (duration - hours * 3600) / 60;
            long seconds = duration - (hours * 3600 + minutes * 60);
            String strDuration;
            if (seconds < 10) {
                strDuration = hours+":"+minutes+":0"+seconds;
            } else {
                strDuration = hours+":"+minutes+":"+seconds;
            }

            mFile.setDuration(strDuration);
            mFileList.add(mFile);
        }
        imageViewHeader.setImageBitmap(mFileList.get(mFileList.size() - 1).getThumbnail());
        return mFileList;
    }

  void init() {
       imageViewHeader = (ImageView) findViewById(R.id.backdrop);
        recyclerView = (RecyclerView) findViewById(R.id.recycleViewVideoList);
//        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        RecycleViewAdapter adapter = new RecycleViewAdapter(createData(),VideoListActivity.this);
//        recyclerView.setAdapter(adapter);
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    class createData extends AsyncTask<Void,List<MFile>,Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(VideoListActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Loading files....");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<MFile> mFileList = new ArrayList<>();

            String path = Environment.getExternalStorageDirectory().toString()+"/HikVision";

            File directory = new File(path);
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                Log.e("Files", "FileName:" + files[i].getName());
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(files[i].getPath(),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                MFile mFile = new MFile();
                mFile.setFilePath(files[i].getPath());
                mFile.setThumbnail(bitmap);
                mFile.setName(files[i].getName());
                float bytes = files[i].length();
                float kilobytes = (bytes / 1024);
                float megabytes = (kilobytes / 1024);

                String size;
                if (megabytes > 1024) {
                    float gb = (megabytes / 1024);
                    size =  String.format("%.2f",gb)+" GB";
                } else {
                    size  =  String.format("%.2f",megabytes)+" MB";
                }
                mFile.setFileSize(size);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(files[i].getPath());
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInmillisec = Long.parseLong( time );
                long duration = timeInmillisec / 1000;
                long hours = duration / 3600;
                long minutes = (duration - hours * 3600) / 60;
                long seconds = duration - (hours * 3600 + minutes * 60);
                String strDuration;
                if (seconds < 10) {
                    strDuration = hours+":"+minutes+":0"+seconds;
                } else {
                    strDuration = hours+":"+minutes+":"+seconds;
                }

                mFile.setDuration(strDuration);
                mFileList.add(mFile);
            }

            publishProgress(mFileList);
            return null;
        }

        @Override
        protected void onProgressUpdate(List<MFile>... values) {
            super.onProgressUpdate(values);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(VideoListActivity.this, 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            RecycleViewAdapter adapter = new RecycleViewAdapter(values[0],VideoListActivity.this);
            recyclerView.setAdapter(adapter);
            imageViewHeader.setImageBitmap(values[0].get(values[0].size()-1).getThumbnail());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

}
