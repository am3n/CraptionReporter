package ir.am3n.craptionreporter.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import ir.am3n.craptionreporter.CrashReporter;
import ir.am3n.craptionreporter.R;
import ir.am3n.craptionreporter.adapter.MainPagerAdapter;
import ir.am3n.craptionreporter.utils.Constants;
import ir.am3n.craptionreporter.utils.CrashUtil;
import ir.am3n.craptionreporter.utils.FileUtils;
import ir.am3n.craptionreporter.utils.SimplePageChangeListener;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

public class CrashReporterActivity extends AppCompatActivity {

    private MainPagerAdapter mainPagerAdapter;
    private int selectedTabPosition = 0;

    //region activity callbacks
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.log_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_crash_logs) {
            clearCrashLog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_reporter_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.crash_reporter));
        toolbar.setSubtitle(getApplicationName());
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
    //endregion

    private void clearCrashLog() {
        new Thread(() -> {
            String crashReportPath = TextUtils.isEmpty(CrashReporter.getInstance().getCrashReportPath()) ?
                    CrashUtil.getDefaultCrashPath() : CrashReporter.getInstance().getCrashReportPath();
            File[] logs = new File(crashReportPath).listFiles();
            for (File file : logs) {
                FileUtils.delete(file);
            }


            String logReportPath = TextUtils.isEmpty(CrashReporter.getInstance().getLogReportPath()) ?
                    CrashUtil.getDefaultLogPath() : CrashReporter.getInstance().getLogReportPath();
            String filename = "logReports.txt";
            File file = new File(logReportPath + File.separator + filename);
            if (file.exists())
                file.delete();


            runOnUiThread(() -> mainPagerAdapter.clearLogs());

        }).start();
    }

    private void clearLog() {

    }

    private void setupViewPager(ViewPager viewPager) {
        String[] titles = {getString(R.string.crashes), getString(R.string.exceptions), getString(R.string.logs)};
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), titles);
        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        viewPager.addOnPageChangeListener(new SimplePageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTabPosition = position;
            }
        });

        Intent intent = getIntent();
        if (intent != null && !intent.getBooleanExtra(Constants.LANDING, false)) {
            selectedTabPosition = 1;
        }
        viewPager.setCurrentItem(selectedTabPosition);
    }

    private String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }

}
