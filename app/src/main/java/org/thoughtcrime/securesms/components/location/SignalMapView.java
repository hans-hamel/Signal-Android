package org.thoughtcrime.securesms.components.location;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.omh.android.maps.api.factories.OmhMapProvider;
import com.omh.android.maps.api.presentation.interfaces.maps.OmhMapView;
import com.omh.android.maps.api.presentation.models.OmhCoordinate;
import com.omh.android.maps.api.presentation.models.OmhMarkerOptions;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.concurrent.ListenableFuture;
import org.thoughtcrime.securesms.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;

public class SignalMapView extends LinearLayout {

  private FrameLayout frameLayout;
  private ImageView imageView;
  private TextView  textView;

  public SignalMapView(Context context) {
    this(context, null);
  }

  public SignalMapView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public SignalMapView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  private void initialize(Context context) {
    setOrientation(LinearLayout.VERTICAL);
    LayoutInflater.from(context).inflate(R.layout.signal_map_view, this, true);

    this.frameLayout = findViewById(R.id.map_view_container);
    this.imageView = findViewById(R.id.image_view);
    this.textView  = findViewById(R.id.address_view);
  }

  public ListenableFuture<Bitmap> display(final SignalPlace place) {
    final SettableFuture<Bitmap> future = new SettableFuture<>();

    this.imageView.setVisibility(View.GONE);
    this.textView.setText(place.getDescription());
    snapshot(place, frameLayout).addListener(new ListenableFuture.Listener<>() {
      @Override
      public void onSuccess(Bitmap result) {
        future.set(result);
        imageView.setImageBitmap(result);
        imageView.setVisibility(View.VISIBLE);
      }

      @Override
      public void onFailure(ExecutionException e) {
        future.setException(e);
      }
    });

    return future;
  }

  public static ListenableFuture<Bitmap> snapshot(final OmhCoordinate place, @NonNull final FrameLayout frameLayout) {
    final SettableFuture<Bitmap> future = new SettableFuture<>();
    final OmhMapView omhMapView = OmhMapProvider.getInstance().provideOmhMapView(frameLayout.getContext());

    omhMapView.onCreate(null);
    omhMapView.onStart();
    omhMapView.onResume();
    frameLayout.addView(omhMapView.getView());
    frameLayout.setVisibility(View.VISIBLE);

    omhMapView.getMapAsync(omhMap -> {
      OmhMarkerOptions omhMarkerOptions = new OmhMarkerOptions();
      omhMarkerOptions.setPosition(place);
      omhMap.moveCamera(place, 13);
      omhMap.addMarker(omhMarkerOptions);
      omhMap.setOnMapLoadedCallback(() -> omhMap.snapshot(bitmap -> {
        future.set(bitmap);
        frameLayout.setVisibility(View.GONE);
        omhMapView.onPause();
        omhMapView.onStop();
        omhMapView.onDestroy();
      }));
    });

    return future;
  }

  public static ListenableFuture<Bitmap> snapshot(final SignalPlace place, @NonNull final FrameLayout frameLayout) {
    return snapshot(place.getOmhCoordinate(), frameLayout);
  }

}
