
package recordviewer.accela.com.myapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int section = args.getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            if (section==1) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
            }else if (section==2){
                rootView = inflater.inflate(R.layout.fragment_two, container, false);
            }else if (section==3){
                rootView = inflater.inflate(R.layout.fragment_three, container, false);
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycleId);
//                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                HeaderAndFooterWrapper adapterWrapper = new HeaderAndFooterWrapper(getContext(), new MyAdapter(getContext()));

                View header = inflater.inflate(R.layout.list_item, null);
                TextView text = (TextView) header.findViewById(R.id.textId);
                text.setText("This is for header test");
                ImageView image = (ImageView) header.findViewById(R.id.imageId);
                image.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                TextView footer1 = new TextView(getContext());
                footer1.setTextSize(30.0f);
                footer1.setText("This is a test Footer1!");
                TextView footer2 = new TextView(getContext());
                footer2.setTextSize(60.0f);
                footer2.setText("This is a test Footer2!");

                adapterWrapper.addHeader(header);
                adapterWrapper.addFooter(footer1);
                adapterWrapper.addFooter(footer2);
                recyclerView.setAdapter(adapterWrapper);
            }else if (section==4){
                rootView = inflater.inflate(R.layout.fragment_four, container, false);
                final PieChart pie = (PieChart) rootView.findViewById(R.id.Pie);
                Resources res = getResources();
                pie.addItem("Agamemnon", 2, res.getColor(R.color.seafoam));
                pie.addItem("Bocephus", 3.5f, res.getColor(R.color.chartreuse));
                pie.addItem("Calliope", 2.5f, res.getColor(R.color.emerald));
                pie.addItem("Daedalus", 3, res.getColor(R.color.bluegrass));
                pie.addItem("Euripides", 1, res.getColor(R.color.turquoise));
                pie.addItem("Ganymede", 3, res.getColor(R.color.slate));

                ((Button) rootView.findViewById(R.id.Reset)).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        pie.setCurrentItem(0);
                    }
                });
            }else if (section==5){
                rootView = inflater.inflate(R.layout.fragment_five, container, false);
            }else{
                rootView = inflater.inflate(R.layout.fragment_six, container, false);

                ConnerLayout connerLayout = (ConnerLayout) rootView.findViewById(R.id.connerLayoutId);
                final View finalRootView = rootView;
                connerLayout.post(new Runnable() {// Post in the parent's message queue to make sure the parent lays out its children before you call getHitRect()
                    @Override
                    public void run() {
                        // The bounds for the delegate view (an ImageButton in here)
                        Rect delegateArea = new Rect();
                        ImageButton imageButton = (ImageButton) finalRootView.findViewById(R.id.imageButtonId);
                        imageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), "Touch occurred within ImageButton touch region.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        //the hit rectangle for the imageButton
                        imageButton.getHitRect(delegateArea);
                        //extend the touch area
                        delegateArea.right += 100;
                        delegateArea.bottom += 100;
                        TouchDelegate touchDelegate = new TouchDelegate(delegateArea, imageButton);
                        if (View.class.isInstance(imageButton.getParent())){
                            ((View) imageButton.getParent()).setTouchDelegate(touchDelegate);
                        }
                    }
                });
            }
            return rootView;
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private final Context mContext;

        public MyAdapter(Context context){
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder){
                ((ViewHolder)holder).textView.setText("Index: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 20;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
                case 3:
                    return "SECTION 4";
                case 4:
                    return "SECTION 5";
                case 5:
                    return "SECTION 6";
            }
            return null;
        }
    }
}
