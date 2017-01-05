package herosauce.app1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ThreePartFragment extends Fragment{

    // Store instance variables
    private String title;
    private String body;
    private String body_middle;
    private String body_bottom;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static ThreePartFragment newInstance(int page, String title, String body, String body_middle, String body_bottom) {
        ThreePartFragment fragment = new ThreePartFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("pageTitle", title);
        args.putString("pageBody", body);
        args.putString("pageMiddle", body_middle);
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
        body_middle = getArguments().getString("pageMiddle");
        body_bottom = getArguments().getString("pageBottom");

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_quick_start_three_part_fragment, container, false);
        TextView tvLabel = (TextView) view.findViewById(R.id.fragment_title);
        TextView tvBody = (TextView) view.findViewById(R.id.fragment_body);
        TextView tvMiddle = (TextView) view.findViewById(R.id.fragment_body_middle);
        TextView tvBottom = (TextView) view.findViewById(R.id.fragment_body_bottom);
        String pageTitle = String.valueOf(page) + " -- " + title;
        tvLabel.setText(pageTitle);
        tvBody.setText(body);
        tvMiddle.setText(body_middle);
        tvBottom.setText(body_bottom);

        return view;
    }
}
