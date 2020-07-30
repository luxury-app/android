package nz.shelto.luxury;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        final TextView uvTextView = findViewById(R.id.uvTextView);
        final int uvIndex = (int)getIntent().getLongExtra("uv_index", 1);
        uvTextView.setText(String.format("%d", uvIndex));
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMin(0);
        progressBar.setMax(11);
        progressBar.setProgress(uvIndex);
    }
}