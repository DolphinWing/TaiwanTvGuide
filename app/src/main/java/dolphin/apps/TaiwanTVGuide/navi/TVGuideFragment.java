package dolphin.apps.TaiwanTVGuide.navi;


import android.app.ActionBar;
import androidx.fragment.app.Fragment;

import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

@Deprecated
/**
 * A simple {@link Fragment} subclass.
 */
public abstract class TVGuideFragment extends Fragment {

    public TVGuideFragment() {
        // Required empty public constructor
    }

    ActionBar getSActionBar() {
        return getActivity().getActionBar();
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        TextView textView = new TextView(getActivity());
//        textView.setText(R.string.hello_blank_fragment);
//        return textView;
//    }

    public abstract void updateView(ProgramItem pItem);

}
