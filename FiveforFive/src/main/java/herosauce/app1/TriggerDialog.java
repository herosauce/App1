package herosauce.app1;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static android.content.Context.MODE_PRIVATE;
import static herosauce.app1.R.id.bCancel;
import static herosauce.app1.Settings.FUSE_LENGTH;

/**
 * Created by herosauce on 1/27/2017.
 */

public class TriggerDialog extends DialogFragment {

    public TriggerDialog() {
    }

    public interface UserDialog {
        void onFinishUserDialog(String user);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.dialog_trigger, container);

        //Setting up buttons
        Button bSave = (Button) view.findViewById(R.id.bSave);
        Button bCancel= (Button) view.findViewById(R.id.bCancel);

        final SharedPreferences clicksSP = this.getActivity().getSharedPreferences(FUSE_LENGTH, MODE_PRIVATE);

        final EditText editText = (EditText) view.findViewById(R.id.et_timer_length);
        editText.setText(String.valueOf(clicksSP.getInt("fuse",15)));

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numClicks = Integer.parseInt(editText.getText().toString());
                clicksSP.edit().putInt("fuse", numClicks).apply();
                getDialog().dismiss();
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(final DialogInterface dialog){
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener){
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
