package herosauce.app1;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by herosauce on 2/24/2016.
 */
public class QuickStartFragment extends Fragment{
    //TODO: rename this class and clean up the junk.
    //TODO: v.2 Add a button to the last page that dismisses it
    //TODO: v.2 make the pages more interesting, and have them show what to do. Or something.
    // Store instance variables
    private String title;
    private String body;
    private String body_bottom;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static QuickStartFragment newInstance(int page, String title, String body, String body_bottom) {
        QuickStartFragment fragment = new QuickStartFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("pageTitle", title);
        args.putString("pageBody", body);
        args.putString("pageBottom", body_bottom);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("pageTitle");
        body = getArguments().getString("pageBody");
        body_bottom = getArguments().getString("pageBottom");

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_quick_start_fragment, container, false);
        TextView tvLabel = (TextView) view.findViewById(R.id.fragment_title);
        TextView tvBody = (TextView) view.findViewById(R.id.fragment_body);
        TextView tvBottom = (TextView) view.findViewById(R.id.fragment_body_bottom);
        String pageTitle = String.valueOf(page) + " -- " + title;
        tvLabel.setText(pageTitle);
        tvBody.setText(body);
        tvBottom.setText(body_bottom);

        return view;
    }
}
