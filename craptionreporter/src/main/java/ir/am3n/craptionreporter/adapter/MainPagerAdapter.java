package ir.am3n.craptionreporter.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ir.am3n.craptionreporter.ui.ExceptionLogFragment;
import ir.am3n.craptionreporter.ui.CrashLogFragment;
import ir.am3n.craptionreporter.ui.LogFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private LogFragment logFragment;
    private CrashLogFragment crashLogFragment;
    private ExceptionLogFragment exceptionLogFragment;
    private String[] titles;

    public MainPagerAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return crashLogFragment = new CrashLogFragment();
        } else if (position == 1) {
            return exceptionLogFragment = new ExceptionLogFragment();
        } else if (position == 2) {
            return logFragment = new LogFragment();
        } else {
            return new CrashLogFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    public void clearLogs() {
        logFragment.clearLog();
        crashLogFragment.clearLog();
        exceptionLogFragment.clearLog();
    }
}